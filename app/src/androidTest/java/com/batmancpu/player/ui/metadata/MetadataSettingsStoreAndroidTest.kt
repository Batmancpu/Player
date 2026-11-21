package com.batmancpu.player.ui.metadata

import androidx.test.platform.app.InstrumentationRegistry
import java.security.KeyStore
import java.util.UUID
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

/** Uses generated, non-authentic fixture values only. Never logs a credential. */
class MetadataSettingsStoreAndroidTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    private fun isolated(test: suspend (MetadataSettingsStore, String) -> Unit) = runBlocking {
        val namespace = UUID.randomUUID().toString()
        val store = MetadataSettingsStore(context, namespace)
        try {
            test(store, namespace)
        } finally {
            store.clear()
        }
    }

    @Test fun freshSettingsDoNotEnableMetadataOrCreateKey() = isolated { store, _ ->
        assertEquals(MetadataSettingsState(), store.load())
        assertNull(store.credentialIfEnabled())
        assertFalse(store.storageFile.exists())
        assertFalse(keys().containsAlias(store.keyAlias))
        val missing = store.save(token = null, enabled = true)
        assertFalse(missing.enabled)
        assertTrue(missing.tokenRejected)
        assertNull(store.credentialIfEnabled())
        assertFalse(keys().containsAlias(store.keyAlias))
    }

    @Test fun explicitConsentAndEncryptedCredentialSurviveReopen() = isolated { store, namespace ->
        assertEquals(MetadataSettingsState(credentialConfigured = true), store.save(TOKEN, enabled = false))
        assertNull(store.credentialIfEnabled())
        assertTrue(store.storageFile.canonicalPath.startsWith(context.noBackupFilesDir.canonicalPath + "/"))
        assertFalse("Private ciphertext must not contain the token", store.storageFile.readBytes()
            .toString(Charsets.ISO_8859_1).contains(TOKEN))
        val reopened = MetadataSettingsStore(context, namespace)
        assertTrue(reopened.load().credentialConfigured)
        assertFalse(reopened.load().enabled)
        assertNull(reopened.credentialIfEnabled())
        assertEquals(MetadataSettingsState(enabled = true, credentialConfigured = true), reopened.save(null, enabled = true))
        assertTrue("The authorized credential must be available only after opt-in", TOKEN == reopened.credentialIfEnabled())
        assertEquals(MetadataSettingsState(enabled = true, credentialConfigured = true),
            MetadataSettingsStore(context, namespace).load())
        assertFalse(store.load().toString().contains(TOKEN))
    }

    @Test fun disableAndClearRevokeAccessAcrossInstances() = isolated { store, namespace ->
        assertTrue(store.save(TOKEN, enabled = true).enabled)
        val other = MetadataSettingsStore(context, namespace)
        assertTrue(other.credentialIfEnabled() != null)
        val disabled = store.disable()
        assertFalse(disabled.enabled)
        assertTrue(disabled.credentialConfigured)
        assertNull(other.credentialIfEnabled())
        assertFalse(other.load().enabled)
        assertTrue(other.save(null, enabled = true).enabled)
        assertEquals(MetadataSettingsState(), store.clear())
        assertNull(other.credentialIfEnabled())
        assertFalse(store.storageFile.exists())
        assertFalse(keys().containsAlias(store.keyAlias))
        assertEquals(MetadataSettingsState(), MetadataSettingsStore(context, namespace).load())
    }

    @Test fun ciphertextTamperAndKeyLossFailClosedWithoutRecreatingKeys() = isolated { store, namespace ->
        assertTrue(store.save(TOKEN, enabled = true).enabled)
        val bytes = store.storageFile.readBytes()
        bytes[bytes.lastIndex] = (bytes.last().toInt() xor 1).toByte()
        store.storageFile.writeBytes(bytes)
        val tampered = MetadataSettingsStore(context, namespace)
        assertTrue(tampered.load().storageError)
        assertFalse(tampered.load().enabled)
        assertNull(tampered.credentialIfEnabled())
        assertTrue(store.save(TOKEN, enabled = true).enabled)
        keys().deleteEntry(store.keyAlias)
        val lostKey = MetadataSettingsStore(context, namespace)
        assertTrue(lostKey.load().storageError)
        assertNull(lostKey.credentialIfEnabled())
        assertFalse("A read must never silently generate a replacement key", keys().containsAlias(store.keyAlias))
        assertTrue(store.save(TOKEN, enabled = true).enabled)
        assertTrue(store.load().credentialConfigured)
    }

    @Test fun malformedAndOversizedCiphertextCannotBecomeConsent() = isolated { store, _ ->
        for (bytes in listOf(byteArrayOf(1, 2, 3), ByteArray(8192))) {
            store.storageFile.writeBytes(bytes)
            val state = store.load()
            assertTrue(state.storageError)
            assertFalse(state.enabled)
            assertNull(store.credentialIfEnabled())
            assertFalse(keys().containsAlias(store.keyAlias))
        }
    }

    @Test fun invalidTokenDoesNotReplaceThePreviouslySavedCredential() = isolated { store, _ ->
        assertTrue(store.save(TOKEN, enabled = true).enabled)
        val original = store.storageFile.readBytes()
        val invalid = listOf("", "   ", "Bearer fixture", "a\nb", "a\rb", "a\tb", "unicodé", "a:b", "a=b=c", "a".repeat(2049))
        for (value in invalid) {
            val state = store.save(value, enabled = true)
            assertTrue("Input validation must reject unsafe Bearer values", state.tokenRejected)
            assertTrue(state.enabled)
            assertArrayEquals(original, store.storageFile.readBytes())
            assertTrue("Rejected input must preserve the earlier authorized credential", TOKEN == store.credentialIfEnabled())
        }
        assertTrue(store.save("  $TOKEN  ", enabled = true).enabled)
        assertTrue(TOKEN == store.credentialIfEnabled())
    }

    @Test fun repeatedEncryptionUsesFreshNonces() = isolated { store, _ ->
        store.save(TOKEN, enabled = true)
        val first = store.storageFile.readBytes()
        store.save(TOKEN, enabled = true)
        val second = store.storageFile.readBytes()
        assertFalse("AES-GCM writes must never reuse a nonce", first.copyOfRange(5, 17).contentEquals(second.copyOfRange(5, 17)))
        assertFalse(first.contentEquals(second))
        assertTrue(TOKEN == store.credentialIfEnabled())
    }

    @Test fun pendingOlderEnableCannotUndoLaterRevocation() = isolated { store, _ ->
        store.save(TOKEN, enabled = true)
        coroutineScope {
            val pending = async(start = CoroutineStart.UNDISPATCHED) { store.save(TOKEN, enabled = true) }
            store.disable()
            pending.await()
        }
        assertFalse(store.load().enabled)
        assertNull(store.credentialIfEnabled())
    }

    private fun keys(): KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    companion object {
        private const val TOKEN = "player.synthetic-fixture.not-a-real-api-token_2026"
    }
}
