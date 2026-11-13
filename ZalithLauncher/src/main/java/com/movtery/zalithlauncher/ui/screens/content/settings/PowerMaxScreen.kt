package com.movtery.zalithlauncher.ui.screens.content.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.movtery.zalithlauncher.ui.base.BaseScreen
import com.movtery.zalithlauncher.ui.screens.NestedNavKey
import com.movtery.zalithlauncher.ui.screens.NormalNavKey
import com.movtery.zalithlauncher.ui.screens.TitledNavKey
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.CardPosition
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCard
import com.movtery.zalithlauncher.ui.screens.content.settings.layouts.SettingsCardColumn
import com.movtery.zalithlauncher.ui.components.verticalScrollWithBar

@Composable
fun PowerMaxScreen(
    key: NestedNavKey.Settings,
    settingsScreenKey: TitledNavKey?,
    mainScreenKey: TitledNavKey?
) {
    BaseScreen(
        Triple(key, mainScreenKey, false),
        Triple(NormalNavKey.Settings.PowerMax, settingsScreenKey, false)
    ) { isVisible ->
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
                    summary = "Hệ thống tối ưu hiệu năng khi chạy Minecraft."
                )

                SettingsCard(
                    position = CardPosition.Middle,
                    title = "CPU MAX",
                    summary = "Ưu tiên hiệu năng xử lý CPU trong giới hạn Android cho phép."
                )

                SettingsCard(
                    position = CardPosition.Middle,
                    title = "GPU MAX",
                    summary = "Ưu tiên hiệu năng đồ họa khi chơi Minecraft."
                )

                SettingsCard(
                    position = CardPosition.Middle,
                    title = "RAM OPTIMIZATION",
                    summary = "Theo dõi bộ nhớ và tối ưu tài nguyên của launcher."
                )

                SettingsCard(
                    position = CardPosition.Middle,
                    title = "THERMAL GUARD",
                    summary = "Theo dõi nhiệt độ và tự giảm mức tối ưu khi thiết bị quá nóng."
                )

                SettingsCard(
                    position = CardPosition.Middle,
                    title = "BATTERY MONITOR",
                    summary = "Theo dõi phần trăm pin, trạng thái sạc và mức tiêu thụ pin."
                )

                SettingsCard(
                    position = CardPosition.Bottom,
                    title = "FPS PRIORITY",
                    summary = "Ưu tiên FPS ổn định khi chơi Minecraft."
                )
            }

            Text(
                text = "QVEX MAX POWER sẽ tự kích hoạt khi Minecraft khởi chạy và tắt khi trò chơi kết thúc.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
