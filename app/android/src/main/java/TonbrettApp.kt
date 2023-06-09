package dev.schlaubi.tonbrett.app.android

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.util.Log

class TonbrettApp : Application(), Application.ActivityLifecycleCallbacks {
    private var currentActivity: Activity? = null

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(this)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("Tonbrett", "An error occurred in ${currentActivity!!::class.simpleName}", throwable)
            if (throwable is IllegalArgumentException && currentActivity is AppActivity) {
                val intent = Intent(applicationContext, UpdateActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            }
            Process.killProcess(Process.myPid())
        }
    }

    override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(p0: Activity) = Unit

    override fun onActivityStopped(p0: Activity) = Unit

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) = Unit

    override fun onActivityDestroyed(activity: Activity) {
        currentActivity = activity
    }
}
