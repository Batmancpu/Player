package com.batmancpu.player.ui.library

import android.content.ContentResolver
import android.database.Cursor
import android.net.Uri
import android.os.CancellationSignal
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext

/** Cursor ownership stays inside this function, including cancellation/error paths. */
internal suspend fun <T> ContentResolver.readSafCursor(
    uri: Uri,
    projection: Array<String>,
    read: (Cursor, checkActive: () -> Unit) -> T,
): T = withContext(Dispatchers.IO) {
    val jobContext = currentCoroutineContext()
    suspendCancellableCoroutine { continuation ->
        val signal = CancellationSignal()
        continuation.invokeOnCancellation { runCatching { signal.cancel() } }
        try {
            jobContext.ensureActive()
            val cursor = query(uri, projection, null, null, null, signal)
                ?: throw IOException("Local provider returned no cursor")
            val result = cursor.use { read(it) { jobContext.ensureActive() } }
            jobContext.ensureActive()
            continuation.resumeWith(Result.success(result))
        } catch (failure: Exception) {
            continuation.resumeWith(Result.failure(failure))
        }
    }
}
