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
    private val resolver get() = context.contentResolver
    private val rootDocumentUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, rootId)

    suspend fun scan(): List<DiscoveredLocalMedia> {
        check(resolver.persistedUriPermissions.any { it.uri == treeUri && it.isReadPermission }) {
            "Local folder read access was revoked"
        }
        return MediaTreeScanner(
            listChildren = ::queryImmediateChildren,
            isWithinSelectedTree = ::isWithinSelectedTree,
        ).scan(rootId)
    }

    private suspend fun isWithinSelectedTree(root: String, documentId: String): Boolean =
        withContext(Dispatchers.IO) {
            if (root != rootId) return@withContext false
            if (documentId == rootId) return@withContext true
            val candidate = DocumentsContract.buildDocumentUriUsingTree(treeUri, documentId)
            DocumentsContract.isChildDocument(resolver, rootDocumentUri, candidate)
        }

    private suspend fun queryImmediateChildren(parentId: String): List<TreeEntry> =
        withContext(Dispatchers.IO) {
            val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, parentId)
            val projection = arrayOf(
                DocumentsContract.Document.COLUMN_DOCUMENT_ID,
                DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                DocumentsContract.Document.COLUMN_MIME_TYPE,
            )
            val results = mutableListOf<TreeEntry>()
            val cursor = resolver.query(childrenUri, projection, null, null, null)
                ?: throw IOException("Local folder provider returned no results cursor")
            cursor.use {
                val idColumn = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DOCUMENT_ID)
                val nameColumn = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_DISPLAY_NAME)
                val mimeColumn = it.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_MIME_TYPE)
                while (it.moveToNext()) {
                    val documentId = it.getString(idColumn) ?: continue
                    val name = it.getString(nameColumn) ?: continue
                    val mime = it.getString(mimeColumn)
                    results.add(
                        TreeEntry(
                            documentId = documentId,
                            displayName = name,
                            mimeType = mime,
                            isDirectory = mime == DocumentsContract.Document.MIME_TYPE_DIR,
                        ),
                    )
                }
            }
            results
        }
}
