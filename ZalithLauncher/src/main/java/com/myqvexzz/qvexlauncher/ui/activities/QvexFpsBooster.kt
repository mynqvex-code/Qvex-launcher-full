package com.myqvexzz.qvexlauncher.ui.activities

import android.app.Activity
import android.os.Build
import android.os.PerformanceHintManager
import com.myqvexzz.qvexlauncher.setting.AllSettings

object QvexFpsBooster {
    private var session: PerformanceHintManager.Session? = null
    private var renderThreadId = -1
    private var targetDurationNs = 0L

    @Synchronized
    fun onFrameBatch(
        activity: Activity,
        threadId: Int,
        actualDurationNs: Long
    ) {
        try {
            val enabled =
                AllSettings.qvexPowerMax.getValue() &&
                AllSettings.qvexFpsPriority.getValue()

            if (!enabled ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                activity.isDestroyed ||
                activity.isFinishing
            ) {
                stop()
                return
            }

            if (threadId <= 0 || actualDurationNs <= 0L) return

            val refreshRate = activity.windowManager
                .defaultDisplay.refreshRate

            if (refreshRate <= 0f) return

            val newTarget = (1_000_000_000L / refreshRate).toLong()
            var currentSession = session

            if (currentSession == null || renderThreadId != threadId) {
                stop()

                val manager = activity.getSystemService(
                    PerformanceHintManager::class.java
                ) ?: return

                currentSession = manager.createHintSession(
                    intArrayOf(threadId),
                    newTarget
                ) ?: return

                session = currentSession
                renderThreadId = threadId
                targetDurationNs = newTarget
            }

            if (targetDurationNs != newTarget) {
                currentSession.updateTargetWorkDuration(newTarget)
                targetDurationNs = newTarget
            }

            if (actualDurationNs <= newTarget * 4) {
                currentSession.reportActualWorkDuration(actualDurationNs)
            }
        } catch (_: Exception) {
            stop()
        }
    }

    @Synchronized
    fun stop() {
        runCatching { session?.close() }
        session = null
        renderThreadId = -1
        targetDurationNs = 0L
    }
}
