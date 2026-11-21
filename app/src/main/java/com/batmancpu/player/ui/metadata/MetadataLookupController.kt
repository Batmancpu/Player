package com.batmancpu.player.ui.metadata

import android.content.Context
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val matchedFiles: Int = 0,
    val issue: MetadataLookupIssue? = null,
)

/**
 * Explicit, foreground-only lookup coordinator. It never starts a search from
 * library loading, scanning, scrolling, restoration or process startup.
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
    @Volatile private var generation = 0L
    private var work: Job? = null

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
                val searchable = searchTerms(title) != null
                val cachedMatch = localMatches.singleOrNull()
                val posterFile = cachedMatch?.posterPath?.let { artworkCache.cached(it, TmdbImageSize.POSTER) }
                if (!active(ticket, tree, title)) return@launch
                mutableState.value = MetadataLookupUiState(
                    tree = tree,
                    titleKey = title.key,
                    settingsEnabled = configured.enabled,
                    credentialConfigured = configured.credentialConfigured,
                    searchable = searchable,
                    cached = cachedMatch,
                    posterFile = posterFile,
                    matchedFiles = if (localMatches.size == 1) {
                        fileIds.count { cachedByDocument[it] == localMatches.single() }
                    } else 0,
                    issue = when {
                        configured.storageError -> MetadataLookupIssue.LOCAL_ERROR
                        !searchable -> MetadataLookupIssue.NO_SAFE_TITLE
                        else -> null
                    },
                )
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
                val stored = OfflineMetadataCache.confirm(
                    app, tree, title.files.map { it.documentId }, metadata,
                )
                if (!active(ticket, tree, title)) return@launch
                mutableState.value = mutableState.value.copy(
                    confirming = false,
                    cached = metadata.takeIf { stored },
                    posterFile = null,
                    matchedFiles = title.files.size.takeIf { stored } ?: 0,
                    candidates = emptyList(),
                    issue = if (stored) null else MetadataLookupIssue.STALE_LIBRARY,
                )
                // Fetch only after the explicit, separate match confirmation.
                val poster = metadata.posterPath?.takeIf { stored }?.let {
                    artworkCache.fetch(it, TmdbImageSize.POSTER)
                }
                if (poster != null && active(ticket, tree, title)) {
                    mutableState.value = mutableState.value.copy(posterFile = poster)
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
                OfflineMetadataCache.remove(app, tree, title.files.map { it.documentId })
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
}
