package com.batmancpu.player.ui.library

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.batmancpu.player.R
import com.batmancpu.player.ui.icons.PlayerGlyph
import com.batmancpu.player.ui.icons.PlayerGlyphIcon
import com.batmancpu.player.ui.theme.PlayerPalette
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LocalLibraryScreen(
    model: LibraryViewModel,
    onOpenGlassLab: () -> Unit,
    onOpenMetadataSettings: () -> Unit = {},
) {
    val folderState by model.folder.collectAsState()
    val library by model.session.state.collectAsState()
    val snapshot by model.catalogue.collectAsState()
    val folder = folderState.folder
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
) {
    val titles = snapshot.takeIf { it.tree == folder.tree }?.titles.orEmpty()
    val matchingLibrary = library.takeIf { it.tree == folder.tree }
    val loading = matchingLibrary == null || library.isLoading || snapshot.tree != folder.tree
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf(CatalogueFilter.ALL) }
    var selectedKey by rememberSaveable { mutableStateOf<String?>(null) }
    var settings by rememberSaveable { mutableStateOf(false) }
    var confirmForget by rememberSaveable { mutableStateOf(false) }
    val results = remember(titles, query, filter) { LocalCatalogue.search(titles, query, filter) }
    val selected = titles.firstOrNull { it.key == selectedKey }
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
    Scaffold(
        containerColor = PlayerPalette.background,
        contentColor = PlayerPalette.textPrimary,
        snackbarHost = { SnackbarHost(snackbar) },
        contentWindowInsets = WindowInsets.safeDrawing,
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.catalogue_kicker), style = MaterialTheme.typography.labelSmall, color = PlayerPalette.accentMuted)
                    Text(stringResource(R.string.catalogue_title), style = MaterialTheme.typography.headlineLarge)
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
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp),
            )
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.weight(1f).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CatalogueFilter.entries.forEach { choice ->
                    FilterChip(selected = filter == choice, onClick = { filter = choice }, label = {
                        Text(stringResource(when (choice) {
                            CatalogueFilter.ALL -> R.string.catalogue_all
                            CatalogueFilter.MOVIES -> R.string.nav_movies
                            CatalogueFilter.SERIES -> R.string.nav_shows
                        }))
                    })
                }
                }
            }
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
                    else TextButton(onClick = { query = ""; filter = CatalogueFilter.ALL }) { Text(stringResource(R.string.catalogue_clear_search)) }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(132.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp), verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    items(results, key = { it.key }) { title ->
                        LocalTitleCard(title) { focus.clearFocus(); selectedKey = title.key }
                    }
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        Text(stringResource(R.string.catalogue_local_note), color = PlayerPalette.textTertiary, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
    if (selected != null) {
        ModalBottomSheet(onDismissRequest = { selectedKey = null }, containerColor = PlayerPalette.surface) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(selected.title, style = MaterialTheme.typography.headlineLarge, color = PlayerPalette.textPrimary)
                Text(selected.year?.toString() ?: stringResource(R.string.catalogue_on_device), color = PlayerPalette.textSecondary)
                if (!selected.isSeries) {
                    Text(selected.files.first().displayName, color = PlayerPalette.textSecondary)
                    Button(onClick = { onPlay(selected.files.first()); selectedKey = null }, modifier = Modifier.fillMaxWidth()) {
                        Text(stringResource(R.string.catalogue_play))
                    }
                } else {
                    Text(pluralStringResource(R.plurals.catalogue_episodes, selected.files.size, selected.files.size), color = PlayerPalette.textSecondary)
                    LazyColumn(Modifier.fillMaxWidth().heightIn(max = 380.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(selected.files, key = { it.documentId }) { file ->
                            TextButton(onClick = { onPlay(file); selectedKey = null }, modifier = Modifier.fillMaxWidth()) {
                                Text(file.displayName, modifier = Modifier.fillMaxWidth(), color = PlayerPalette.textPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
    if (settings) {
        ModalBottomSheet(onDismissRequest = { settings = false }, containerColor = PlayerPalette.surface) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 28.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
private fun LocalTitleCard(title: LocalTitle, onClick: () -> Unit) {
    val palettes = listOf(Color(0xFF3B465C), Color(0xFF514350), Color(0xFF294D47), Color(0xFF5A4835))
    val color = palettes[title.key.hashCode().ushr(1) % palettes.size]
    Card(onClick = onClick, shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = PlayerPalette.surface)) {
        Box(Modifier.fillMaxWidth().height(158.dp).background(Brush.verticalGradient(listOf(color, PlayerPalette.surface)))) {
            PlayerGlyphIcon(
                if (title.isSeries) PlayerGlyph.SHOWS else PlayerGlyph.MOVIES, null,
                modifier = Modifier.align(Alignment.Center), size = 38.dp, tint = Color.White.copy(alpha = .7f),
            )
            Text(
                stringResource(if (title.isSeries) R.string.nav_shows else R.string.catalogue_on_device),
                color = Color.White.copy(alpha = .75f), style = MaterialTheme.typography.labelSmall,
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
