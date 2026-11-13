package com.movtery.zalithlauncher.ui.screens.game.elements

import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.background
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.roundToInt

private data class QvexCpuStats(
    val idle: Long,
    val total: Long
)

private data class QvexHardwareStats(
    val cpuUsage: Int?,
    val gpuUsage: Int?,
    val cpuFrequency: Int?,
    val batteryLevel: Int? = null,
    val batteryCharging: Boolean? = null,
    val batteryTemperature: Float? = null
)

private fun readCpuStats(): QvexCpuStats? {
    return try {
        val line = File("/proc/stat").bufferedReader().use {
            it.readLine()
        } ?: return null

        val values = line.trim().split(Regex("\\s+"))
            .drop(1)
            .mapNotNull { it.toLongOrNull() }

        if (values.size < 4) return null

        val idle = values[3] + (values.getOrNull(4) ?: 0L)
        val total = values.sum()

        QvexCpuStats(idle, total)
    } catch (_: Exception) {
        null
    }
}

private fun calculateCpuUsage(
    previous: QvexCpuStats?,
    current: QvexCpuStats?
): Int? {
    if (previous == null || current == null) return null

    val totalDifference = current.total - previous.total
    val idleDifference = current.idle - previous.idle

    if (totalDifference <= 0) return null

    return (
        (totalDifference - idleDifference).toFloat() /
            totalDifference.toFloat() * 100f
        ).roundToInt().coerceIn(0, 100)
}

private fun readGpuUsage(): Int? {
    val paths = listOf(
        "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage",
        "/sys/class/kgsl/kgsl-3d0/gpubusy"
    )

    for (path in paths) {
        try {
            val value = File(path).readText().trim()

            val percentage = value.toIntOrNull()
            if (percentage != null && percentage in 0..100) {
                return percentage
            }

            val numbers = Regex("\\d+").findAll(value)
                .mapNotNull { it.value.toLongOrNull() }
                .toList()

            if (numbers.size >= 2 && numbers[1] > 0) {
                return (
                    numbers[0].toDouble() /
                        numbers[1].toDouble() * 100.0
                    ).roundToInt().coerceIn(0, 100)
            }
        } catch (_: Exception) {
            // Không có quyền đọc hoặc thiết bị không hỗ trợ.
        }
    }

    return null
}

private fun readCpuFrequency(): Int? {
    val paths = listOf(
        "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq",
        "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_cur_freq"
    )

    for (path in paths) {
        try {
            val khz = File(path).readText().trim().toLongOrNull()
            if (khz != null && khz > 0) {
                return (khz / 1000L).toInt()
            }
        } catch (_: Exception) {
            // Không có quyền đọc hoặc thiết bị không hỗ trợ.
        }
    }

    return null
}

private fun readBatteryInfo(
    context: Context
): Triple<Int?, Boolean?, Float?> {
    val batteryIntent = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    ) ?: return Triple(null, null, null)

    val level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
    val percentage = if (level >= 0 && scale > 0) {
        level * 100 / scale
    } else null

    val status = batteryIntent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL

    val temperature = batteryIntent.getIntExtra(
        BatteryManager.EXTRA_TEMPERATURE, -1
    ).let { if (it >= 0) it / 10f else null }

    return Triple(percentage, charging, temperature)
}

private suspend fun sampleHardware(
    context: Context,
    previousCpu: QvexCpuStats?
): Pair<QvexHardwareStats, QvexCpuStats?> =
    withContext(Dispatchers.IO) {
        val currentCpu = readCpuStats()
        val battery = readBatteryInfo(context)

        val stats = QvexHardwareStats(
            cpuUsage = calculateCpuUsage(previousCpu, currentCpu),
            gpuUsage = readGpuUsage(),
            cpuFrequency = readCpuFrequency(),
            batteryLevel = battery.first,
            batteryCharging = battery.second,
            batteryTemperature = battery.third
        )

        stats to currentCpu
    }

@Composable
fun QvexHardwareMonitor(
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
    var stats by remember {
        mutableStateOf(
            QvexHardwareStats(
                cpuUsage = null,
                gpuUsage = null,
                cpuFrequency = null
            )
        )
    }

    LaunchedEffect(Unit) {
        var previousCpu: QvexCpuStats? = null

        while (true) {
            val result = sampleHardware(context, previousCpu)
            stats = result.first
            previousCpu = result.second
            delay(1000)
        }
    }

    Surface(
        modifier = modifier,
        color = Color(0xE620242B),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "QVEX HARDWARE",
                color = Color(0xFF64B5F6),
                fontSize = 13.sp
            )

            HardwareRow(
                label = "CPU",
                value = stats.cpuUsage?.let { "$it%" } ?: "N/A"
            )

            HardwareRow(
                label = "GPU",
                value = stats.gpuUsage?.let { "$it%" } ?: "N/A"
            )
            HardwareRow(
                label = "CPU CLOCK",
                value = stats.cpuFrequency?.let { "$it MHz" } ?: "N/A"
            )

            HardwareRow(
                label = "BATTERY",
                value = stats.batteryLevel?.let { "$it%" } ?: "N/A"
            )

            HardwareRow(
                label = "CHARGING",
                value = stats.batteryCharging?.let {
                    if (it) "YES" else "NO"
                } ?: "N/A"
            )

            HardwareRow(
                label = "BATTERY TEMP",
                value = stats.batteryTemperature?.let { "$it°C" } ?: "N/A"
            )
        }
    }
}

@Composable
private fun HardwareRow(
    label: String,
    value: String
) {
) {
    Row(
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 12.sp
        )

        Text(
            text = value,
            color = Color(0xFF80CBC4),
            fontSize = 12.sp
        )
    }
}
