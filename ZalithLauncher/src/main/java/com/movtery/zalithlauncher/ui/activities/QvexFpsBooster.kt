package com.movtery.zalithlauncher.ui.activities

import android.app.Activity
import android.os.Build
import android.os.Debug
import android.os.PerformanceHintManager
import android.os.Process
import com.movtery.zalithlauncher.setting.AllSettings
object QvexFpsBooster {

    private var session: PerformanceHintManager.Session? = null
    private var lastFrameTimeNs = 0L
    private var renderThreadId = -1
    private var targetDurationNs = 0L

    @Synchronized
    fun onFrameBatch(activity: Activity) {
        val enabled =
            AllSettings.qvexPowerMax.getValue() &&
            AllSettings.qvexFpsPriority.getValue()

        if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            stop()
            return
        }

        val threadId = Process.myTid()
        val frameTime = SystemClock.currentThreadTimeNanos()

        val refreshRate = activity.windowManager
            .defaultDisplay.refreshRate

        if (refreshRate <= 0f) return

        val newTarget = (8_000_000_000L / refreshRate).toLong()

        if (session == null || renderThreadId != threadId) {
            stop()

            val manager = activity.getSystemService(
                PerformanceHintManager::class.java
            ) ?: return

            session = manager.createHintSession(
                intArrayOf(threadId),
                newTarget
            ) ?: return

            renderThreadId = threadId
            targetDurationNs = newTarget
            lastFrameTimeNs = frameTime
            return
        }

        if (targetDurationNs != newTarget) {
            session?.updateTargetWorkDuration(newTarget)
            targetDurationNs = newTarget
        }

        val actualDuration = frameTime - lastFrameTimeNs
lastFrameTimeNs = frameTime

if (actualDuration > 0L && actualDuration <= newTarget * 4) {
    session?.reportActualWorkDuration(actualDuration)
}
    }
    @Synchronized
    fun stop() {
        session?.close()
        session = null
        lastFrameTimeNs = 0L
        renderThreadId = -1
        targetDurationNs = 0L
    }
}
