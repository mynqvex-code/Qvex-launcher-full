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
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn

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
                SettingsCard(
                    position = CardPosition.Top,
                    title = "QVEX MAX POWER",
                    summary = "Hệ thống tối ưu hiệu năng khi chạy Minecraft.",
                    onClick = {}
                )
                SettingsCard(
                    position = CardPosition.Middle,
                    title = "CPU MAX",
                    summary = "Ưu tiên hiệu năng xử lý CPU trong giới hạn Android.",
                    onClick = {}
                )
                SettingsCard(
                    position = CardPosition.Middle,
                    title = "GPU MAX",
                    summary = "Ưu tiên hiệu năng đồ họa khi chơi Minecraft.",
                    onClick = {}
                )
                SettingsCard(
                    position = CardPosition.Middle,
                    title = "RAM OPTIMIZATION",
                    summary = "Theo dõi và tối ưu tài nguyên bộ nhớ.",
                    onClick = {}
                )
                SettingsCard(
                    position = CardPosition.Middle,
                    title = "THERMAL GUARD",
                    summary = "Theo dõi nhiệt độ và điều chỉnh tối ưu khi máy nóng.",
                    onClick = {}
                )
                SettingsCard(
                    position = CardPosition.Middle,
                    title = "BATTERY MONITOR",
                    summary = "Theo dõi pin, trạng thái sạc và mức tiêu thụ.",
                    onClick = {}
                )
                SettingsCard(
                    position = CardPosition.Bottom,
                    title = "FPS PRIORITY",
                    summary = "Ưu tiên FPS ổn định khi chơi Minecraft.",
                    onClick = {}
                )
            }

            Text(
                text = "QVEX MAX POWER sẽ tự kích hoạt khi Minecraft khởi chạy và tắt khi trò chơi kết thúc."
            )
        }
    }
}
