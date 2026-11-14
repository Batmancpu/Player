package com.batmancpu.player.ui.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.batmancpu.player.R
import com.batmancpu.player.ui.theme.PlayerPalette
import com.batmancpu.player.ui.theme.PlayerSpacing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREFERENCES_NAME = "player_library_preferences"
private const val KEY_TREE_URI = "selected_local_tree_uri"
private const val LOCAL_STORAGE_AUTHORITY = "com.android.externalstorage.documents"

private data class FolderSelection(
    val uri: Uri,
    val displayName: String,
)

@Composable
internal fun FolderSetupScreen(
    onOpenGlassLab: () -> Unit,
) {
    val context = LocalContext.current
    val preferences = remember(context) {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }
    val scope = rememberCoroutineScope()

    var selection by remember { mutableStateOf<FolderSelection?>(null) }
    var isRestoring by remember { mutableStateOf(true) }
    var feedback by remember { mutableStateOf<String?>(null) }
    var discoveredMedia by remember { mutableStateOf<List<DiscoveredLocalMedia>>(emptyList()) }
    var scanError by remember { mutableStateOf<String?>(null) }
    var isScanning by remember { mutableStateOf(false) }
    var scanJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(selection?.uri) {
        scanJob?.cancel()
        scanJob = null
        isScanning = false
        scanError = null
        discoveredMedia = emptyList()
        val current = selection?.uri
        if (current != null) {
            try {
                discoveredMedia = OfflineLibraryIndex.load(context, current)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                scanError = context.getString(R.string.library_scan_error)
            }
        }
    }

    fun startScan(treeUri: Uri) {
        scanJob?.cancel()
        scanError = null
        isScanning = true
        scanJob = scope.launch {
            try {
                val results = SafLocalMediaSource(context, treeUri).scan()
                OfflineLibraryIndex.saveSuccessfulScan(context, treeUri, results)
                discoveredMedia = results
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                scanError = context.getString(R.string.library_scan_error)
            } finally {
                isScanning = false
            }
        }
    }


    LaunchedEffect(preferences) {
        val restored = withContext(Dispatchers.IO) {
            val saved = preferences.getString(KEY_TREE_URI, null) ?: return@withContext null
            val uri = runCatching { Uri.parse(saved) }.getOrNull()
                ?: return@withContext null
            val readGrantExists = context.contentResolver.persistedUriPermissions.any {
                it.uri == uri && it.isReadPermission
            }
            if (!isSupportedLocalTree(uri) || !readGrantExists) {
                null
            } else {
                FolderSelection(uri, resolveFolderDisplayName(context, uri))
            }
        }
        selection = restored
        isRestoring = false
        if (restored == null && preferences.contains(KEY_TREE_URI)) {
            preferences.edit().remove(KEY_TREE_URI).apply()
            feedback = context.getString(R.string.folder_setup_permission_lost)
        }
    }

    val folderPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree(),
    ) { uri ->
        if (uri != null) {
            if (!isSupportedLocalTree(uri)) {
                feedback = context.getString(R.string.folder_setup_unsupported_provider)
            } else {
                val grantSaved = runCatching {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION,
                    )
                    context.contentResolver.persistedUriPermissions.any {
                        it.uri == uri && it.isReadPermission
                    }
                }.getOrDefault(false)

                if (!grantSaved) {
                    feedback = context.getString(R.string.folder_setup_permission_failed)
                } else {
                    val previous = selection?.uri
                    if (previous != null && previous != uri) {
                        runCatching {
                            context.contentResolver.releasePersistableUriPermission(
                                previous,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION,
                            )
                        }
                    }
                    scope.launch {
                        val name = withContext(Dispatchers.IO) {
                            resolveFolderDisplayName(context, uri)
                        }
                        preferences.edit().putString(KEY_TREE_URI, uri.toString()).apply()
                        selection = FolderSelection(uri, name)
                        if (previous != null && previous != uri) {
                            withContext(Dispatchers.IO) {
                                OfflineLibraryIndex.forgetTree(context, previous)
                            }
                        }
                        feedback = context.getString(R.string.folder_setup_access_saved)
                    }
                }
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = PlayerPalette.background,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = PlayerSpacing.screenHorizontal, vertical = 34.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(R.string.folder_setup_kicker),
                color = PlayerPalette.accentMuted,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                text = stringResource(R.string.folder_setup_title),
                color = PlayerPalette.textPrimary,
                style = MaterialTheme.typography.displayMedium,
            )
            Text(
                text = stringResource(R.string.folder_setup_description),
                color = PlayerPalette.textSecondary,
                style = MaterialTheme.typography.bodyLarge,
            )

            when {
                isRestoring -> {
                    Text(
                        text = stringResource(R.string.folder_setup_restoring),
                        color = PlayerPalette.textTertiary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                selection != null -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = PlayerPalette.surface,
                        ),
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Text(
                                text = stringResource(R.string.folder_setup_selected_label),
                                color = PlayerPalette.textTertiary,
                                style = MaterialTheme.typography.labelMedium,
                            )
                            Text(
                                text = selection!!.displayName,
                                color = PlayerPalette.textPrimary,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Text(
                                text = stringResource(R.string.folder_setup_access_saved),
                                color = PlayerPalette.textSecondary,
                                style = MaterialTheme.typography.bodySmall,
                            )
                            Button(
                                onClick = { startScan(selection!!.uri) },
                                enabled = !isScanning,
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.library_scan_start))
                            }
                            if (isScanning) {
                                Text(
                                    text = stringResource(R.string.library_scan_progress),
                                    color = PlayerPalette.textSecondary,
                                )
                                OutlinedButton(
                                    onClick = { scanJob?.cancel() },
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    Text(stringResource(R.string.library_scan_cancel))
                                }
                            }
                            if (!scanError.isNullOrBlank()) {
                                Text(scanError!!, color = PlayerPalette.accentMuted)
                            }
                            if (discoveredMedia.isNotEmpty()) {
                                val videos = discoveredMedia.count { it.kind == LocalMediaKind.VIDEO }
                                val subtitles = discoveredMedia.size - videos
                                Text(
                                    text = stringResource(R.string.library_scan_found, videos, subtitles),
                                    color = PlayerPalette.textPrimary,
                                )
                                val files = discoveredMedia.filter { it.kind == LocalMediaKind.VIDEO }
                                val movies = files.filter { MediaNameParser.parse(it.displayName) !is ParsedMediaName.Episode }
                                val episodes = files.filter { MediaNameParser.parse(it.displayName) is ParsedMediaName.Episode }
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp),
                                ) {
                                    if (movies.isNotEmpty()) {
                                        item { Text(stringResource(R.string.library_movies_heading), color = PlayerPalette.textPrimary) }
                                        items(movies, key = { it.documentId }) { document ->
                                            Text(
                                                text = mediaDisplayLabel(document),
                                                color = PlayerPalette.textSecondary,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                    if (episodes.isNotEmpty()) {
                                        item { Text(stringResource(R.string.library_episodes_heading), color = PlayerPalette.textPrimary) }
                                        items(episodes.sortedBy { it.displayName }, key = { it.documentId }) { document ->
                                            Text(
                                                text = mediaDisplayLabel(document),
                                                color = PlayerPalette.textSecondary,
                                                style = MaterialTheme.typography.bodyMedium,
                                            )
                                        }
                                    }
                                }
                            }
                            OutlinedButton(
                                onClick = { folderPicker.launch(selection!!.uri) },
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Text(stringResource(R.string.folder_setup_change_folder))
                            }
                        }
                    }
                }
                else -> {
                    Text(
                        text = stringResource(R.string.folder_setup_no_folder),
                        color = PlayerPalette.textTertiary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Button(
                        onClick = { folderPicker.launch(null) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(stringResource(R.string.folder_setup_choose_folder))
                    }
                }
            }

            if (!feedback.isNullOrBlank()) {
                Text(
                    text = feedback!!,
                    color = PlayerPalette.accentMuted,
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.folder_setup_privacy),
                color = PlayerPalette.textTertiary,
                style = MaterialTheme.typography.bodySmall,
            )
            TextButton(
                onClick = onOpenGlassLab,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(stringResource(R.string.folder_setup_glass_lab))
            }
        }
    }
}

private fun isSupportedLocalTree(uri: Uri): Boolean =
    DocumentsContract.isTreeUri(uri) && uri.authority == LOCAL_STORAGE_AUTHORITY

private fun resolveFolderDisplayName(context: Context, treeUri: Uri): String {
    return runCatching {
        val documentId = DocumentsContract.getTreeDocumentId(treeUri)
        val documentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
        context.contentResolver.query(
            documentUri,
            arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME),
            null,
            null,
            null,
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getString(0)?.takeIf { it.isNotBlank() }
            } else {
                null
            }
        }
    }.getOrNull() ?: context.getString(R.string.folder_setup_fallback_name)
}

private fun mediaDisplayLabel(document: DiscoveredLocalMedia): String =
    when (val parsed = MediaNameParser.parse(document.displayName)) {
        is ParsedMediaName.Movie -> parsed.title + (parsed.year?.let { " ($it)" } ?: "")
        is ParsedMediaName.Episode -> parsed.series + " · S" +
            parsed.season.toString().padStart(2, '0') + "E" +
            parsed.episode.toString().padStart(2, '0')
        is ParsedMediaName.Unrecognized -> document.displayName
    }
