package com.batmancpu.player.ui.library

import android.content.Context
import android.net.Uri
import android.provider.DocumentsContract
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android SAF bridge for the one local tree selected through OpenDocumentTree.
 * Only immediate children of that granted tree and its descendants are queried.
 * No media bytes are opened and no write operations are used.
 */
internal class SafLocalMediaSource(
    private val context: Context,
    private val treeUri: Uri,
) {
    init {
        require(
            DocumentsContract.isTreeUri(treeUri) &&
                treeUri.authority == "com.android.externalstorage.documents"
        ) { "Only a local Android external-storage tree is supported" }
    }

    private val rootId = DocumentsContract.getTreeDocumentId(treeUri)
    private val limits = ScanLimits()
    private val resolver get() = context.contentResolver
    private val rootDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootId)

    suspend fun scan(): List<DiscoveredLocalMedia> = withContext(Dispatchers.IO) {
        if (resolver.persistedUriPermissions.none { it.uri == treeUri && it.isReadPermission }) {
            throw SecurityException("Local folder read access was revoked")
        }
        MediaTreeScanner(
            listChildren = ::queryImmediateChildren,
            isWithinSelectedTree = ::isWithinSelectedTree,
            limits = limits,
        ).scan(rootId)
    }

    private suspend fun isWithinSelectedTree(root: String, documentId: String): Boolean =
        withContext(Dispatchers.IO) {
            if (root != rootId) return@withContext false
            if (documentId == rootId) return@withContext true
            val candidate = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
            DocumentsContract.isChildDocument(resolver, rootDocumentUri, candidate)
        }

    private suspend fun queryImmediateChildren(parentId: String): TreeListing =
        withContext(Dispatchers.IO) {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
            )
            val results = mutableListOf<TreeEntry>()
            resolver.readSafCursor(childrenUri, projection) { cursor, checkActive ->
                val it = cursor
                val idColumn = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameColumn = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeColumn = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                while (it.moveToNext()) {
                    checkActive()
                    if (results.size >= limits.childrenPerDirectory) {
                        throw IOException("Local directory entry limit reached")
                    }
                    val documentId = it.getString(idColumn)?.takeIf(String::isNotBlank)
                        ?: throw IOException("Local folder returned an invalid document ID")
                    val name = it.getString(nameColumn)?.takeIf(String::isNotBlank)
                        ?: throw IOException("Local folder returned an invalid document name")
                    val mime = it.getString(mimeColumn)?.takeIf(String::isNotBlank)
                        ?: throw IOException("Local folder returned an invalid document type")
                    results.add(
                        TreeEntry(
                            documentId = documentId,
                            displayName = name,
                            mimeType = mime,
                            isDirectory = mime == DocumentsContract.Document.MIME_TYPE_DIR,
                        ),
                    )
                }
                // Read status after consuming rows, while the cursor remains open.
                // Never expose provider error text: it may contain private paths.
                TreeListing(
                    entries = results,
                    isLoading = it.extras.getBoolean(DocumentsContract.EXTRA_LOADING, false),
                    hasError = it.extras.containsKey(DocumentsContract.EXTRA_ERROR),
                )
            }
        }
}

