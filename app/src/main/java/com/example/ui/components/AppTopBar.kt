package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.sync.SyncStatus
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    phaseTitle: String,
    phaseSubtitle: String,
    syncStatus: SyncStatus,
    isDarkMode: Boolean,
    onToggleDark: () -> Unit,
    onOpenSyncSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        modifier = modifier.testTag("app_top_bar"),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground
        ),
        title = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "The 90-Day Challenge",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.3.sp
                        )
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = phaseTitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = "— $phaseSubtitle",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        },
        actions = {
            // Cloud Sync Status Pill
            Surface(
                onClick = onOpenSyncSettings,
                shape = RoundedCornerShape(16.dp),
                color = when (syncStatus) {
                    SyncStatus.SYNCED -> SageGreen.copy(alpha = 0.18f)
                    SyncStatus.SYNCING -> AmberGold.copy(alpha = 0.18f)
                    SyncStatus.OFFLINE_READY -> SteelBlue.copy(alpha = 0.18f)
                    SyncStatus.ERROR -> Color(0xFFE57373).copy(alpha = 0.18f)
                },
                modifier = Modifier.padding(end = 4.dp).testTag("sync_pill_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(
                                when (syncStatus) {
                                    SyncStatus.SYNCED -> SageGreen
                                    SyncStatus.SYNCING -> AmberGold
                                    SyncStatus.OFFLINE_READY -> SteelBlue
                                    SyncStatus.ERROR -> Color(0xFFE57373)
                                }
                            )
                    )
                    Text(
                        text = when (syncStatus) {
                            SyncStatus.SYNCED -> "Cloud Synced"
                            SyncStatus.SYNCING -> "Syncing..."
                            SyncStatus.OFFLINE_READY -> "Offline Ready"
                            SyncStatus.ERROR -> "Sync Alert"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    )
                }
            }

            // Dark / Light toggle
            IconButton(
                onClick = onToggleDark,
                modifier = Modifier.testTag("theme_toggle_button")
            ) {
                Icon(
                    imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle Dark Mode",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // Settings button
            IconButton(
                onClick = onOpenSyncSettings,
                modifier = Modifier.testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.CloudSync,
                    contentDescription = "Sync & Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    )
}
