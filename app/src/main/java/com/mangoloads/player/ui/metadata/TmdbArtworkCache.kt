package com.mangoloads.player.ui.metadata

import android.content.Context
import android.graphics.BitmapFactory
import android.util.AtomicFile
import com.mangoloads.player.ui.library.MetadataSearchConsent
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.security.MessageDigest
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.FutureTask
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** No credential parameter exists on the artwork transport. */
internal fun interface TmdbArtworkTransport {
    suspend fun fetch(uri: URI): TmdbArtworkResponse?
}

internal class TmdbArtworkResponse(val status: Int, val body: ByteArray, val contentType: String?)

/** Fixed, unauthenticated TMDB CDN transport; never follows redirects. */
@OptIn(kotlinx.coroutines.InternalCoroutinesApi::class)
internal class HttpsTmdbArtworkTransport(
    private val connectionFactory: (URI) -> HttpsURLConnection = {
        it.toURL().openConnection() as HttpsURLConnection
    },
) : TmdbArtworkTransport {
    override suspend fun fetch(uri: URI): TmdbArtworkResponse? {
        if (!approved(uri)) return null
        return suspendCancellableCoroutine { continuation ->
            val active = AtomicReference<HttpsURLConnection?>()
            val task = FutureTask<Unit> {
                var connection: HttpsURLConnection? = null
                val result = try {
                    if (!continuation.isActive) return@FutureTask
                    connection = connectionFactory(uri)
                    active.set(connection)
                    if (!continuation.isActive) return@FutureTask
                    connection.instanceFollowRedirects = false
                    connection.useCaches = false
                    connection.connectTimeout = 10_000
                    connection.readTimeout = 15_000
                    connection.requestMethod = "GET"
                    connection.setRequestProperty("User-Agent", "brrPlayer")
                    connection.setRequestProperty("Accept", "image/jpeg,image/png")
                    val status = connection.responseCode
                    if (status != 200) {
                        TmdbArtworkResponse(status, byteArrayOf(), null)
                    } else if (connection.contentLengthLong > MAX_ARTWORK_BYTES) {
                        null
                    } else {
                        val bytes = connection.inputStream.use { input ->
                            val output = ByteArrayOutputStream()
                            val buffer = ByteArray(8192)
                            while (true) {
                                if (!continuation.isActive) return@FutureTask
                                val count = input.read(buffer)
                                if (count < 0) break
                                if (output.size() + count > MAX_ARTWORK_BYTES) throw java.io.IOException("Artwork response exceeds limit")
                                output.write(buffer, 0, count)
                            }
                            output.toByteArray()
                        }
                        TmdbArtworkResponse(status, bytes, connection.getHeaderField("Content-Type"))
                    }
                } catch (_: Exception) {
                    null
                } finally {
                    active.set(null)
                    connection?.disconnect()
                }
                continuation.tryResume(result)?.let(continuation::completeResume)
            }
            fun cancel() {
                task.cancel(true)
                active.getAndSet(null)?.disconnect()
            }
            continuation.invokeOnCancellation { cancel() }
            try {
                workers.execute(task)
            } catch (_: java.util.concurrent.RejectedExecutionException) {
                continuation.tryResume(null)?.let(continuation::completeResume)
            }

            fun unused() = Unit
        }
    }

    companion object {
        private val imageRoute = Regex("/t/p/(?:w342|w780)/[A-Za-z0-9_-]+\\.(?:jpg|png)")
        private val workers = ThreadPoolExecutor(
            0, 2, 30, TimeUnit.SECONDS, ArrayBlockingQueue(8),
            { task -> Thread(task, "PlayerArtwork").apply { isDaemon = true } },
        )

        private fun approved(uri: URI): Boolean = uri.scheme == "https" &&
            uri.host == "image.tmdb.org" && uri.port == -1 && uri.userInfo == null &&
            uri.rawQuery == null && uri.rawFragment == null && imageRoute.matches(uri.rawPath.orEmpty())
    }
}

private const val MAX_ARTWORK_BYTES = 2 * 1024 * 1024
private const val MAX_CACHE_BYTES = 96L * 1024 * 1024

/**
 * App-private bounded artwork. Network work is explicit and consent gated;
 * cached artwork stays readable while offline or after metadata is disabled.
 */
internal class TmdbArtworkCache(
    context: Context,
    private val settings: MetadataSettingsStore,
    private val transport: TmdbArtworkTransport = HttpsTmdbArtworkTransport(),
    testNamespace: String? = null,
) {
    private val suffix = testNamespace?.also {
        require(it.matches(Regex("[A-Za-z0-9_-]{1,48}")))
    }?.let { ".test.$it" }.orEmpty()
    internal val directory = File(context.applicationContext.noBackupFilesDir, "tmdb-artwork$suffix")
    private val gate = gates.getOrPut(directory.absolutePath) { CacheGate() }

    suspend fun cached(path: String, size: TmdbImageSize): File? = withContext(Dispatchers.IO) {
        val uri = destination(path, size) ?: return@withContext null
        gate.disk.withLock { readCached(uri) }
    }

    suspend fun fetch(path: String, size: TmdbImageSize): File? = withContext(Dispatchers.IO) {
        val uri = destination(path, size) ?: return@withContext null
        gate.download.withLock {
            val credential = settings.credentialIfEnabled() ?: return@withLock null
            gate.disk.withLock { readCached(uri) }?.let { return@withLock it }
            currentCoroutineContext().ensureActive()
            val response = try {
                transport.fetch(uri)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                null
            } ?: return@withLock null
            currentCoroutineContext().ensureActive()
            // Drop an in-flight response after disabling metadata or replacing its token.
            if (settings.credentialIfEnabled() != credential) return@withLock null
            if (response.status != 200 || !validImage(response)) return@withLock null
            gate.disk.withLock {
                currentCoroutineContext().ensureActive()
                if (settings.credentialIfEnabled() != credential) return@withLock null
                if (!directory.isDirectory && !directory.mkdirs()) return@withLock null
                val target = target(uri)
                try { trim(response.body.size.toLong(), target) } catch (_: Exception) { return@withLock null }
                val file = AtomicFile(target)
                val output = try { file.startWrite() } catch (_: Exception) { return@withLock null }
                try {
                    output.write(response.body)
                    currentCoroutineContext().ensureActive()
                    if (settings.credentialIfEnabled() != credential) {
                        file.failWrite(output)
                        return@withLock null
                    }
                    file.finishWrite(output)
                    target.setLastModified(System.currentTimeMillis())
                    target
                } catch (cancelled: CancellationException) {
                    file.failWrite(output)
                    throw cancelled
                } catch (_: Exception) {
                    file.failWrite(output)
                    null
                }
            }
        }
    }

    private fun destination(path: String, size: TmdbImageSize): URI? = TmdbRequestPolicy.artwork(
        path, size, MetadataSearchConsent.ENABLED_BY_USER,
    )

    private fun target(uri: URI): File {
        val hash = MessageDigest.getInstance("SHA-256").digest(uri.toASCIIString().toByteArray(Charsets.UTF_8))
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        return File(directory, "$hash.img")
    }

    private fun readCached(uri: URI): File? {
        val file = target(uri)
        if (!file.isFile || file.length() !in 1..MAX_ARTWORK_BYTES.toLong()) return null
        val valid = runCatching {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.absolutePath, options)
            validBounds(options)
        }.getOrDefault(false)
        if (!valid) { file.delete(); return null }
        file.setLastModified(System.currentTimeMillis())
        return file
    }

    private fun validImage(response: TmdbArtworkResponse): Boolean {
        if (response.body.size !in 1..MAX_ARTWORK_BYTES) return false
        val type = response.contentType?.substringBefore(';')?.trim()?.lowercase()
        if (type !in setOf("image/jpeg", "image/png")) return false
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(response.body, 0, response.body.size, options)
        return validBounds(options) && options.outMimeType == type
    }

    private fun validBounds(options: BitmapFactory.Options): Boolean =
        options.outWidth in 1..2048 && options.outHeight in 1..2048 &&
            options.outMimeType in setOf("image/jpeg", "image/png")

    private fun trim(incoming: Long, replacing: File) {
        val files = directory.listFiles()?.filter { it.isFile && it.name.matches(Regex("[0-9a-f]{64}\\.img")) }.orEmpty()
        var total = files.filter { it != replacing }.sumOf { it.length() }
        for (file in files.filter { it != replacing }.sortedBy { it.lastModified() }) {
            if (total + incoming <= MAX_CACHE_BYTES) break
            val length = file.length()
            if (!file.delete()) throw java.io.IOException("Artwork cache is full")
            total -= length
        }
        if (total + incoming > MAX_CACHE_BYTES) throw java.io.IOException("Artwork cache is full")
    }

    private class CacheGate { val disk = Mutex(); val download = Mutex() }
    companion object { private val gates = ConcurrentHashMap<String, CacheGate>() }
}
