
package com.movtery.zalithlauncher.ui.screens.content

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.setting.AllSettings
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SwitchSettingsCard

@Composable
fun PowerMaxScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.PowerMax, settingsScreenKey, false)
    ) { _ ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScrollWithBar(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingsCardColumn(
                modifier = Modifier.fillMaxWidth()
            ) {
                SwitchSettingsCard(
                    unit = AllSettings.qvexPowerMax,
                    position = CardPosition.Top,
                    title = "QVEX MAX POWER",
                    summary = "Bật hoặc tắt hệ thống QVEX MAX POWER."
                )

                SwitchSettingsCard(
                    unit = AllSettings.qvexCpuMax,
                    position = CardPosition.Middle,
                    title = "CPU MAX",
                    summary = "Ưu tiên hiệu năng xử lý CPU.",
                    enabled = AllSettings.qvexPowerMax.state
                )

                SwitchSettingsCard(
                    unit = AllSettings.qvexGpuMax,
                    position = CardPosition.Middle,
                    title = "GPU MAX",
                    summary = "Ưu tiên hiệu năng đồ họa.",
                    enabled = AllSettings.qvexPowerMax.state
                )

                SwitchSettingsCard(
                    unit = AllSettings.qvexRamOptimization,
                    position = CardPosition.Middle,
                    title = "RAM OPTIMIZATION",
                    summary = "Theo dõi và tối ưu tài nguyên bộ nhớ.",
                    enabled = AllSettings.qvexPowerMax.state
                )

                SwitchSettingsCard(
                    unit = AllSettings.qvexThermalGuard,
                    position = CardPosition.Middle,
                    title = "THERMAL GUARD",
                    summary = "Theo dõi nhiệt độ và điều chỉnh khi máy nóng.",
                    enabled = AllSettings.qvexPowerMax.state
                )

                SwitchSettingsCard(
                    unit = AllSettings.qvexBatteryMonitor,
                    position = CardPosition.Middle,
                    title = "BATTERY MONITOR",
                    summary = "Theo dõi pin và trạng thái sạc.",
                    enabled = AllSettings.qvexPowerMax.state
                )

                SwitchSettingsCard(
                    unit = AllSettings.qvexFpsPriority,
                    position = CardPosition.middle,
                    title = "FPS BOOSTER",
                    summary = "Ưu tiên FPS ổn định khi chơi Minecraft.",
                    enabled = AllSettings.qvexPowerMax.state
                )
                SwitchSettingsCard(
                    unit = AllSettings.qvexHardwareMonitor,
                    position = CardPosition.Bottom,
                    title = "HARDWARE MONITOR",
                    summary = "Hiển thị phần trăm sử dụng CPU và GPU theo thời gian thực.",
                    enabled = AllSettings.qvexPowerMax.state
)
            }
            

            Text(
                text = "LƯU Ý BẬT CÁC TÙY CHỌN CÓ THỂ NÓNG MÁY."
            )
        }
    }
}
