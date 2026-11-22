package com.batmancpu.player.ui.metadata

import android.content.Context
import android.os.SystemClock
import com.batmancpu.player.ui.library.CachedTmdbMetadata
import com.batmancpu.player.ui.library.LocalTitle
import com.batmancpu.player.ui.library.MediaNameParser
import com.batmancpu.player.ui.library.MetadataSearchConsent
import com.batmancpu.player.ui.library.OfflineMetadataCache
import com.batmancpu.player.ui.library.ParsedMediaName
import com.batmancpu.player.ui.library.TmdbSearchTerms
import java.io.File
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

internal enum class MetadataLookupIssue {
    NEEDS_SETUP, NO_SAFE_TITLE, NO_RESULTS, UNAUTHORIZED, RATE_LIMITED, UNAVAILABLE, INVALID_RESPONSE, STALE_LIBRARY, LOCAL_ERROR,
}

/** UI state never contains a credential, path or provider request URL. */
internal data class MetadataLookupUiState(
    val tree: String? = null,
    val titleKey: String? = null,
    val preparing: Boolean = false,
    val settingsEnabled: Boolean = false,
    val credentialConfigured: Boolean = false,
    val searchable: Boolean = false,
    val searching: Boolean = false,
    val confirming: Boolean = false,
    val candidates: List<TmdbCandidate> = emptyList(),
    val cached: CachedTmdbMetadata? = null,
    val posterFile: File? = null,
    val backdropFile: File? = null,
    val matchedFiles: Int = 0,
    val issue: MetadataLookupIssue? = null,
)

/**
 * Foreground-only lookup coordinator. Automatic searches are allowed only for
 * composed catalogue cards or an opened detail, after local opt-in. Card jobs
 * are bounded, serialized and cancelled when their cards leave composition.
 * It never searches during scanning, restoration or process startup.
 */
internal class MetadataLookupController(
    context: Context,
    private val scope: CoroutineScope,
    private val settings: MetadataSettingsStore,
    private val client: TmdbClient,
    private val artworkCache: TmdbArtworkCache = TmdbArtworkCache(context, settings),
    private val currentTree: () -> String?,
    private val isCurrentTitle: (String, LocalTitle) -> Boolean,
) {
    private val app = context.applicationContext
    private val mutableState = MutableStateFlow(MetadataLookupUiState())
    val state = mutableState.asStateFlow()
    private val mutableCatalogueRevision = MutableStateFlow(0L)
    val catalogueRevision: StateFlow<Long> = mutableCatalogueRevision.asStateFlow()
    @Volatile private var generation = 0L
    private var work: Job? = null
    private var attemptTree: String? = null
    private val automaticAttempts = LinkedHashSet<String>()
    private val visibleJobs = LinkedHashMap<String, Job>()
    private val visibleTitles = LinkedHashMap<String, Pair<String, LocalTitle>>()
    @Volatile private var catalogueForeground = true
    private val automaticRequestGate = Mutex()
    @Volatile private var lastAutomaticRequestElapsedRealtimeMs = 0L

    /** Queue work only for cards that are currently composed in the foreground catalogue. */
    fun catalogueTitleVisible(tree: String, title: LocalTitle) {
        if (currentTree() != tree || !isCurrentTitle(tree, title)) return
        val key = visibilityKey(tree, title.key)
        synchronized(visibleJobs) {
            if (visibleTitles.containsKey(key) || visibleTitles.size >= MAX_VISIBLE_CATALOGUE_TITLES) return
            visibleTitles[key] = tree to title
            if (catalogueForeground) startVisibleLocked(key, tree, title)
        }
    }

    fun catalogueTitleHidden(tree: String, titleKey: String) {
        val key = visibilityKey(tree, titleKey)
        val job = synchronized(visibleJobs) {
            visibleTitles.remove(key)
            visibleJobs.remove(key)
        }
        job?.cancel()
    }

    fun pauseCatalogueEnrichment() {
        catalogueForeground = false
        val jobs = synchronized(visibleJobs) {
            visibleJobs.values.toList().also { visibleJobs.clear() }
        }
        jobs.forEach { it.cancel() }
    }

    fun resumeCatalogueEnrichment() {
        catalogueForeground = true
        synchronized(visibleJobs) {
            visibleTitles.forEach { (key, value) ->
                if (visibleJobs[key]?.isActive != true) {
                    visibleJobs.remove(key)
                    startVisibleLocked(key, value.first, value.second)
                }
            }
        }
    }

    private fun startVisibleLocked(key: String, tree: String, title: LocalTitle) {
        val job = scope.launch(start = CoroutineStart.LAZY) {
            try {
                loadVisibleCatalogueTitle(tree, key, title)
            } finally {
                val completingJob = kotlinx.coroutines.currentCoroutineContext()[Job]
                synchronized(visibleJobs) {
                    if (visibleJobs[key] === completingJob) {
                        visibleJobs.remove(key)
                    }
                }
            }
        }
        visibleJobs[key] = job
        job.start()
    }

    fun prepare(title: LocalTitle) {
        val tree = currentTree()
        if (tree == null || !isCurrentTitle(tree, title)) {
            clear()
            return
        }
        val ticket = ++generation
        work?.cancel()
        mutableState.value = MetadataLookupUiState(tree = tree, titleKey = title.key, preparing = true)
        work = scope.launch {
            try {
                val configured = settings.load()
                val cachedByDocument = OfflineMetadataCache.load(app, tree)
                val fileIds = title.files.map { it.documentId }
                val localMatches = fileIds.mapNotNull(cachedByDocument::get)
                    .distinctBy { it.tmdbId to it.mediaType }
                val lookupSuppressed = OfflineMetadataCache.isLookupSuppressed(app, tree, fileIds)
                val searchable = searchTerms(title) != null
                val cachedMatch = localMatches.singleOrNull()
                val posterFile = cachedMatch?.posterPath?.let { artworkCache.cached(it, TmdbImageSize.POSTER) }
                val backdropFile = cachedMatch?.backdropPath?.let { artworkCache.cached(it, TmdbImageSize.BACKDROP) }
                if (!active(ticket, tree, title)) return@launch
                mutableState.value = MetadataLookupUiState(
                    tree = tree,
                    titleKey = title.key,
                    settingsEnabled = configured.enabled,
                    credentialConfigured = configured.credentialConfigured,
                    searchable = searchable,
                    cached = cachedMatch,
                    posterFile = posterFile,
                    backdropFile = backdropFile,
                    matchedFiles = if (localMatches.size == 1) {
                        fileIds.count { cachedByDocument[it] == localMatches.single() }
                    } else 0,
                    issue = when {
                        configured.storageError -> MetadataLookupIssue.LOCAL_ERROR
                        !searchable -> MetadataLookupIssue.NO_SAFE_TITLE
                        else -> null
                    },
                )
                val canAutomaticallySearch = configured.enabled && configured.credentialConfigured &&
                    searchable && localMatches.isEmpty() && !lookupSuppressed
                if (canAutomaticallySearch) {
                    searchTerms(title)?.let { terms ->
                        if (markAutomaticAttempt(tree, title.key)) {
                            runAutomaticLookup(ticket, tree, title, terms)
                        }
                    }
                } else if (configured.enabled && cachedMatch != null) {
                    // Metadata may outlive an evicted image file; refill only
                    // while the user is viewing the title and has opted in.
                    val poster = cachedMatch.posterPath?.let { artworkCache.fetch(it, TmdbImageSize.POSTER) }
                    val backdrop = cachedMatch.backdropPath?.let { artworkCache.fetch(it, TmdbImageSize.BACKDROP) }
                    if (active(ticket, tree, title) && (poster != null || backdrop != null)) {
                        mutableState.value = mutableState.value.copy(posterFile = poster, backdropFile = backdrop)
                    }
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                if (active(ticket, tree, title)) {
                    mutableState.value = MetadataLookupUiState(tree = tree, titleKey = title.key, issue = MetadataLookupIssue.LOCAL_ERROR)
                }
            }
        }
    }

    /** Called only by the visible Search TMDB action. */
    fun search(title: LocalTitle) {
        val before = mutableState.value
        val tree = before.tree ?: return
        if (before.titleKey != title.key || !before.searchable ||
            !before.settingsEnabled || !before.credentialConfigured ||
            !isCurrentTitle(tree, title)
        ) return
        val terms = searchTerms(title) ?: run {
            mutableState.value = before.copy(issue = MetadataLookupIssue.NO_SAFE_TITLE)
            return
        }
        val ticket = ++generation
        work?.cancel()
        mutableState.value = before.copy(searching = true, candidates = emptyList(), issue = null)
        work = scope.launch {
            try {
                when (val response = client.search(terms)) {
                    is TmdbResult.Success -> {
                        if (!active(ticket, tree, title)) return@launch
                        mutableState.value = mutableState.value.copy(
                            searching = false,
                            candidates = response.value,
                            issue = if (response.value.isEmpty()) MetadataLookupIssue.NO_RESULTS else null,
                        )
                    }
                    TmdbResult.Disabled -> publishIssue(ticket, tree, title, MetadataLookupIssue.NEEDS_SETUP)
                    TmdbResult.Unauthorized -> publishIssue(ticket, tree, title, MetadataLookupIssue.UNAUTHORIZED)
                    TmdbResult.RateLimited -> publishIssue(ticket, tree, title, MetadataLookupIssue.RATE_LIMITED)
                    TmdbResult.Unavailable -> publishIssue(ticket, tree, title, MetadataLookupIssue.UNAVAILABLE)
                    TmdbResult.InvalidResponse -> publishIssue(ticket, tree, title, MetadataLookupIssue.INVALID_RESPONSE)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                publishIssue(ticket, tree, title, MetadataLookupIssue.UNAVAILABLE)
            }
        }
    }

    /** A candidate is persisted only after the user taps a separate confirmation action. */
    fun confirm(title: LocalTitle, candidate: TmdbCandidate) {
        val before = mutableState.value
        val tree = before.tree ?: return
        val offered = before.candidates.firstOrNull {
            it.id == candidate.id && it.mediaType == candidate.mediaType
        } ?: return
        if (before.titleKey != title.key || before.confirming || !isCurrentTitle(tree, title)) {
            mutableState.value = before.copy(issue = MetadataLookupIssue.STALE_LIBRARY)
            return
        }
        val ticket = ++generation
        work?.cancel()
        mutableState.value = before.copy(confirming = true, issue = null)
        work = scope.launch {
            try {
                if (settings.credentialIfEnabled() == null || !active(ticket, tree, title)) {
                    publishIssue(ticket, tree, title, MetadataLookupIssue.NEEDS_SETUP)
                    return@launch
                }
                val metadata = CachedTmdbMetadata(
                    tmdbId = offered.id,
                    mediaType = offered.mediaType.name,
                    title = offered.title,
                    year = offered.year,
                    overview = offered.overview,
                    posterPath = offered.posterPath,
                    backdropPath = offered.backdropPath,
                    cachedAtEpochMs = System.currentTimeMillis(),
                )
                val stored = OfflineMetadataCache.bind(
                    app, tree, title.files.map { it.documentId }, metadata,
                )
                if (!active(ticket, tree, title)) return@launch
                mutableState.value = mutableState.value.copy(
                    confirming = false,
                    cached = metadata.takeIf { stored },
                    posterFile = null,
                    backdropFile = null,
                    matchedFiles = title.files.size.takeIf { stored } ?: 0,
                    candidates = emptyList(),
                    issue = if (stored) null else MetadataLookupIssue.STALE_LIBRARY,
                )
                // Fetch only after the explicit, separate match confirmation.
                val poster = metadata.posterPath?.takeIf { stored }?.let {
                    artworkCache.fetch(it, TmdbImageSize.POSTER)
                }
                val backdrop = metadata.backdropPath?.takeIf { stored }?.let {
                    artworkCache.fetch(it, TmdbImageSize.BACKDROP)
                }
                if ((poster != null || backdrop != null) && active(ticket, tree, title)) {
                    mutableState.value = mutableState.value.copy(posterFile = poster, backdropFile = backdrop)
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                publishIssue(ticket, tree, title, MetadataLookupIssue.LOCAL_ERROR)
            }
        }
    }

    fun remove(title: LocalTitle) {
        val before = mutableState.value
        val tree = before.tree ?: return
        if (before.titleKey != title.key || !isCurrentTitle(tree, title)) {
            mutableState.value = before.copy(issue = MetadataLookupIssue.STALE_LIBRARY)
            return
        }
        val ticket = ++generation
        work?.cancel()
        mutableState.value = before.copy(preparing = true, confirming = false, issue = null)
        work = scope.launch {
            try {
                OfflineMetadataCache.suppressLookup(app, tree, title.files.map { it.documentId })
                if (!active(ticket, tree, title)) return@launch
                mutableState.value = before.copy(preparing = false, cached = null, matchedFiles = 0, candidates = emptyList(), issue = null)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                publishIssue(ticket, tree, title, MetadataLookupIssue.LOCAL_ERROR)
            }
        }
    }

    fun clear() {
        generation++
        work?.cancel()
        work = null
        mutableState.value = MetadataLookupUiState()
    }

    private suspend fun runAutomaticLookup(
        ticket: Long,
        tree: String,
        title: LocalTitle,
        terms: TmdbSearchTerms,
    ) {
        try {
            if (!active(ticket, tree, title) || settings.credentialIfEnabled() == null) return
            mutableState.value = mutableState.value.copy(searching = true)
            val response = automaticSearch(ticket, tree, title, terms)
            when (response) {
                is TmdbResult.Success -> {
                    if (!active(ticket, tree, title)) return
                    val candidate = TmdbAutoMatchPolicy.uniqueExactMatch(terms, response.value)
                    if (candidate == null) {
                        // Keep ambiguous/non-exact suggestions visible for the
                        // existing explicit confirmation flow; never guess.
                        mutableState.value = mutableState.value.copy(
                            searching = false,
                            candidates = response.value,
                            issue = if (response.value.isEmpty()) MetadataLookupIssue.NO_RESULTS else null,
                        )
                        return
                    }
                    val metadata = CachedTmdbMetadata(
                        tmdbId = candidate.id,
                        mediaType = candidate.mediaType.name,
                        title = candidate.title,
                        year = candidate.year,
                        overview = candidate.overview,
                        posterPath = candidate.posterPath,
                        backdropPath = candidate.backdropPath,
                        cachedAtEpochMs = System.currentTimeMillis(),
                    )
                    val stored = OfflineMetadataCache.bind(
                        app, tree, title.files.map { it.documentId }, metadata, respectLookupSuppression = true,
                    )
                    if (!active(ticket, tree, title)) return
                    if (!stored) {
                        publishIssue(ticket, tree, title, MetadataLookupIssue.STALE_LIBRARY)
                        return
                    }
                    mutableState.value = mutableState.value.copy(
                        searching = false,
                        cached = metadata,
                        matchedFiles = title.files.size,
                        candidates = emptyList(),
                        issue = null,
                    )
                    val poster = metadata.posterPath?.let { artworkCache.fetch(it, TmdbImageSize.POSTER) }
                    val backdrop = metadata.backdropPath?.let { artworkCache.fetch(it, TmdbImageSize.BACKDROP) }
                    if (active(ticket, tree, title) && (poster != null || backdrop != null)) {
                        mutableState.value = mutableState.value.copy(posterFile = poster, backdropFile = backdrop)
                    }
                }
                TmdbResult.Disabled -> publishIssue(ticket, tree, title, MetadataLookupIssue.NEEDS_SETUP)
                TmdbResult.Unauthorized -> publishIssue(ticket, tree, title, MetadataLookupIssue.UNAUTHORIZED)
                TmdbResult.RateLimited -> publishIssue(ticket, tree, title, MetadataLookupIssue.RATE_LIMITED)
                TmdbResult.Unavailable -> publishIssue(ticket, tree, title, MetadataLookupIssue.UNAVAILABLE)
                TmdbResult.InvalidResponse -> publishIssue(ticket, tree, title, MetadataLookupIssue.INVALID_RESPONSE)
            }
        } catch (cancelled: CancellationException) {
            forgetAutomaticAttempt(tree, title.key)
            throw cancelled
        } catch (_: Exception) {
            publishIssue(ticket, tree, title, MetadataLookupIssue.UNAVAILABLE)
        }
    }

    private suspend fun loadVisibleCatalogueTitle(tree: String, key: String, title: LocalTitle) {
        try {
            val configured = settings.load()
            if (!configured.enabled || !configured.credentialConfigured || !visible(tree, key, title)) return
            val cachedByDocument = OfflineMetadataCache.load(app, tree)
            val fileIds = title.files.map { it.documentId }
            val localMatches = fileIds.mapNotNull(cachedByDocument::get).distinctBy { it.tmdbId to it.mediaType }
            if (localMatches.size > 1) return
            val cached = localMatches.singleOrNull()
            if (cached != null) {
                val poster = cached.posterPath?.let { artworkCache.fetch(it, TmdbImageSize.POSTER) }
                val backdrop = cached.backdropPath?.let { artworkCache.fetch(it, TmdbImageSize.BACKDROP) }
                if (visible(tree, key, title) && (poster != null || backdrop != null)) {
                    mutableCatalogueRevision.update { it + 1 }
                }
                return
            }
            if (OfflineMetadataCache.isLookupSuppressed(app, tree, fileIds) || !visible(tree, key, title)) return
            val terms = searchTerms(title) ?: return
            if (!markAutomaticAttempt(tree, title.key)) return
            if (settings.credentialIfEnabled() == null || !visible(tree, key, title)) return
            val result = automaticCatalogueSearch(tree, key, title, terms) ?: return
            if (result !is TmdbResult.Success || !visible(tree, key, title)) return
            val candidate = TmdbAutoMatchPolicy.uniqueExactMatch(terms, result.value) ?: return
            val metadata = CachedTmdbMetadata(
                tmdbId = candidate.id,
                mediaType = candidate.mediaType.name,
                title = candidate.title,
                year = candidate.year,
                overview = candidate.overview,
                posterPath = candidate.posterPath,
                backdropPath = candidate.backdropPath,
                cachedAtEpochMs = System.currentTimeMillis(),
            )
            if (!visible(tree, key, title) || !OfflineMetadataCache.bind(
                    app, tree, fileIds, metadata, respectLookupSuppression = true,
                )
            ) return
            mutableCatalogueRevision.update { it + 1 }
            publishCatalogueMatch(tree, title, metadata)
            val poster = metadata.posterPath?.let { artworkCache.fetch(it, TmdbImageSize.POSTER) }
            val backdrop = metadata.backdropPath?.let { artworkCache.fetch(it, TmdbImageSize.BACKDROP) }
            publishCatalogueMatch(tree, title, metadata, poster, backdrop)
            if (visible(tree, key, title) && (poster != null || backdrop != null)) {
                mutableCatalogueRevision.update { it + 1 }
            }
        } catch (cancelled: CancellationException) {
            forgetAutomaticAttempt(tree, title.key)
            throw cancelled
        } catch (_: Exception) {
            // Catalogue enrichment is opportunistic; local browsing and playback stay available.
        }
    }

    private suspend fun automaticCatalogueSearch(
        tree: String,
        key: String,
        title: LocalTitle,
        terms: TmdbSearchTerms,
    ): TmdbResult<List<TmdbCandidate>>? {
        automaticRequestGate.lock()
        try {
            if (!visible(tree, key, title) || settings.credentialIfEnabled() == null) return null
            val elapsed = SystemClock.elapsedRealtime()
            val waitMs = MIN_AUTOMATIC_REQUEST_INTERVAL_MS - (elapsed - lastAutomaticRequestElapsedRealtimeMs)
            if (waitMs > 0) delay(waitMs)
            if (!visible(tree, key, title) || settings.credentialIfEnabled() == null) return null
            lastAutomaticRequestElapsedRealtimeMs = SystemClock.elapsedRealtime()
            return client.search(terms)
        } finally {
            automaticRequestGate.unlock()
        }
    }

    private fun visible(tree: String, key: String, title: LocalTitle): Boolean =
        synchronized(visibleJobs) { visibleJobs[key]?.isActive == true } &&
            currentTree() == tree && isCurrentTitle(tree, title)

    private fun publishCatalogueMatch(
        tree: String,
        title: LocalTitle,
        metadata: CachedTmdbMetadata,
        poster: File? = null,
        backdrop: File? = null,
    ) {
        val current = mutableState.value
        if (current.tree == tree && current.titleKey == title.key) {
            mutableState.value = current.copy(
                preparing = false,
                searching = false,
                cached = metadata,
                posterFile = poster,
                backdropFile = backdrop,
                matchedFiles = title.files.size,
                candidates = emptyList(),
                issue = null,
            )
        }
    }

    private fun visibilityKey(tree: String, titleKey: String) = "$tree\u0000$titleKey"

    private suspend fun automaticSearch(
        ticket: Long,
        tree: String,
        title: LocalTitle,
        terms: TmdbSearchTerms,
    ): TmdbResult<List<TmdbCandidate>> {
        automaticRequestGate.lock()
        try {
            if (!active(ticket, tree, title) || settings.credentialIfEnabled() == null) return TmdbResult.Disabled
            val elapsed = SystemClock.elapsedRealtime()
            val waitMs = MIN_AUTOMATIC_REQUEST_INTERVAL_MS - (elapsed - lastAutomaticRequestElapsedRealtimeMs)
            if (waitMs > 0) delay(waitMs)
            if (!active(ticket, tree, title) || settings.credentialIfEnabled() == null) return TmdbResult.Disabled
            lastAutomaticRequestElapsedRealtimeMs = SystemClock.elapsedRealtime()
            return client.search(terms)
        } finally {
            automaticRequestGate.unlock()
        }
    }

    @Synchronized
    private fun markAutomaticAttempt(tree: String, titleKey: String): Boolean {
        if (attemptTree != tree) {
            automaticAttempts.clear()
            attemptTree = tree
        }
        if (!automaticAttempts.add(titleKey)) return false
        while (automaticAttempts.size > MAX_AUTOMATIC_ATTEMPTS) {
            automaticAttempts.iterator().run { next(); remove() }
        }
        return true
    }

    @Synchronized
    private fun forgetAutomaticAttempt(tree: String, titleKey: String) {
        if (attemptTree == tree) automaticAttempts.remove(titleKey)
    }

    private fun searchTerms(title: LocalTitle): TmdbSearchTerms? {
        val firstVideo = title.files.firstOrNull() ?: return null
        if (firstVideo.kind != com.batmancpu.player.ui.library.LocalMediaKind.VIDEO) return null
        val parsed = MediaNameParser.parse(firstVideo.displayName)
        if (parsed !is ParsedMediaName.Movie && parsed !is ParsedMediaName.Episode) return null
        return TmdbSearchTerms.from(parsed, MetadataSearchConsent.ENABLED_BY_USER)
    }

    private fun active(ticket: Long, tree: String, title: LocalTitle): Boolean =
        generation == ticket && currentTree() == tree && isCurrentTitle(tree, title)

    private fun publishIssue(ticket: Long, tree: String, title: LocalTitle, issue: MetadataLookupIssue) {
        if (active(ticket, tree, title)) {
            mutableState.value = mutableState.value.copy(preparing = false, searching = false, confirming = false, issue = issue)
        }
    }

    private companion object {
        const val MAX_AUTOMATIC_ATTEMPTS = 512
        const val MAX_VISIBLE_CATALOGUE_TITLES = 12
        const val MIN_AUTOMATIC_REQUEST_INTERVAL_MS = 800L
    }
}
