package com.mangoloads.player.ui.metadata

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.AtomicFile
import com.mangoloads.player.BuildConfig
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.File
import java.io.IOException
import java.security.KeyStore
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicLong
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/** Deliberately contains no credential, filename or exception details. */
internal data class MetadataSettingsState(
    val enabled: Boolean = false,
    val credentialConfigured: Boolean = false,
    val appCredentialAvailable: Boolean = false,
    val storageError: Boolean = false,
    val tokenRejected: Boolean = false,
)

/**
 * Explicit local consent is atomically encrypted with an Android Keystore
 * AES-GCM key. A publisher credential remains in generated app configuration;
 * the current UI never asks a user to enter one. Legacy user tokens stay
 * encrypted for compatibility and are used only with their prior saved consent.
 * Nothing is written to the media tree.
 * Reading a missing/invalid file or missing key never creates a key or enables
 * metadata. Callers must read credentialIfEnabled again before every request.
 */
internal class MetadataSettingsStore(
    context: Context,
    testNamespace: String? = null,
    appCredential: String = BuildConfig.TMDB_READ_ACCESS_TOKEN,
) {
    private val app = context.applicationContext
    private val suffix = testNamespace?.also {
        require(it.matches(Regex("[A-Za-z0-9_-]{1,48}")))
    }?.let { ".test.$it" }.orEmpty()
    internal val storageFile = File(app.noBackupFilesDir, "metadata-settings$suffix.bin")
    internal val keyAlias = "com.mangoloads.player.metadata$suffix"
    private val file = AtomicFile(storageFile)
    private val gate = gates.getOrPut(keyAlias) { AccessGate() }
    private val publisherCredential = validatedToken(appCredential)

    suspend fun load(): MetadataSettingsState = withContext(Dispatchers.IO) {
        gate.mutex.withLock {
            try {
                state(read())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                gate.denied = true
                failureState()
            }
        }
    }

    /** null retains the existing credential; blank/invalid input is rejected. */
    suspend fun save(token: String?, enabled: Boolean): MetadataSettingsState {
        val validated = token?.let(::validatedToken)
        if (token != null && validated == null) return load().copy(tokenRejected = true)
        val revision = gate.revision.incrementAndGet()
        gate.denied = true
        return withContext(Dispatchers.IO) {
            gate.mutex.withLock {
                if (gate.revision.get() != revision) return@withLock safelyReadState()
                try {
                    currentCoroutineContext().ensureActive()
                    // Only a fresh, explicit token can replace unreadable state.
                    val existing = if (validated == null) read() else null
                    val localCredential = validated ?: existing?.token
                    if (localCredential == null && publisherCredential == null) {
                        return@withLock MetadataSettingsState(tokenRejected = enabled)
                    }
                    val payload = Payload(enabled, localCredential)
                    write(payload)
                    if (gate.revision.get() == revision) gate.denied = !enabled
                    state(payload)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    gate.denied = true
                    // If persisting revocation fails, invalidate the credential
                    // rather than leaving an older enabled blob usable after restart.
                    if (!enabled) runCatching { keyStore().deleteEntry(keyAlias) }
                    failureState()
                }
            }
        }
    }

    suspend fun disable(): MetadataSettingsState = withContext(NonCancellable) {
        save(token = null, enabled = false)
    }

    suspend fun clear(): MetadataSettingsState {
        val revision = gate.revision.incrementAndGet()
        gate.denied = true
        return withContext(NonCancellable + Dispatchers.IO) {
            gate.mutex.withLock {
                if (gate.revision.get() != revision) return@withLock safelyReadState()
                try {
                    currentCoroutineContext().ensureActive()
                    file.delete()
                    if (storageFile.exists() || File(storageFile.path + ".bak").exists() ||
                        File(storageFile.path + ".new").exists()) {
                        throw IOException("Credential removal failed")
                    }
                    keyStore().deleteEntry(keyAlias)
                    state(null)
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    // Also revoke the key if removing the ciphertext failed.
                    runCatching { keyStore().deleteEntry(keyAlias) }
                    MetadataSettingsState(storageError = true, appCredentialAvailable = publisherCredential != null,
                        credentialConfigured = publisherCredential != null)
                }
            }
        }
    }

    suspend fun credentialIfEnabled(): String? {
        if (gate.denied) return null
        return withContext(Dispatchers.IO) {
            gate.mutex.withLock {
                if (gate.denied) return@withLock null
                try {
                    val payload = read()
                    payload?.takeIf { it.enabled && !gate.denied }?.let {
                        publisherCredential ?: it.token
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (_: Exception) {
                    gate.denied = true
                    null
                }
            }
        }
    }

    private fun state(payload: Payload?): MetadataSettingsState = MetadataSettingsState(
        enabled = payload?.enabled == true && (publisherCredential != null || payload.token != null) && !gate.denied,
        credentialConfigured = payload?.token != null || publisherCredential != null,
        appCredentialAvailable = publisherCredential != null,
    )

    private fun safelyReadState(): MetadataSettingsState = try {
        state(read())
    } catch (_: Exception) {
        gate.denied = true
        failureState()
    }

    private fun failureState() = MetadataSettingsState(
        storageError = true,
        appCredentialAvailable = publisherCredential != null,
        credentialConfigured = publisherCredential != null,
    )

    private fun read(): Payload? {
        val bytes = try {
            file.openRead().use { input ->
                val buffer = ByteArray(MAX_FILE_BYTES + 1)
                var count = 0
                while (count < buffer.size) {
                    val read = input.read(buffer, count, buffer.size - count)
                    if (read < 0) break
                    count += read
                }
                require(count <= MAX_FILE_BYTES)
                buffer.copyOf(count)
            }
        } catch (missing: java.io.FileNotFoundException) {
            if (!storageFile.exists() && !File(storageFile.path + ".bak").exists()) return null
            throw missing
        }
        require(bytes.size >= MAGIC.size + 1 + IV_BYTES + TAG_BYTES)
        val encrypted = DataInputStream(ByteArrayInputStream(bytes))
        val magic = ByteArray(MAGIC.size).also(encrypted::readFully)
        val version = encrypted.readUnsignedByte()
        require(magic.contentEquals(MAGIC) && version in LEGACY_FORMAT_VERSION..FORMAT_VERSION)
        val iv = ByteArray(IV_BYTES).also(encrypted::readFully)
        val ciphertext = ByteArray(encrypted.available()).also(encrypted::readFully)
        val key = (keyStore().getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: throw IOException("Credential key unavailable")
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BYTES * 8, iv))
        cipher.updateAAD(associatedData(version))
        val plaintext = cipher.doFinal(ciphertext)
        return try {
            DataInputStream(ByteArrayInputStream(plaintext)).use { data ->
                require(data.readUnsignedByte() == version)
                val enabled = data.readUnsignedByte().also { require(it in 0..1) } == 1
                val length = data.readUnsignedShort().also {
                    require(it in (if (version == LEGACY_FORMAT_VERSION) 1..MAX_TOKEN_CHARS else 0..MAX_TOKEN_CHARS))
                }
                require(data.available() == length)
                val token = if (length == 0) null else {
                    val tokenBytes = ByteArray(length).also(data::readFully)
                    val parsed = tokenBytes.toString(Charsets.US_ASCII)
                    tokenBytes.fill(0)
                    require(validatedToken(parsed) == parsed)
                    parsed
                }
                Payload(enabled, token)
            }
        } finally {
            plaintext.fill(0)
        }
    }

    private fun write(payload: Payload) {
        val key = (keyStore().getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry)?.secretKey
            ?: createKey()
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        cipher.updateAAD(associatedData(FORMAT_VERSION))
        val iv = cipher.iv.also { require(it.size == IV_BYTES) }
        val output = ByteArrayOutputStream()
        DataOutputStream(output).use { data ->
            val tokenBytes = payload.token?.toByteArray(Charsets.US_ASCII) ?: byteArrayOf()
            data.writeByte(FORMAT_VERSION)
            data.writeByte(if (payload.enabled) 1 else 0)
            data.writeShort(tokenBytes.size)
            data.write(tokenBytes)
            tokenBytes.fill(0)
        }
        val plaintext = output.toByteArray()
        val ciphertext = try {
            cipher.doFinal(plaintext)
        } finally {
            plaintext.fill(0)
        }
        val stream = file.startWrite()
        try {
            stream.write(MAGIC)
            stream.write(FORMAT_VERSION)
            stream.write(iv)
            stream.write(ciphertext)
            file.finishWrite(stream)
        } catch (failure: Exception) {
            file.failWrite(stream)
            throw failure
        }
    }

    private fun keyStore(): KeyStore = KeyStore.getInstance(KEYSTORE).apply { load(null) }

    private fun createKey(): SecretKey = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, KEYSTORE).apply {
        init(KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setRandomizedEncryptionRequired(true)
            .build())
    }.generateKey()

    private fun associatedData(version: Int): ByteArray = "${app.packageName}:$keyAlias:$version".toByteArray(Charsets.UTF_8)

    // A regular class avoids an automatically generated toString containing the secret.
    private class Payload(val enabled: Boolean, val token: String?)
    private class AccessGate {
        val mutex = Mutex()
        val revision = AtomicLong()
        @Volatile var denied = false
    }

    companion object {
        private const val KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val LEGACY_FORMAT_VERSION = 1
        private const val FORMAT_VERSION = 2
        private const val IV_BYTES = 12
        private const val TAG_BYTES = 16
        private const val MAX_TOKEN_CHARS = 2048
        private const val MAX_FILE_BYTES = 4096
        private val MAGIC = byteArrayOf(0x50, 0x4d, 0x45, 0x54)
        private val gates = ConcurrentHashMap<String, AccessGate>()
        private val bearerToken = Regex("[A-Za-z0-9._~+/\\-]+=*")

        internal fun validatedToken(raw: String): String? {
            if (raw.length > MAX_TOKEN_CHARS + 64 || raw.any { Character.isISOControl(it.code) }) return null
            return raw.trim(' ').takeIf { it.length in 1..MAX_TOKEN_CHARS && bearerToken.matches(it) }
        }
    }
}
