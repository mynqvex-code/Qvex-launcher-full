package com.movtery.zalithlauncher.ui.activities

import android.os.Process
import com.movtery.zalithlauncher.setting.AllSettings

object QvexCpuMax {

    private var targetThreadId = -1
    private var originalPriority: Int? = null

    @Synchronized
    fun applyToThread(threadId: Int) {
        if (!AllSettings.qvexPowerMax.state ||
            !AllSettings.qvexCpuMax.state
        ) {
            stop()
            return
        }

        if (threadId <= 0) return

        if (targetThreadId == threadId) return

        stop()

        try {
            val oldPriority = Process.getThreadPriority(threadId)

            Process.setThreadPriority(
                threadId,
                Process.THREAD_PRIORITY_DISPLAY
            )

            targetThreadId = threadId
            originalPriority = oldPriority
        } catch (_: SecurityException) {
            stop()
        } catch (_: Exception) {
            stop()
        }
    }

    @Synchronized
    fun stop() {
        val threadId = targetThreadId
        val priority = originalPriority

        targetThreadId = -1
        originalPriority = null

        if (threadId > 0 && priority != null) {
            try {
                Process.setThreadPriority(threadId, priority)
            } catch (_: Exception) {
            }
        }
    }
}
