package com.mangoloads.player.ui.metadata

import com.mangoloads.player.ui.library.MetadataSearchConsent
import com.mangoloads.player.ui.library.TmdbMediaType
import com.mangoloads.player.ui.library.TmdbSearchTerms
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URI
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.FutureTask
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/** Provider data only. No source document, local path, credential or episode identity. */
internal data class TmdbCandidate(
    val id: Long,
    val mediaType: TmdbMediaType,
    val title: String,
    val year: Int?,
    val overview: String,
    val posterPath: String?,
    val backdropPath: String?,
)

internal sealed interface TmdbResult<out T> {
    data class Success<T>(val value: T) : TmdbResult<T>
    data object Disabled : TmdbResult<Nothing>
    data object Unauthorized : TmdbResult<Nothing>
    data object RateLimited : TmdbResult<Nothing>
    data object Unavailable : TmdbResult<Nothing>
    data object InvalidResponse : TmdbResult<Nothing>
}

internal data class TmdbHttpResponse(val statusCode: Int, val body: ByteArray)

/** A credential is scoped to this call, never retained in a request value or logged. */
internal fun interface TmdbTransport {
    suspend fun get(uri: URI, credential: String): TmdbHttpResponse
}

/** Explicit calls only: no polling, automatic scans, background retry or connectivity listener. */
internal class TmdbClient(
    private val credential: suspend () -> String?,
    private val transport: TmdbTransport = HttpsTmdbTransport(),
) {
    constructor(settings: MetadataSettingsStore) : this(settings::credentialIfEnabled)

    suspend fun search(terms: TmdbSearchTerms): TmdbResult<List<TmdbCandidate>> {
        val uri = TmdbRequestPolicy.search(terms, MetadataSearchConsent.ENABLED_BY_USER)
            ?: return TmdbResult.Disabled
        return request(uri) { body ->
            val results = JSONObject(body).getJSONArray("results")
            buildList {
                // One bounded page; no hidden follow-up requests or broad directory queries.
                for (index in 0 until minOf(results.length(), 20)) {
                    parseCandidate(results.optJSONObject(index), terms.mediaType)?.let(::add)
                }
            }
        }
    }

    suspend fun details(id: Long, mediaType: TmdbMediaType): TmdbResult<TmdbCandidate> {
        val uri = TmdbRequestPolicy.details(id, mediaType, MetadataSearchConsent.ENABLED_BY_USER)
            ?: return TmdbResult.InvalidResponse
        return request(uri) { body ->
            parseCandidate(JSONObject(body), mediaType)?.takeIf { it.id == id }
                ?: throw InvalidTmdbResponse()
        }
    }

    private suspend fun <T> request(uri: URI, parse: (String) -> T): TmdbResult<T> {
        val token = credential() ?: return TmdbResult.Disabled
        return try {
            currentCoroutineContext().ensureActive()
            val response = transport.get(uri, token)
            currentCoroutineContext().ensureActive()
            // Discard an in-flight result if permission was revoked or the token replaced.
            if (credential() != token) return TmdbResult.Disabled
            val result = when (response.statusCode) {
                200 -> {
                    if (response.body.size > MAX_JSON_BYTES) return TmdbResult.InvalidResponse
                    TmdbResult.Success(parse(response.body.toString(Charsets.UTF_8)))
                }
                401, 403 -> TmdbResult.Unauthorized
                429 -> TmdbResult.RateLimited
                in 300..399 -> TmdbResult.InvalidResponse // Redirects are never followed.
                else -> TmdbResult.Unavailable
            }
            currentCoroutineContext().ensureActive()
            if (credential() != token) TmdbResult.Disabled else result
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: InvalidTmdbResponse) {
            TmdbResult.InvalidResponse
        } catch (_: org.json.JSONException) {
            TmdbResult.InvalidResponse
        } catch (_: Exception) {
            // No server error body, URL, token or exception message enters UI/state/logs.
            TmdbResult.Unavailable
        }
    }

    private fun parseCandidate(value: JSONObject?, type: TmdbMediaType): TmdbCandidate? {
        value ?: return null
        val id = value.optLong("id", -1).takeIf { it > 0 } ?: return null
        val title = value.optString(if (type == TmdbMediaType.MOVIE) "title" else "name", "")
            .trim().takeIf { it.isNotEmpty() && it.length <= 512 && it.none(Char::isISOControl) }
            ?: return null
        val date = value.optString(if (type == TmdbMediaType.MOVIE) "release_date" else "first_air_date", "")
        val year = date.take(4).toIntOrNull()?.takeIf { it in 1000..9999 }
        fun image(key: String): String? = value.optString(key, "").takeIf {
            TmdbRequestPolicy.artwork(it, TmdbImageSize.POSTER, MetadataSearchConsent.ENABLED_BY_USER) != null
        }
        return TmdbCandidate(
            id, type, title, year, value.optString("overview", "").take(4_000),
            image("poster_path"), image("backdrop_path"),
        )
    }

    companion object {
        const val MAX_JSON_BYTES = 1_048_576
    }
}

private class InvalidTmdbResponse : IOException()

/**
 * A small bounded pool permits disconnect() to run immediately when the caller
 * cancels a blocking platform read. Workers time out when idle, and no work is
 * scheduled until an explicit client call passes the consent/credential gate.
 */
internal class HttpsTmdbTransport(
    private val connectionFactory: (URI) -> HttpsURLConnection = { it.toURL().openConnection() as HttpsURLConnection },
) : TmdbTransport {
    override suspend fun get(uri: URI, credential: String): TmdbHttpResponse {
        require(isApprovedApiUri(uri))
        require(credential.length in 1..2_048 && BEARER_TOKEN.matches(credential))
        return suspendCancellableCoroutine { continuation ->
            val connection = AtomicReference<HttpsURLConnection?>()
            val task = FutureTask<Unit> {
                try {
                    if (!continuation.isActive) return@FutureTask
                    val active = connectionFactory(uri)
                    connection.set(active)
                    if (!continuation.isActive) return@FutureTask
                    active.instanceFollowRedirects = false
                    active.connectTimeout = 10_000
                    active.readTimeout = 15_000
                    active.useCaches = false
                    active.requestMethod = "GET"
                    active.setRequestProperty("Accept", "application/json")
                    active.setRequestProperty("User-Agent", "Player")
                    active.setRequestProperty("Authorization", "Bearer $credential")
                    val status = active.responseCode
                    val body = if (status == 200) {
                        if (active.contentLengthLong > TmdbClient.MAX_JSON_BYTES) throw InvalidTmdbResponse()
                        val mime = active.contentType?.substringBefore(';')?.trim()?.lowercase()
                        if (mime != "application/json") throw InvalidTmdbResponse()
                        active.inputStream.use { input ->
                            val output = ByteArrayOutputStream()
                            val buffer = ByteArray(8_192)
                            while (true) {
                                if (!continuation.isActive || Thread.currentThread().isInterrupted) throw CancellationException()
                                val count = input.read(buffer)
                                if (count < 0) break
                                if (output.size() + count > TmdbClient.MAX_JSON_BYTES) throw InvalidTmdbResponse()
                                output.write(buffer, 0, count)
                            }
                            output.toByteArray()
                        }
                    } else ByteArray(0) // Never retain or expose server error responses.
                    if (continuation.isActive) continuation.resume(TmdbHttpResponse(status, body))
                } catch (failure: Exception) {
                    if (continuation.isActive) continuation.resumeWithException(failure)
                } finally {
                    connection.getAndSet(null)?.disconnect()
                }
            }
            continuation.invokeOnCancellation {
                connection.getAndSet(null)?.disconnect()
                task.cancel(true)
            }
            try {
                workers.execute(task)
            } catch (failure: Exception) {
                if (continuation.isActive) continuation.resumeWithException(failure)
            }
        }
    }

    private fun isApprovedApiUri(uri: URI): Boolean =
        uri.scheme == "https" && uri.host == "api.themoviedb.org" && uri.port == -1 &&
            uri.rawUserInfo == null && uri.rawFragment == null &&
            API_PATH.matches(uri.rawPath) && !uri.toASCIIString().contains('\\')

    companion object {
        private val API_PATH = Regex("/3/(?:search/(?:movie|tv)|(?:movie|tv)/[1-9][0-9]*)")
        private val BEARER_TOKEN = Regex("[A-Za-z0-9._~+/-]+=*")
        private val workers = ThreadPoolExecutor(
            2, 2, 30, TimeUnit.SECONDS, ArrayBlockingQueue(16),
            { task -> Thread(task, "Player-metadata").apply { isDaemon = true } },
        ).apply { allowCoreThreadTimeOut(true) }
    }
}
