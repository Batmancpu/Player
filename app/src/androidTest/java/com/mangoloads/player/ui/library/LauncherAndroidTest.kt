package com.mangoloads.player.ui.library

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.ViewModelProvider
import androidx.test.platform.app.InstrumentationRegistry
import com.mangoloads.player.MainActivity
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class LauncherAndroidTest {
    @Test(timeout = 20_000) fun launcherCreatesAndRetainsLibraryOwnerAcrossRecreation() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val context = instrumentation.targetContext
        val original = instrumentation.startActivitySync(
            Intent(context, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK),
        )
        val model = AtomicReference<LibraryViewModel>()
        instrumentation.runOnMainSync { model.set(ViewModelProvider(original as MainActivity)[LibraryViewModel::class.java]) }
        val recreated = AtomicReference<Activity>()
        val resumed = CountDownLatch(1)
        val app = context.applicationContext as Application
        val callback = object : Application.ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                if (activity is MainActivity && activity !== original) { recreated.set(activity); resumed.countDown() }
            }
            override fun onActivityCreated(activity: Activity, state: Bundle?) = Unit
            override fun onActivityStarted(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivityStopped(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, state: Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        }
        app.registerActivityLifecycleCallbacks(callback)
        try {
            instrumentation.runOnMainSync { original.recreate() }
            assertTrue(resumed.await(10, TimeUnit.SECONDS))
            instrumentation.runOnMainSync {
                assertSame(model.get(), ViewModelProvider(recreated.get() as MainActivity)[LibraryViewModel::class.java])
            }
        } finally {
            app.unregisterActivityLifecycleCallbacks(callback)
            instrumentation.runOnMainSync { (recreated.get() ?: original).finish() }
        }
    }
}
