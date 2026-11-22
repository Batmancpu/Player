package com.batmancpu.player.ui.library

import android.net.Uri
import java.io.File
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batmancpu.player.R
import com.batmancpu.player.ui.glass.PlayerGlassDock
import com.batmancpu.player.ui.glass.PlayerUiSettingsStore
import com.batmancpu.player.ui.icons.PlayerGlyph
import com.batmancpu.player.ui.icons.PlayerGlyphIcon
import com.batmancpu.player.ui.theme.PlayerPalette
import com.batmancpu.player.ui.metadata.MetadataLookupIssue
import com.batmancpu.player.ui.metadata.MetadataLookupUiState
import com.batmancpu.player.ui.metadata.TmdbCandidate
import com.batmancpu.player.ui.metadata.MetadataSettingsStore
import com.batmancpu.player.ui.metadata.TmdbArtworkCache
import com.batmancpu.player.ui.metadata.TmdbImageSize
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocalLibraryScreen(
    model: LibraryViewModel,
    onOpenGlassLab: () -> Unit,
    onOpenMetadataSettings: () -> Unit = {},
) {
    val context = LocalContext.current
    val appContext = remember(context) { context.applicationContext }
    val activity = context.activity()
    DisposableEffect(activity, model) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) model.clearMetadata()
        }
        activity?.lifecycle?.addObserver(observer)
        onDispose { activity?.lifecycle?.removeObserver(observer) }
    }
    val folderState by model.folder.collectAsState()
    val library by model.session.state.collectAsState()
    val snapshot by model.catalogue.collectAsState()
    val metadataState by model.metadataLookup.collectAsState()
    val artworkCache = remember(appContext) { TmdbArtworkCache(appContext, MetadataSettingsStore(appContext)) }
    val folder = folderState.folder
    var cachedMetadataByDocumentId by remember(folderState.folder?.tree) { mutableStateOf(emptyMap<String, CachedTmdbMetadata>()) }
    LaunchedEffect(folder?.tree, snapshot, metadataState.cached?.tmdbId, metadataState.cached?.cachedAtEpochMs, metadataState.posterFile) {
        val tree = folder?.tree
        cachedMetadataByDocumentId = if (tree != null && snapshot.tree == tree) {
            OfflineMetadataCache.load(appContext, tree)
        } else emptyMap()
    }
    if (folder == null || folderState.busy) {
        FolderSetupScreen(model, onOpenGlassLab)
        return
    }
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) model.connectFolder(uri.toString())
    }
    LocalLibraryContent(
        folder, library, snapshot,
        onScan = model.session::startScan,
        onCancel = model.session::cancelScan,
        scanStillRunning = { model.session.state.value.isScanning },
        onChooseFolder = { picker.launch(Uri.parse(folder.tree)) },
        onPlay = model::play,
        onOpenGlassLab = onOpenGlassLab,
        onOpenMetadataSettings = onOpenMetadataSettings,
        onForgetFolder = model::forgetFolder,
        metadataState = metadataState,
        cachedMetadataByDocumentId = cachedMetadataByDocumentId,
        artworkCache = artworkCache,
        posterRefreshKey = metadataState.posterFile,
        onPrepareMetadata = model::prepareMetadata,
        onSearchMetadata = model::searchMetadata,
        onConfirmMetadata = model::confirmMetadata,
        onRemoveMetadata = model::removeMetadata,
        onClearMetadata = model::clearMetadata,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocalLibraryContent(
    folder: LocalFolder,
    library: LibrarySessionState,
    snapshot: CatalogueSnapshot,
    onScan: () -> Unit,
    onCancel: () -> Unit,
    scanStillRunning: () -> Boolean,
    onChooseFolder: () -> Unit,
    onPlay: (DiscoveredLocalMedia) -> Unit,
    onOpenGlassLab: () -> Unit,
    onOpenMetadataSettings: () -> Unit = {},
    onForgetFolder: () -> Unit = {},
    metadataState: MetadataLookupUiState = MetadataLookupUiState(),
    onPrepareMetadata: (LocalTitle) -> Unit = {},
    onSearchMetadata: (LocalTitle) -> Unit = {},
    onConfirmMetadata: (LocalTitle, TmdbCandidate) -> Unit = { _, _ -> },
    onRemoveMetadata: (LocalTitle) -> Unit = {},
    onClearMetadata: () -> Unit = {},
    cachedMetadataByDocumentId: Map<String, CachedTmdbMetadata> = emptyMap(),
    artworkCache: TmdbArtworkCache? = null,
    posterRefreshKey: File? = null,
) {
    val titles = snapshot.takeIf { it.tree == folder.tree }?.titles.orEmpty()
    val matchingLibrary = library.takeIf { it.tree == folder.tree }
    val loading = matchingLibrary == null || library.isLoading || snapshot.tree != folder.tree
    var query by rememberSaveable { mutableStateOf("") }
    var selectedDestination by rememberSaveable { mutableIntStateOf(0) }
    val filter = when (selectedDestination) {
        1 -> CatalogueFilter.MOVIES
        2 -> CatalogueFilter.SERIES
        else -> CatalogueFilter.ALL
    }
    val searchFocusRequester = remember { FocusRequester() }
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var confirmForget by rememberSaveable { mutableStateOf(false) }
    val results = remember(titles, query, filter) { LocalCatalogue.search(titles, query, filter) }
    val movieShelf = remember(results) { results.filterNot(LocalTitle::isSeries) }
    val seriesShelf = remember(results) { results.filter(LocalTitle::isSeries) }
    val selected = titles.firstOrNull { it.key == selectedKey }
    val context = LocalContext.current
    val uiSettings = remember(context) { PlayerUiSettingsStore(context) }
    var glassEnabled by rememberSaveable { mutableStateOf(uiSettings.glassEnabled()) }
    var selectedCandidateId by rememberSaveable(selectedKey) { mutableStateOf<Long?>(null) }
    LaunchedEffect(folder.tree, selected?.key) {
        selectedCandidateId = null
        if (selected == null) onClearMetadata() else onPrepareMetadata(selected)
    }
    DisposableEffect(Unit) { onDispose { onClearMetadata() } }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val started = stringResource(R.string.catalogue_scan_started)
    val cancelled = stringResource(R.string.library_scan_cancelled)
    val finished = stringResource(R.string.catalogue_scan_finished)
    fun scanAction() {
        if (library.isScanning) {
            val wasRunning = scanStillRunning()
            onCancel()
            scope.launch { snackbar.showSnackbar(if (wasRunning) cancelled else finished) }
        } else {
            onScan()
            scope.launch { snackbar.showSnackbar(started) }
        }
    }
    val hazeState = rememberHazeState()
    val glassSource = if (glassEnabled) Modifier.hazeSource(state = hazeState) else Modifier
    Scaffold(
        containerColor = PlayerPalette.background,
        contentColor = PlayerPalette.textPrimary,
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            PlayerGlassDock(
                hazeState = hazeState,
                enabled = glassEnabled,
                selected = selectedDestination,
                onSelected = { index ->
                    selectedDestination = index
                    selectedKey = null
                    if (index == 3) {
                        searchFocusRequester.requestFocus()
                    } else {
                        query = ""
                        focus.clearFocus()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 18.dp, vertical = 8.dp),
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp).then(glassSource)) {
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.catalogue_kicker), style = MaterialTheme.typography.labelSmall, color = PlayerPalette.accentMuted)
                    Text(
                        stringResource(if (selectedDestination == 3) R.string.nav_search else R.string.catalogue_title),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                }
                TextButton(onClick = { settings = true }) { Text(stringResource(R.string.catalogue_manage)) }
            }
            Row(Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.catalogue_counts, library.media.count { it.kind == LocalMediaKind.VIDEO }, library.media.count { it.kind == LocalMediaKind.SUBTITLE }),
                    color = PlayerPalette.textSecondary, style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = ::scanAction, enabled = !loading) {
                    Text(stringResource(if (library.isScanning) R.string.library_scan_cancel else R.string.catalogue_refresh))
                }
            }
            OutlinedTextField(
                value = query, onValueChange = { query = it }, singleLine = true,
                label = { Text(stringResource(R.string.catalogue_search)) },
                leadingIcon = { PlayerGlyphIcon(PlayerGlyph.SEARCH, null) },
                modifier = Modifier.fillMaxWidth().focusRequester(searchFocusRequester),
                shape = RoundedCornerShape(24.dp),
            )
            Spacer(Modifier.height(12.dp))
            when {
                library.isScanning -> {
                    LinearProgressIndicator(Modifier.fillMaxWidth())
                    Text(stringResource(R.string.library_scan_progress), style = MaterialTheme.typography.bodySmall, color = PlayerPalette.textSecondary)
                }
                library.hasError -> Text(stringResource(R.string.library_scan_error), color = PlayerPalette.accentMuted)
                library.wasCancelled -> Text(cancelled, color = PlayerPalette.textSecondary, style = MaterialTheme.typography.bodySmall)
                library.hasCompletedScan -> Text(finished, color = PlayerPalette.textSecondary, style = MaterialTheme.typography.bodySmall)
            }
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (results.isEmpty()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(if (titles.isEmpty()) R.string.library_index_empty else R.string.catalogue_no_matches), style = MaterialTheme.typography.titleLarge)
                    if (titles.isEmpty()) Button(onClick = ::scanAction) { Text(stringResource(R.string.library_scan_start)) }
                    else TextButton(onClick = { query = ""; selectedDestination = 0; focus.clearFocus() }) { Text(stringResource(R.string.catalogue_clear_search)) }
                }
            } else if (selectedDestination == 3 && query.isBlank()) {
                Box(Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                    Text(
                        stringResource(R.string.catalogue_search_prompt),
                        color = PlayerPalette.textSecondary,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
            } else if (query.isBlank() && filter == CatalogueFilter.ALL) {
                val featuredTitle = movieShelf.firstOrNull() ?: seriesShelf.firstOrNull()
                LazyColumn(
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(26.dp),
                ) {
                    if (featuredTitle != null) {
                        item(key = "catalogue-featured") {
                            CatalogueFeatureHero(
                                title = featuredTitle,
                                cachedMetadataByDocumentId = cachedMetadataByDocumentId,
                                artworkCache = artworkCache,
                                posterRefreshKey = posterRefreshKey,
                                onPlay = { onPlay(featuredTitle.files.first()); selectedKey = null },
                                onDetails = { focus.clearFocus(); selectedKey = featuredTitle.key },
                            )
                        }
                    }
                    if (movieShelf.isNotEmpty()) {
                        item(key = "shelf-movies") {
                            CatalogueShelf(
                                title = R.string.nav_movies,
                                titles = movieShelf,
                                cachedMetadataByDocumentId = cachedMetadataByDocumentId,
                                artworkCache = artworkCache,
                                posterRefreshKey = posterRefreshKey,
                                onSelect = { focus.clearFocus(); selectedKey = it.key },
                            )
                        }
                    }
                    if (seriesShelf.isNotEmpty()) {
                        item(key = "shelf-series") {
                            CatalogueShelf(
                                title = R.string.catalogue_shelf_series,
                                titles = seriesShelf,
                                cachedMetadataByDocumentId = cachedMetadataByDocumentId,
                                artworkCache = artworkCache,
                                posterRefreshKey = posterRefreshKey,
                                onSelect = { focus.clearFocus(); selectedKey = it.key },
                            )
                        }
                    }
                    item(key = "catalogue-local-note") {
                        Text(stringResource(R.string.catalogue_local_note), color = PlayerPalette.textTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(132.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    items(results, key = { it.key }) { title ->
                        LocalTitleCard(title, cachedMetadataByDocumentId, artworkCache, posterRefreshKey) { focus.clearFocus(); selectedKey = title.key }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(stringResource(R.string.catalogue_local_note), color = PlayerPalette.textTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
    if (selected != null) {
        val episodesBySeason = remember(selected.key, selected.files) {
            selected.files.mapNotNull { file ->
                (MediaNameParser.parse(file.displayName) as? ParsedMediaName.Episode)?.let { file to it }
            }.groupBy(keySelector = { it.second.season }, valueTransform = { it }).toSortedMap()
        }
        val lookup = metadataState.takeIf { it.tree == folder.tree && it.titleKey == selected.key }
        val chosenCandidate = lookup?.candidates?.firstOrNull { it.id == selectedCandidateId }
        val detailsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { selectedKey = null },
            sheetState = detailsSheetState,
            containerColor = PlayerPalette.surface,
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item(key = "hero") {
                    val artworkDescription = stringResource(
                        if (lookup?.backdropFile == null) R.string.catalogue_detail_placeholder_description
                        else R.string.catalogue_detail_artwork_description,
                        selected.title,
                    )
                    val heroPalette = listOf(Color(0xFF26344A), Color(0xFF45364A), Color(0xFF24433D), Color(0xFF514033))
                    val heroColor = heroPalette[selected.key.hashCode().ushr(1) % heroPalette.size]
                    Box(
                        Modifier.fillMaxWidth().height(218.dp).clip(RoundedCornerShape(26.dp)),
                    ) {
                        if (lookup?.backdropFile != null) {
                            AsyncImage(
                                model = lookup.backdropFile,
                                contentDescription = artworkDescription,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                            )
                        } else {
                            Box(
                                Modifier.fillMaxSize()
                                    .background(Brush.linearGradient(listOf(heroColor, PlayerPalette.background)))
                                    .semantics { contentDescription = artworkDescription },
                            )
                            Text(
                                selected.title.take(1).uppercase(),
                                modifier = Modifier.align(Alignment.Center),
                                style = MaterialTheme.typography.displayLarge,
                                color = Color.White.copy(alpha = .24f),
                            )
                        }
                        Box(Modifier.fillMaxSize().background(
                            Brush.verticalGradient(listOf(Color.Black.copy(alpha = .10f), Color.Transparent, Color.Black.copy(alpha = .82f))),
                        ))
                        TextButton(
                            onClick = { selectedKey = null },
                            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        ) { Text(stringResource(R.string.catalogue_close_details), color = Color.White) }
                        Column(
                            Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Text(selected.title, style = MaterialTheme.typography.headlineLarge, color = Color.White, maxLines = 2, overflow = TextOverflow.Ellipsis)
                            Text(
                                selected.year?.toString() ?: lookup?.cached?.year?.toString() ?: stringResource(R.string.catalogue_on_device),
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = .82f),
                            )
                        }
                    }
                }
                if (!selected.isSeries) {
                    item(key = "filename") {
                        Text(selected.files.first().displayName, color = PlayerPalette.textSecondary)
                    }
                    item(key = "play") {
                        Button(onClick = { onPlay(selected.files.first()); selectedKey = null }, modifier = Modifier.fillMaxWidth()) {
                            Text(stringResource(R.string.catalogue_play))
                        }
                    }
                } else {
                    item(key = "episode-count") {
                        Text(pluralStringResource(R.plurals.catalogue_episodes, selected.files.size, selected.files.size), color = PlayerPalette.textSecondary)
                    }
                    episodesBySeason.forEach { (season, episodes) ->
                        item(key = "season-$season") {
                            Text(
                                stringResource(R.string.catalogue_season, season),
                                modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 2.dp),
                                style = MaterialTheme.typography.labelLarge,
                                color = PlayerPalette.accentMuted,
                            )
                        }
                        items(episodes, key = { it.first.documentId }) { (file, episode) ->
                            TextButton(onClick = { onPlay(file); selectedKey = null }, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    stringResource(R.string.catalogue_episode_label, episode.season, episode.episode),
                                    modifier = Modifier.fillMaxWidth(),
                                    color = PlayerPalette.textPrimary,
                                )
                            }
                        }
                    }
                }
                item(key = "metadata") {
                Surface(color = PlayerPalette.background, shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(stringResource(R.string.metadata_lookup_heading), style = MaterialTheme.typography.labelLarge, color = PlayerPalette.accentMuted)
                        Text(stringResource(R.string.metadata_lookup_explanation), style = MaterialTheme.typography.bodySmall, color = PlayerPalette.textSecondary)
                        when {
                            lookup == null || lookup.preparing -> {
                                CircularProgressIndicator()
                            }
                            lookup.cached != null -> {
                                Text(stringResource(R.string.metadata_lookup_matched), style = MaterialTheme.typography.titleMedium, color = PlayerPalette.textPrimary)
                                Text(
                                    lookup.cached.title + (lookup.cached.year?.let { " ($it)" } ?: ""),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = PlayerPalette.textPrimary,
                                )
                                lookup.posterFile?.let { poster ->
                                    AsyncImage(
                                        model = poster,
                                        contentDescription = stringResource(R.string.metadata_lookup_poster_description, lookup.cached.title),
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.width(144.dp).height(216.dp),
                                    )
                                }
                                if (lookup.cached.overview.isNotBlank()) {
                                    Text(lookup.cached.overview, style = MaterialTheme.typography.bodyMedium, color = PlayerPalette.textSecondary)
                                }
                                Text(
                                    stringResource(R.string.metadata_lookup_matched_files, lookup.matchedFiles, selected.files.size),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PlayerPalette.textTertiary,
                                )
                                OutlinedButton(
                                    onClick = { onRemoveMetadata(selected) },
                                    enabled = !lookup.confirming && !lookup.preparing,
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(stringResource(R.string.metadata_lookup_remove)) }
                            }
                            !lookup.searchable -> Text(
                                stringResource(R.string.metadata_lookup_unsafe_title),
                                style = MaterialTheme.typography.bodySmall,
                                color = PlayerPalette.textSecondary,
                            )
                            !lookup.settingsEnabled || !lookup.credentialConfigured -> {
                                Text(stringResource(R.string.metadata_lookup_setup_required), color = PlayerPalette.textSecondary)
                                OutlinedButton(
                                    onClick = { selectedKey = null; onOpenMetadataSettings() },
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(stringResource(R.string.metadata_lookup_open_settings)) }
                            }
                            else -> {
                                Button(
                                    onClick = { selectedCandidateId = null; onSearchMetadata(selected) },
                                    enabled = !lookup.searching && !lookup.confirming,
                                    modifier = Modifier.fillMaxWidth(),
                                ) { Text(stringResource(if (lookup.searching) R.string.metadata_lookup_searching else R.string.metadata_lookup_search)) }
                                if (lookup.searching) LinearProgressIndicator(Modifier.fillMaxWidth())
                                val issueText = when (lookup.issue) {
                                    MetadataLookupIssue.NO_RESULTS -> R.string.metadata_lookup_no_results
                                    MetadataLookupIssue.UNAUTHORIZED -> R.string.metadata_lookup_unauthorized
                                    MetadataLookupIssue.RATE_LIMITED -> R.string.metadata_lookup_rate_limited
                                    MetadataLookupIssue.UNAVAILABLE -> R.string.metadata_lookup_unavailable
                                    MetadataLookupIssue.INVALID_RESPONSE -> R.string.metadata_lookup_invalid_response
                                    MetadataLookupIssue.NEEDS_SETUP -> R.string.metadata_lookup_setup_required
                                    MetadataLookupIssue.STALE_LIBRARY -> R.string.metadata_lookup_stale
                                    MetadataLookupIssue.LOCAL_ERROR -> R.string.metadata_lookup_local_error
                                    MetadataLookupIssue.NO_SAFE_TITLE, null -> null
                                }
                                issueText?.let { Text(stringResource(it), color = PlayerPalette.textSecondary, style = MaterialTheme.typography.bodySmall) }
                                lookup.candidates.forEach { candidate ->
                                    Surface(color = PlayerPalette.surface, shape = RoundedCornerShape(16.dp)) {
                                        Row(
                                            Modifier.fillMaxWidth().clickable { selectedCandidateId = candidate.id }.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            RadioButton(
                                                selected = selectedCandidateId == candidate.id,
                                                onClick = { selectedCandidateId = candidate.id },
                                            )
                                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Text(
                                                    candidate.title + (candidate.year?.let { " ($it)" } ?: ""),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = PlayerPalette.textPrimary,
                                                )
                                                if (candidate.overview.isNotBlank()) {
                                                    Text(candidate.overview, style = MaterialTheme.typography.bodySmall, color = PlayerPalette.textSecondary, maxLines = 3, overflow = TextOverflow.Ellipsis)
                                                }
                                            }
                                        }
                                    }
                                }
                                if (chosenCandidate != null) {
                                    Button(
                                        onClick = { onConfirmMetadata(selected, chosenCandidate) },
                                        enabled = !lookup.confirming,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        Text(if (selected.isSeries) {
                                            stringResource(R.string.metadata_lookup_confirm_series, selected.files.size)
                                        } else {
                                            stringResource(R.string.metadata_lookup_confirm_movie)
                                        })
                                    }
                                }
                                if (lookup.confirming) LinearProgressIndicator(Modifier.fillMaxWidth())
                            }
                        }
                    }
                }
                }
            }
        }
    }
    if (settings) {
        ModalBottomSheet(
            onDismissRequest = { settings = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = PlayerPalette.surface,
        ) {
            Column(
                Modifier.fillMaxWidth()
                    // Keep the last settings controls above Android's gesture/navigation area.
                    // Applying the inset before scrolling also leaves them reachable by touch.
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 28.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(stringResource(R.string.catalogue_settings), style = MaterialTheme.typography.headlineMedium, color = PlayerPalette.textPrimary)
                Text(folder.label, color = PlayerPalette.textSecondary)
                Button(onClick = { settings = false; scanAction() }, enabled = !loading, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(if (library.isScanning) R.string.library_scan_cancel else R.string.library_scan_start))
                }
                OutlinedButton(onClick = { settings = false; onChooseFolder() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.folder_setup_change_folder))
                }
                OutlinedButton(onClick = { confirmForget = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.catalogue_forget_folder))
                }
                OutlinedButton(onClick = { settings = false; onOpenMetadataSettings() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.metadata_open_settings))
                }
                TextButton(onClick = { settings = false; onOpenGlassLab() }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.catalogue_glass_preview))
                }
                Text(stringResource(R.string.catalogue_glass_note), color = PlayerPalette.textTertiary, style = MaterialTheme.typography.bodySmall)
                val glassDescription = stringResource(R.string.settings_glass_accessibility)
                val glassStateDescription = stringResource(if (glassEnabled) R.string.settings_glass_on else R.string.settings_glass_off)
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(stringResource(R.string.settings_glass_title), color = PlayerPalette.textPrimary)
                        Text(stringResource(R.string.settings_glass_description), color = PlayerPalette.textSecondary,
                            style = MaterialTheme.typography.bodySmall)
                    }
                    fun updateGlassPreference(enabled: Boolean) {
                        glassEnabled = enabled
                        uiSettings.setGlassEnabled(enabled)
                    }
                    Switch(
                        checked = glassEnabled,
                        onCheckedChange = ::updateGlassPreference,
                        modifier = Modifier.clearAndSetSemantics {
                            role = Role.Switch
                            toggleableState = if (glassEnabled) ToggleableState.On else ToggleableState.Off
                            // Include the localized state in the label as well as the native
                            // Switch state so Android accessibility services announce it clearly.
                            contentDescription = "$glassDescription, $glassStateDescription"
                            stateDescription = glassStateDescription
                            onClick(label = glassDescription) {
                                updateGlassPreference(!glassEnabled)
                                true
                            }
                        },
                    )
                }
            }
        }
    }
    if (confirmForget) {
        AlertDialog(
            onDismissRequest = { confirmForget = false },
            title = { Text(stringResource(R.string.catalogue_forget_title)) },
            text = { Text(stringResource(R.string.catalogue_forget_explanation)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmForget = false
                    settings = false
                    onForgetFolder()
                }) { Text(stringResource(R.string.catalogue_forget_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmForget = false }) {
                    Text(stringResource(R.string.catalogue_forget_cancel))
                }
            },
        )
    }
}

@Composable
private fun LocalTitleCard(
    title: LocalTitle,
    cachedMetadataByDocumentId: Map<String, CachedTmdbMetadata>,
    artworkCache: TmdbArtworkCache?,
    posterRefreshKey: File?,
    onClick: () -> Unit,
) {
    val palettes = listOf(Color(0xFF3B465C), Color(0xFF514350), Color(0xFF294D47), Color(0xFF5A4835))
    val color = palettes[title.key.hashCode().ushr(1) % palettes.size]
    val titleKind = stringResource(if (title.isSeries) R.string.catalogue_card_series else R.string.catalogue_card_movie)
    val titleContext = if (title.isSeries) {
        pluralStringResource(R.plurals.catalogue_episodes, title.files.size, title.files.size)
    } else {
        title.year?.toString() ?: stringResource(R.string.catalogue_on_device)
    }
    val cardAccessibilityDescription = stringResource(
        R.string.catalogue_card_accessibility,
        title.title,
        titleKind,
        titleContext,
    )
    val metadata = remember(title.key, cachedMetadataByDocumentId) {
        title.files.mapNotNull { cachedMetadataByDocumentId[it.documentId] }
            .distinctBy { it.tmdbId to it.mediaType }.singleOrNull()
    }
    var posterFile by remember(title.key, metadata?.posterPath) { mutableStateOf<File?>(null) }
    LaunchedEffect(metadata?.posterPath, posterRefreshKey) {
        posterFile = if (metadata?.posterPath != null && artworkCache != null) {
            artworkCache.cached(metadata.posterPath, TmdbImageSize.POSTER)
        } else null
    }
    Card(
        onClick = onClick,
        modifier = Modifier.semantics(mergeDescendants = true) {
            contentDescription = cardAccessibilityDescription
        },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = PlayerPalette.surface),
    ) {
        Box(Modifier.fillMaxWidth().aspectRatio(2f / 3f)) {
            if (posterFile != null) {
                AsyncImage(
                    model = posterFile,
                    // The clickable card's title/year provide its accessible name. Artwork is
                    // decorative here; a child contentDescription would replace that name.
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = .78f)))))
            } else {
                Box(
                    Modifier.fillMaxSize()
                        .background(Brush.linearGradient(listOf(color, PlayerPalette.surface))),
                )
                Text(
                    title.title.take(1).uppercase(),
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White.copy(alpha = .25f),
                )
                PlayerGlyphIcon(
                    if (title.isSeries) PlayerGlyph.SHOWS else PlayerGlyph.MOVIES, null,
                    modifier = Modifier.align(Alignment.Center), size = 38.dp, tint = Color.White.copy(alpha = .7f),
                )
            }
            Text(
                stringResource(if (title.isSeries) R.string.nav_shows else R.string.catalogue_on_device),
                color = Color.White.copy(alpha = .9f), style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.align(Alignment.BottomStart).padding(14.dp),
            )
        }
        Column(Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title.title, style = MaterialTheme.typography.titleMedium, color = PlayerPalette.textPrimary, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(
                if (title.isSeries) pluralStringResource(R.plurals.catalogue_episodes, title.files.size, title.files.size)
                else title.year?.toString() ?: stringResource(R.string.catalogue_local_video),
                color = PlayerPalette.textSecondary, style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun CatalogueFeatureHero(
    title: LocalTitle,
    cachedMetadataByDocumentId: Map<String, CachedTmdbMetadata>,
    artworkCache: TmdbArtworkCache?,
    posterRefreshKey: File?,
    onPlay: () -> Unit,
    onDetails: () -> Unit,
) {
    val metadata = remember(title.key, cachedMetadataByDocumentId) {
        title.files.mapNotNull { cachedMetadataByDocumentId[it.documentId] }
            .distinctBy { it.tmdbId to it.mediaType }.singleOrNull()
    }
    var backdrop by remember(title.key, metadata?.backdropPath) { mutableStateOf<File?>(null) }
    LaunchedEffect(metadata?.backdropPath, posterRefreshKey) {
        backdrop = metadata?.backdropPath?.let { path -> artworkCache?.cached(path, TmdbImageSize.BACKDROP) }
    }
    val yearOrCount = if (title.isSeries) {
        pluralStringResource(R.plurals.catalogue_episodes, title.files.size, title.files.size)
    } else title.year?.toString() ?: stringResource(R.string.catalogue_local_video)
    val palette = listOf(Color(0xFF344661), Color(0xFF594358), Color(0xFF31544A), Color(0xFF66513C))
    val accent = palette[title.key.hashCode().ushr(1) % palette.size]
    Box(
        Modifier.fillMaxWidth().heightIn(min = 188.dp).clip(RoundedCornerShape(28.dp))
            .background(Brush.linearGradient(listOf(accent, PlayerPalette.surfaceElevated))),
    ) {
        if (backdrop != null) {
            AsyncImage(
                model = backdrop,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Text(
                title.title.take(1).uppercase(),
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White.copy(alpha = .11f),
            )
        }
        Box(
            Modifier.fillMaxSize().background(
                Brush.verticalGradient(listOf(Color.Black.copy(alpha = .04f), Color.Black.copy(alpha = .88f))),
            ),
        )
        Column(
            Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                stringResource(R.string.catalogue_featured_label),
                color = Color.White.copy(alpha = .76f),
                style = MaterialTheme.typography.labelSmall,
                letterSpacing = 1.2.sp,
            )
            Text(
                title.title,
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(yearOrCount, color = Color.White.copy(alpha = .78f), style = MaterialTheme.typography.bodySmall)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = onPlay, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.catalogue_feature_play))
                }
                TextButton(onClick = onDetails, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(stringResource(R.string.catalogue_details_action), color = Color.White)
                }
            }
        }
    }
}

private tailrec fun Context.activity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> if (baseContext === this) null else baseContext.activity()
    else -> null
}


@Composable
private fun CatalogueShelf(
    title: Int,
    titles: List<LocalTitle>,
    cachedMetadataByDocumentId: Map<String, CachedTmdbMetadata>,
    artworkCache: TmdbArtworkCache?,
    posterRefreshKey: File?,
    onSelect: (LocalTitle) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(title), style = MaterialTheme.typography.titleLarge, color = PlayerPalette.textPrimary)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            items(titles, key = { it.key }) { item ->
                Box(Modifier.width(156.dp)) {
                    LocalTitleCard(item, cachedMetadataByDocumentId, artworkCache, posterRefreshKey) { onSelect(item) }
                }
            }
        }
    }
}
