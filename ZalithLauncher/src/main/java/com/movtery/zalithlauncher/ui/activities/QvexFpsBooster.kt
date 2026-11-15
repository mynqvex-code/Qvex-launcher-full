package com.movtery.zalithlauncher.ui.activities

import android.app.Activity
import android.os.Build
import android.os.SystemClock
import android.os.PerformanceHintManager
import com.movtery.zalithlauncher.setting.AllSettings
object QvexFpsBooster {

    private var session: PerformanceHintManager.Session? = null
    private var lastFrameTimeNs = 0L
    private var renderThreadId = -1
    private var targetDurationNs = 0L
        @Synchronized
    fun onFrameBatch(activity: Activity, threadId: Int) {
        try {
            val enabled =
                AllSettings.qvexPowerMax.getValue() &&
                AllSettings.qvexFpsPriority.getValue()

            if (!enabled || Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                activity.isDestroyed || activity.isFinishing
            ) {
                stop()
                return
            }

            if (threadId <= 0) return

            val frameTime = SystemClock.uptimeNanos()
            val refreshRate = activity.windowManager
                .defaultDisplay.refreshRate

            if (refreshRate <= 0f) return

            val newTarget = (1_000_000_000L / refreshRate).toLong()
            val currentSession = session

            if (currentSession == null || renderThreadId != threadId) {
                stop()

                val manager = activity.getSystemService(
                    PerformanceHintManager::class.java
                ) ?: return

                val newSession = manager.createHintSession(
                    intArrayOf(threadId),
                    newTarget
                ) ?: return

                session = newSession
                renderThreadId = threadId
                targetDurationNs = newTarget
                lastFrameTimeNs = frameTime
                return
            }

            if (targetDurationNs != newTarget) {
                currentSession.updateTargetWorkDuration(newTarget)
                targetDurationNs = newTarget
            }

            val actualDuration = frameTime - lastFrameTimeNs
            lastFrameTimeNs = frameTime

            if (actualDuration > 0L && actualDuration <= newTarget * 4) {
                currentSession.reportActualWorkDuration(actualDuration)
            }
        } catch (_: Exception) {
            stop()
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
