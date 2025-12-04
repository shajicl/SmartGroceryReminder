package week11.st339556.finalProject.home


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ---------- MAIN HOME SCREEN ----------

@Composable
fun HomeScreenUi(
    onCreateNewListClick: () -> Unit = {},
    onMyListsClick: () -> Unit = {},
    onHouseholdClick: () -> Unit = {},
    onStoresClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onViewProfileClick: () -> Unit = {},
    onLogoutClick: () -> Unit = {}     // <-- NEW callback
) {
    // Outer light purple background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3E8FF)),   // light lavender / purple
        contentAlignment = Alignment.Center
    ) {
        // Center white rounded card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF7F3FF)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ------- Title -------
                Text(
                    text = "Family Organizer",
                    style = TextStyle(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Manage your household together",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                )

                Spacer(Modifier.height(24.dp))

                // -------- Create New List --------
                Button(
                    onClick = onCreateNewListClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E44FF)
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create New List",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Create New List",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(24.dp))

                // -------- Feature Grid --------
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HomeFeatureCard(
                            title = "My Lists",
                            icon = Icons.Default.List,
                            onClick = onMyListsClick
                        )
                        HomeFeatureCard(
                            title = "Household",
                            icon = Icons.Default.Group,
                            onClick = onHouseholdClick
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        HomeFeatureCard(
                            title = "Stores",
                            icon = Icons.Default.Store,
                            onClick = onStoresClick
                        )
                        HomeFeatureCard(
                            title = "Settings",
                            icon = Icons.Default.Settings,
                            onClick = onSettingsClick
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // -------- View Profile --------
                Text(
                    text = "View Profile",
                    modifier = Modifier.clickable { onViewProfileClick() },
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Color(0xFF8E44FF),
                        textDecoration = TextDecoration.Underline
                    ),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(18.dp))

                // -------- LOGOUT BUTTON (purple outline) --------
                Button(
                    onClick = onLogoutClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(45.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 1.dp,
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                            listOf(Color(0xFF8E44FF), Color(0xFF8E44FF))
                        )
                    )
                ) {
                    Text(
                        text = "Logout",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF8E44FF)
                    )
                }
            }
        }
    }
}

// ---------- SMALL REUSABLE CARD FOR EACH FEATURE ----------

@Composable
private fun HomeFeatureCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(140.dp)
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}
