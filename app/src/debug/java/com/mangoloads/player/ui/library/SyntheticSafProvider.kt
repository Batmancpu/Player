package com.mangoloads.player.ui.library

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import android.os.CancellationSignal
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/** Synthetic debug-only provider; absent from signed releases, no source media access. */
class SyntheticSafProvider : ContentProvider() {
    companion object {
        @Volatile var playbackFile: File? = null
        @Volatile var subtitleContents: ByteArray? = null
        @Volatile var blocked = false
        @Volatile var cursorClosed = false
        @Volatile var entered = CountDownLatch(1)
        @Volatile var cancelled = CountDownLatch(1)
        fun reset() {
            playbackFile = null; subtitleContents = null; blocked = false; cursorClosed = false
            entered = CountDownLatch(1); cancelled = CountDownLatch(1)
        }
    }
    override fun onCreate() = true
    override fun getType(uri: Uri) = "vnd.android.document/directory"
    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor =
        query(uri, projection, selection, selectionArgs, sortOrder, null)

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?, cancellationSignal: CancellationSignal?): Cursor {
        if (blocked) {
            val latch = CountDownLatch(1)
            cancellationSignal?.setOnCancelListener { cancelled.countDown(); latch.countDown() }
            entered.countDown()
            check(latch.await(5, TimeUnit.SECONDS)) { "Synthetic query timed out" }
            cancellationSignal?.throwIfCanceled()
        }
        return object : MatrixCursor(arrayOf("name")) {
            override fun close() { cursorClosed = true; super.close() }
        }.apply { addRow(arrayOf("Synthetic")) }
    }
    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        require(mode == "r")
        return when (uri.path) {
            "/video" -> ParcelFileDescriptor.open(checkNotNull(playbackFile), ParcelFileDescriptor.MODE_READ_ONLY)
            "/subtitle" -> {
                val bytes = checkNotNull(subtitleContents)
                val pipe = ParcelFileDescriptor.createPipe()
                ParcelFileDescriptor.AutoCloseOutputStream(pipe[1]).use { it.write(bytes) }
                pipe[0]
            }
            else -> error("Synthetic provider only serves owned test fixtures")
        }
    }
    override fun insert(uri: Uri, values: ContentValues?): Uri? = error("Read only")
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = error("Read only")
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = error("Read only")
}
