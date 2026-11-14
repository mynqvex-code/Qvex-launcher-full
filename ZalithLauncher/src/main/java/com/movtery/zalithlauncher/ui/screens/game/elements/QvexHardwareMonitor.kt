package com.movtery.zalithlauncher.ui.screens.game.elements
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consume
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
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
    val onlineCpus = try {
        File("/sys/devices/system/cpu/online")
            .readText()
            .trim()
            .split(",")
            .flatMap { part ->
                val range = part.split("-")
                if (range.size == 2) {
                    val start = range[0].toInt()
                    val end = range[1].toInt()
                    (start..end).toList()
                } else {
                    listOf(part.toInt())
                }
            }
    } catch (_: Exception) {
        (0..7).toList()
    }

    val frequencies = onlineCpus.mapNotNull { cpu ->
        val paths = listOf(
            "/sys/devices/system/cpu/cpu$cpu/cpufreq/scaling_cur_freq",
            "/sys/devices/system/cpu/cpu$cpu/cpufreq/cpuinfo_cur_freq"
        )

        paths.firstNotNullOfOrNull { path ->
            try {
                File(path).readText().trim().toLongOrNull()
                    ?.takeIf { it > 0 }
            } catch (_: Exception) {
                null
            }
        }
    }

    if (frequencies.isEmpty()) return null

    return (frequencies.average() / 1000.0).roundToInt()
}
    private fun readBatteryTemperature(context: Context): Float? {
    val batteryIntent = context.registerReceiver(
        null,
        IntentFilter(Intent.ACTION_BATTERY_CHANGED)
    ) ?: return null

    val temperature = batteryIntent.getIntExtra(
        BatteryManager.EXTRA_TEMPERATURE, -1
    )

    return if (temperature >= 0) temperature / 10f else null
    }

    
private suspend fun sampleHardware(
    context: Context,
    previousCpu: QvexCpuStats?
): Pair<QvexHardwareStats, QvexCpuStats?> =
    withContext(Dispatchers.IO) {
        val currentCpu = readCpuStats()
        val stats = QvexHardwareStats(
    cpuUsage = calculateCpuUsage(previousCpu, currentCpu),
    gpuUsage = readGpuUsage(),
    cpuFrequency = readCpuFrequency(),
    batteryTemperature = readBatteryTemperature(context)
)
    
        stats to currentCpu
    }

@Composable
fun QvexHardwareMonitor(
    modifier: Modifier = Modifier
) {
  val context = LocalContext.current
    val hudPreferences = remember(context) {
        context.getSharedPreferences(
            "qvex_hardware_hud",
            Context.MODE_PRIVATE
        )
    }    
    var showHighTemperatureWarning by remember {
        mutableStateOf(false)
    }
    var hasShownTemperatureWarning by remember {
        mutableStateOf(false)
    }    
    var stats by remember {
        mutableStateOf(
            QvexHardwareStats(
                cpuUsage = null,
                gpuUsage = null,
                cpuFrequency = null
            )
        )
    }
    val density = LocalDensity.current

    var hudOffset by remember {
        mutableStateOf(
            IntOffset(
                hudPreferences.getInt("hud_x", 0),
                hudPreferences.getInt("hud_y", 0)
            )
        )
    }

    var hudSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    LaunchedEffect(Unit) {
        var previousCpu: QvexCpuStats? = null

        while (true) {
            val result = sampleHardware(context, previousCpu)
            stats = result.first

            val batteryTemp = result.first.batteryTemperature

            if (batteryTemp != null) {
                if (batteryTemp > 50f && !hasShownTemperatureWarning) {
                    showHighTemperatureWarning = true
                    hasShownTemperatureWarning = true
                } else if (batteryTemp <= 50f) {
                    hasShownTemperatureWarning = false
                }
            }

            previousCpu = result.second
            delay(1000)
        }
    }


    BoxWithConstraints(modifier = modifier) {
        val maxWidthPx = with(density) { maxWidth.roundToPx() }
        val maxHeightPx = with(density) { maxHeight.roundToPx() }
        val safeHudOffset = IntOffset(
    hudOffset.x.coerceIn(
        0,
        (maxWidthPx - hudSize.width).coerceAtLeast(0)
    ),
    hudOffset.y.coerceIn(
        0,
        (maxHeightPx - hudSize.height).coerceAtLeast(0)
    )
)

LaunchedEffect(safeHudOffset) {
    if (hudOffset != safeHudOffset) {
        hudOffset = safeHudOffset

        hudPreferences.edit()
            .putInt("hud_x", safeHudOffset.x)
            .putInt("hud_y", safeHudOffset.y)
            .apply()
    }
}
        Surface(
            modifier = Modifier
                .offset { safeHudOffset }
                .onSizeChanged { hudSize = it },
            color = Color(0xE620242B),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "QVEX HARDWARE",
                        modifier = Modifier.pointerInput(maxWidthPx, maxHeightPx) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()

                                val newOffset = IntOffset(
    (hudOffset.x + dragAmount.x.roundToInt())
        .coerceIn(0, (maxWidthPx - hudSize.width).coerceAtLeast(0)),
    (hudOffset.y + dragAmount.y.roundToInt())
        .coerceIn(0, (maxHeightPx - hudSize.height).coerceAtLeast(0))
)

hudOffset = newOffset

hudPreferences.edit()
    .putInt("hud_x", newOffset.x)
    .putInt("hud_y", newOffset.y)
    .apply()
                        
                            }
                        },
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
                }

                Column {
                    HardwareRow(
                        label = "CPU CLOCK",
                        value = stats.cpuFrequency?.let { "$it MHz" } ?: "N/A"
                    )

                    HardwareRow(
                        label = "BATTERY TEMP",
                        value = stats.batteryTemperature?.let { "$it°C" } ?: "N/A"
                    )
                }
            }
        }
    }
    if (showHighTemperatureWarning) {
        AlertDialog(
            onDismissRequest = {
                showHighTemperatureWarning = false
            },
            title = {
                Text("Cảnh báo nhiệt độ")
            },
            text = {
                Text("Nhiệt độ cao, vui lòng tạm dừng trò chơi.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showHighTemperatureWarning = false
                    }
                ) {
                    Text("Đã hiểu")
                }
            }
        )
    }
}

@Composable
private fun HardwareRow(
    label: String,
    value: String
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
