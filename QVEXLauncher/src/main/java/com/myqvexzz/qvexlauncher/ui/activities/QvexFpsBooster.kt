package com.myqvexzz.qvexlauncher.ui.activities

import android.app.Activity
import android.os.Build
import android.os.PerformanceHintManager
import com.myqvexzz.qvexlauncher.setting.AllSettings

object QvexFpsBooster {

    private const val FRAMES_PER_BATCH = 8L
    private const val NANOS_PER_SECOND = 1_000_000_000.0

    private var session: PerformanceHintManager.Session? = null
    private var renderThreadId = -1
    private var targetDurationNs = 0L
    private var activityResumed = false

    @Synchronized
    fun onActivityResumed() {
        activityResumed = true
    }

    @Synchronized
    fun onActivityPaused() {
        activityResumed = false
        stop()
    }

    @Synchronized
    fun onFrameBatch(
        activity: Activity,
        threadId: Int,
        actualDurationNs: Long
    ) {
        try {
            val gpuMaxEnabled =
                AllSettings.qvexGpuMax.getValue()

            val fpsPriorityEnabled =
                AllSettings.qvexFpsPriority.getValue()

            val enabled =
                AllSettings.qvexPowerMax.getValue() &&
                (gpuMaxEnabled || fpsPriorityEnabled)

            if (
                !enabled ||
                !activityResumed ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                activity.isDestroyed ||
                activity.isFinishing
            ) {
                stop()
                return
            }

            if (threadId <= 0 || actualDurationNs <= 0L) {
                return
            }

            @Suppress("DEPRECATION")
            val refreshRate =
                activity.windowManager.defaultDisplay.refreshRate

            if (!refreshRate.isFinite() || refreshRate <= 0f) {
                return
            }

            if (refreshRate <= 90f && !gpuMaxEnabled) {
                stop()
                return
            }

            val newTarget = (
                FRAMES_PER_BATCH *
                    NANOS_PER_SECOND /
                    refreshRate.toDouble()
                ).toLong()

            if (newTarget <= 0L) return

            var currentSession = session

            if (
                currentSession == null ||
                renderThreadId != threadId
            ) {
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

                // Bỏ mẫu đầu tiên khi tạo session mới.
                // Tránh gửi thời gian bị kéo dài do chuyển ứng dụng.
                return
            }

            if (targetDurationNs != newTarget) {
                currentSession.updateTargetWorkDuration(
                    newTarget
                )
                targetDurationNs = newTarget
            }

            // Báo cả những chu kỳ bị chậm để ADPF nhận đúng dữ liệu.
            currentSession.reportActualWorkDuration(
                actualDurationNs
            )

        } catch (_: Exception) {
            stop()
        }
    }

    @Synchronized
    fun stop() {
        runCatching {
            session?.close()
        }

        session = null
        renderThreadId = -1
        targetDurationNs = 0L
    }
}
