package week11.st339556.finalProject.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBackClick: () -> Unit) {

    val items = listOf(
        SettingItem(
            title = "Notifications",
            subtitle = "Reminders and alerts",
            icon = Icons.Default.Notifications,
            iconBg = Color(0xFFE8F0FF),
            iconTint = Color(0xFF2962FF),
            onClick = { /* TODO: open notifications settings */ }
        ),
        SettingItem(
            title = "Privacy & Security",
            subtitle = "Account and data control",
            icon = Icons.Default.Lock,
            iconBg = Color(0xFFFFEBEE),
            iconTint = Color(0xFFD32F2F),
            onClick = { /* TODO */ }
        ),
        SettingItem(
            title = "Appearance",
            subtitle = "Theme and display",
            icon = Icons.Default.ColorLens,
            iconBg = Color(0xFFF3E5F5),
            iconTint = Color(0xFF7B1FA2),
            onClick = { /* TODO */ }
        ),
        SettingItem(
            title = "Language & Region",
            subtitle = "Localization",
            icon = Icons.Default.Language,
            iconBg = Color(0xFFE8F5E9),
            iconTint = Color(0xFF2E7D32),
            onClick = { /* TODO */ }
        ),
        SettingItem(
            title = "Help & Support",
            subtitle = "FAQ and contact",
            icon = Icons.Default.Help,
            iconBg = Color(0xFFFFF8E1),
            iconTint = Color(0xFFF9A825),
            onClick = { /* TODO */ }
        )
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },

        containerColor = Color(0xFFF3F4F6)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            // White rounded card in the middle
            Card(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                ) {
                    items.forEach { item ->
                        SettingsRow(item = item)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Version 1.0.0",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }
}


@Composable
private fun SettingsRow(item: SettingItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.iconTint
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                )
                if (item.subtitle != null) {
                    Text(
                        text = item.subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open",
                tint = Color.LightGray
            )
        }
    }
}

private data class SettingItem(
    val title: String,
    val subtitle: String? = null,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconBg: Color,
    val iconTint: Color,
    val onClick: () -> Unit
)
