package week11.st339556.finalProject.household

import android.widget.Toast
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

import week11.st339556.finalProject.model.Household
import week11.st339556.finalProject.model.HouseholdViewModel


// Theme-ish colors
private val PurpleMain = Color(0xFF8E44FF)
private val PurpleSoftBg = Color(0xFFF6F1FF)

@Composable
fun HouseholdScreenUi(
    navController: NavController,
    householdViewModel: HouseholdViewModel = viewModel()
) {
    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth
    val currentUser = auth.currentUser
    val households by householdViewModel.households.collectAsState()

    val coroutineScope = rememberCoroutineScope()

    var selectedHousehold by remember { mutableStateOf<Household?>(null) }
    var householdName by remember { mutableStateOf(TextFieldValue("")) }
    var showInviteBox by remember { mutableStateOf(false) }

    // Empty-state join/create controls
    var joinCode by remember { mutableStateOf("") }
    var joinError by remember { mutableStateOf<String?>(null) }
    var isBusy by remember { mutableStateOf(false) }

    // Keep selected household in sync with list
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
        } else {
            // If list became empty, clear selection
            selectedHousehold = null
        }
    }

    // Helper for creating a new household for current user
    suspend fun createHouseholdForCurrentUser(): String {
        val uid = currentUser?.uid
            ?: throw IllegalStateException("User not logged in")

        val db = FirebaseFirestore.getInstance()
        val doc = db.collection("households").document()

        val name = "${currentUser.email?.substringBefore('@') ?: "My"} Household"

        val household = Household(
            id = doc.id,
            householdName = name,
            creatorId = uid,
            userIds = listOf(uid),
            groceryListIds = emptyList()
        )

        doc.set(household).await()

        // optional: store householdId on user doc
        db.collection("users").document(uid)
            .update("householdId", doc.id)
            .await()

        return doc.id
    }



    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleSoftBg)
            .padding(18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Top bar
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

        // ✅ MAIN HOUSEHOLD CARD (ONLY if user has at least one household)
        selectedHousehold?.let { household ->

            // Gradient header
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

            // Invite
            OutlinedButton(
                onClick = { showInviteBox = !showInviteBox },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        listOf(Color(0xFF9D50FF), Color(0xFFFF63C3))
                    )
                )
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = PurpleMain
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Invite Member", color = PurpleMain)
            }

            if (showInviteBox) {
                Spacer(modifier = Modifier.height(12.dp))
                HouseholdInviteBox(household.id)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Members
            Text(
                text = "Members",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            val sortedUserIds = household.userIds.sortedBy { it != currentUser?.uid }

            sortedUserIds.forEachIndexed { index, uid ->
                val isCurrentUser = uid == currentUser?.uid
                val isAdmin = index == 0

                MemberRowFromUserId(
                    uid = uid,
                    isAdmin = isAdmin,
                    isCurrentUser = isCurrentUser
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))
        }

        // ✅ EITHER empty-state OR list of households
        if (households.isEmpty()) {
            EmptyHouseholdSection(
                joinCode = joinCode,
                onJoinCodeChange = {
                    joinCode = it
                    joinError = null
                },
                isBusy = isBusy,
                errorMessage = joinError,
                primaryColor = PurpleMain,
                onJoinClick = {
                    val uid = currentUser?.uid
                    if (uid == null) {
                        Toast.makeText(context, "You must be logged in", Toast.LENGTH_SHORT).show()
                        return@EmptyHouseholdSection
                    }
                    if (joinCode.isBlank()) {
                        joinError = "Household ID required"
                        return@EmptyHouseholdSection
                    }

                    isBusy = true
                    coroutineScope.launch {
                        try {
                            val db = FirebaseFirestore.getInstance()
                            val docRef = db.collection("households")
                                .document(joinCode.trim())

                            val snapshot = docRef.get().await()
                            if (!snapshot.exists()) {
                                joinError = "No household found with that ID"
                            } else {
                                val existingUserIds =
                                    snapshot.get("userIds") as? List<String> ?: emptyList()
                                if (existingUserIds.contains(uid)) {
                                    joinError = "You’re already a member of this household"
                                } else {
                                    docRef.update("userIds", FieldValue.arrayUnion(uid)).await()
                                    Toast.makeText(
                                        context,
                                        "Joined household!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    joinCode = ""
                                    joinError = null
                                }
                            }
                        } catch (e: Exception) {
                            joinError = e.message ?: "Failed to join household"
                        } finally {
                            isBusy = false
                        }
                    }
                },
                onCreateClick = {
                    val uid = currentUser?.uid
                    if (uid == null) {
                        Toast.makeText(context, "You must be logged in", Toast.LENGTH_SHORT).show()
                        return@EmptyHouseholdSection
                    }

                    isBusy = true
                    coroutineScope.launch {
                        try {
                            val newId = createHouseholdForCurrentUser()
                            Toast.makeText(
                                context,
                                "Household created! ID: $newId",
                                Toast.LENGTH_SHORT
                            ).show()
                            joinError = null
                        } catch (e: Exception) {
                            joinError = e.message ?: "Failed to create household"
                        } finally {
                            isBusy = false
                        }
                    }
                }
            )
        } else {
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
}

// ====================== MEMBER ROW =====================

@Composable
fun MemberRowFromUserId(
    uid: String,
    isAdmin: Boolean = false,
    isCurrentUser: Boolean = false,
) {
    val auth: FirebaseAuth = Firebase.auth
    val currentUser = auth.currentUser

    var name by remember { mutableStateOf<String?>(null) }
    var email by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        if (isCurrentUser) {
            name = currentUser?.displayName ?: currentUser?.email?.substringBefore("@")
            email = currentUser?.email
        } else {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    name = doc.getString("displayName") ?: doc.getString("name") ?: "Member"
                    email = doc.getString("email")
                }
                .addOnFailureListener {
                    name = "Member"
                    email = null
                }
        }
    }

    val displayName = name ?: if (isCurrentUser) "You" else "Member"
    val emailText = email ?: ""

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
                (displayName.firstOrNull() ?: 'M').uppercaseChar().toString(),
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
            if (emailText.isNotBlank()) {
                Text(emailText, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
        }

        Box(
            modifier = Modifier
                .background(
                    when {
                        isAdmin && isCurrentUser -> Color(0xFFFFE0B2)
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
                    isAdmin && isCurrentUser -> "Admin"
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

// ====================== INVITE BOX =====================

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

// ====================== HOUSEHOLD CARD =====================

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

// ====================== EMPTY STATE =====================

@Composable
fun EmptyHouseholdSection(
    joinCode: String,
    onJoinCodeChange: (String) -> Unit,
    isBusy: Boolean,
    errorMessage: String?,
    primaryColor: Color,
    onJoinClick: () -> Unit,
    onCreateClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No household yet",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Join your family or roommates using a household ID, or create a new household to start sharing lists.",
                color = Color.Gray,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = joinCode,
                onValueChange = onJoinCodeChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Household ID") },
                isError = errorMessage != null
            )
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 🔹 Purple join button
            Button(
                onClick = onJoinClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp),
                shape = RoundedCornerShape(999.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryColor,
                    contentColor = Color.White
                ),
                enabled = !isBusy
            ) {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Join Household")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = onCreateClick, enabled = !isBusy) {
                Text(
                    text = "Or create a new household",
                    color = primaryColor
                )
            }
        }
    }
}
