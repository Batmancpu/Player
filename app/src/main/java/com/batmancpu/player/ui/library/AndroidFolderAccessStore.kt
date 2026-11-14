package com.batmancpu.player.ui.library

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.DocumentsContract
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal class AndroidFolderAccessStore(context: Context) : FolderAccessStore {
    private val app = context.applicationContext
    private val resolver = app.contentResolver
    private val preferences = app.getSharedPreferences("player_library_preferences", Context.MODE_PRIVATE)

    override fun isSupported(tree: String): Boolean = runCatching {
        val uri = Uri.parse(tree)
        uri.scheme == "content" && uri.authority == "com.android.externalstorage.documents" &&
            DocumentsContract.isTreeUri(uri) && DocumentsContract.getTreeDocumentId(uri).isNotBlank()
    }.getOrDefault(false)

    override suspend fun selected(): String? = withContext(Dispatchers.IO) {
        preferences.getString("selected_local_tree_uri", null)
    }

    override suspend fun saveSelected(tree: String) = withContext(Dispatchers.IO) {
        if (!preferences.edit().putString("selected_local_tree_uri", tree).commit()) {
            throw IOException("Local folder selection could not be saved")
        }
    }

    override suspend fun grants(): List<FolderGrant> = withContext(Dispatchers.IO) {
        resolver.persistedUriPermissions.map { FolderGrant(it.uri.toString(), it.isReadPermission, it.isWritePermission) }
    }

    override suspend fun takeRead(tree: String) = withContext(Dispatchers.IO) {
        resolver.takePersistableUriPermission(Uri.parse(tree), Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    override suspend fun release(tree: String, read: Boolean, write: Boolean) = withContext(Dispatchers.IO) {
        val flags = (if (read) Intent.FLAG_GRANT_READ_URI_PERMISSION else 0) or
            (if (write) Intent.FLAG_GRANT_WRITE_URI_PERMISSION else 0)
        resolver.releasePersistableUriPermission(Uri.parse(tree), flags)
    }

    override suspend fun probe(tree: String): String = withContext(Dispatchers.IO) {
        if (!isSupported(tree)) throw UnsupportedFolderException()
        val uri = Uri.parse(tree)
        val root = DocumentsContract.buildDocumentUriUsingTree(uri, DocumentsContract.getTreeDocumentId(uri))
        val columns = arrayOf(DocumentsContract.Document.COLUMN_DISPLAY_NAME, DocumentsContract.Document.COLUMN_MIME_TYPE)
        val cursor = resolver.query(root, columns, null, null, null)
            ?: throw IOException("Local folder provider returned no root cursor")
        cursor.use {
            if (!it.moveToFirst() || it.getString(1) != DocumentsContract.Document.MIME_TYPE_DIR ||
                it.extras.getBoolean(DocumentsContract.EXTRA_LOADING, false) ||
                it.extras.containsKey(DocumentsContract.EXTRA_ERROR)
            ) throw IOException("Local folder is unavailable")
            it.getString(0)?.takeIf(String::isNotBlank) ?: app.getString(com.batmancpu.player.R.string.folder_setup_fallback_name)
        }
    }
}
