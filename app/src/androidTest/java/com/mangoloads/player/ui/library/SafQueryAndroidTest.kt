package com.mangoloads.player.ui.library

import android.net.Uri
import androidx.test.platform.app.InstrumentationRegistry
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SafQueryAndroidTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val uri = Uri.parse("content://com.mangoloads.player.synthetic/root")
    @Before fun reset() = SyntheticSafProvider.reset()

    @Test fun successfulReadClosesCursor() = runBlocking {
        val name = context.contentResolver.readSafCursor(uri, arrayOf("name")) { cursor, checkActive ->
            checkActive(); assertTrue(cursor.moveToFirst()); cursor.getString(0)
        }
        assertEquals("Synthetic", name)
        assertTrue(SyntheticSafProvider.cursorClosed)
    }

    @Test fun failingReadClosesCursor() {
        assertThrows(IOException::class.java) {
            runBlocking {
                context.contentResolver.readSafCursor(uri, arrayOf("name")) { _, _ -> throw IOException("synthetic") }
            }
        }
        assertTrue(SyntheticSafProvider.cursorClosed)
    }

    @Test(timeout = 15_000) fun coroutineCancellationReachesBlockedProvider() = runBlocking {
        SyntheticSafProvider.blocked = true
        val task = async { context.contentResolver.readSafCursor(uri, arrayOf("name")) { _, _ -> Unit } }
        try {
            assertTrue(withContext(Dispatchers.IO) { SyntheticSafProvider.entered.await(5, TimeUnit.SECONDS) })
            task.cancel()
            task.join()
            assertTrue(SyntheticSafProvider.cancelled.await(5, TimeUnit.SECONDS))
        } finally { task.cancel() }
    }
}
