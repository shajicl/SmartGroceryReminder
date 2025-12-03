package week11.st339556.finalProject.household

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

import week11.st339556.finalProject.model.Household
import week11.st339556.finalProject.model.HouseholdViewModel

@Composable
fun HouseholdScreenUi(
    navController: NavController,
    householdViewModel: HouseholdViewModel = viewModel()
) {
    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth
    val currentUser = auth.currentUser

    val households by householdViewModel.households.collectAsState()

    var editingName by remember { mutableStateOf(false) }
    var householdName by remember { mutableStateOf(TextFieldValue("")) }
    var selectedHousehold by remember { mutableStateOf<Household?>(null) }
    var showInviteBox by remember { mutableStateOf(false) }

    LaunchedEffect(households) {
        if (households.isNotEmpty()) {
            if (selectedHousehold == null) {
                selectedHousehold = households.first()
                householdName = TextFieldValue(selectedHousehold?.householdName ?: "")
            } else {
                val updated = households.find { it.id == selectedHousehold!!.id }
                if (updated != null) {
                    selectedHousehold = updated
                    householdName = TextFieldValue(updated.householdName ?: "")
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.Black
                )
            }
            Text(
                text = "Household",
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        selectedHousehold?.let { household ->

            // Household Gradient Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF9D50FF), Color(0xFFFF63C3))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = household.householdName ?: "Household",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "${household.userIds.size} members",
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Invite Member Button
            OutlinedButton(
                onClick = { showInviteBox = !showInviteBox },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF9D50FF))
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = Color(0xFF9D50FF)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invite Member", color = Color(0xFF9D50FF))
            }

            if (showInviteBox) {
                Spacer(modifier = Modifier.height(12.dp))
                HouseholdInviteBox(household.id)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // MEMBERS LIST
            Text(
                text = "Members",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            // Show current user first as admin, then other members
            val sortedUserIds = household.userIds.sortedBy { it != currentUser?.uid }

            sortedUserIds.forEachIndexed { index, uid ->
                val isCurrentUser = uid == currentUser?.uid
                val isAdmin = index == 0 // First user is admin

                MemberRowFromUserId(
                    uid = uid,
                    isAdmin = isAdmin,
                    isCurrentUser = isCurrentUser,
                    currentUserEmail = currentUser?.email
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Household List Below
        households.forEach { household ->
            HouseholdCard(
                household = household,
                onClick = {
                    selectedHousehold = household
                    householdName = TextFieldValue(household.householdName ?: "")
                    showInviteBox = false
                }
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

// Updated member row with proper user info
@Composable
fun MemberRowFromUserId(
    uid: String,
    isAdmin: Boolean = false,
    isCurrentUser: Boolean = false,
    currentUserEmail: String? = null
) {
    val auth: FirebaseAuth = Firebase.auth
    val user = auth.currentUser

    // For the current user, we can get email from Firebase Auth
    // For other users, you'd need to fetch from Firestore (we'll handle this later)
    val displayName = if (isCurrentUser) {
        user?.displayName ?: user?.email?.substringBefore("@") ?: "You"
    } else {
        // For now, show UID - later you can fetch user profiles from Firestore
        "User: ${uid.take(8)}..."
    }

    val email = if (isCurrentUser) {
        user?.email ?: "No email"
    } else {
        "Fetching..." // You'd fetch this from Firestore user collection
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF7F7F7), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(
                    if (isCurrentUser) Color(0xFFE3F2FD) else Color(0xFFEDE7F6),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                displayName.take(1).uppercase(),
                fontWeight = FontWeight.Bold,
                color = if (isCurrentUser) Color(0xFF1976D2) else Color.Black
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                displayName,
                fontWeight = FontWeight.SemiBold,
                color = if (isCurrentUser) Color(0xFF1976D2) else Color.Black
            )
            Text(email, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }

        // Role badge
        Box(
            modifier = Modifier
                .background(
                    when {
                        isAdmin && isCurrentUser -> Color(0xFFFFF8E1)
                        isAdmin -> Color(0xFFFFE0B2)
                        isCurrentUser -> Color(0xFFE8F5E8)
                        else -> Color(0xFFC8F7D0)
                    },
                    RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                when {
                    isAdmin && isCurrentUser -> "You (Admin)"
                    isAdmin -> "Admin"
                    isCurrentUser -> "You"
                    else -> "Member"
                },
                color = Color.Black,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
fun HouseholdInviteBox(householdId: String) {
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFFFF1F8), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Copy Household ID",
                fontWeight = FontWeight.Bold,
                color = Color(0xFF9D50FF)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SelectionContainer {
                    Text(householdId, fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = {
                    clipboardManager.setText(AnnotatedString(householdId))
                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(
                        Icons.Default.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color(0xFF9D50FF)
                    )
                }
            }
        }
    }
}

@Composable
fun HouseholdCard(household: Household, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF6F6F6), RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(Color(0xFFD6CFFF), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(household.householdName?.first()?.uppercase() ?: "")
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(household.householdName ?: "Unnamed Household", fontWeight = FontWeight.Bold)
        }

        Text(
            "${household.userIds.size} members",
            color = Color.Gray,
            style = MaterialTheme.typography.bodySmall
        )
    }
}