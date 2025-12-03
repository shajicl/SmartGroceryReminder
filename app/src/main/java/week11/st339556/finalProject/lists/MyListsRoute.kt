package week11.st339556.finalProject.lists

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.Timestamp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@Composable
fun MyListsRoute(
    onBackClick: () -> Unit,
    onOpenList: (GroceryList) -> Unit,
    onEditList: (GroceryList) -> Unit,
    onHouseholdTabClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }
    val auth = remember { FirebaseAuth.getInstance() }
    val coroutineScope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(true) }
    var lists by remember { mutableStateOf<List<GroceryList>>(emptyList()) }

    // Predefined colors for member avatars
    val memberColors = listOf(
        Color(0xFF8E44FF), // Purple
        Color(0xFF34C759), // Green
        Color(0xFF007AFF), // Blue
        Color(0xFFFF9500), // Orange
        Color(0xFFFF3B30), // Red
        Color(0xFF5AC8FA), // Light Blue
        Color(0xFFAF52DE), // Deep Purple
        Color(0xFFFF2D55)  // Pink
    )

    LaunchedEffect(Unit) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(context, "Please log in again", Toast.LENGTH_SHORT).show()
            isLoading = false
            return@LaunchedEffect
        }

        db.collection("lists")
            .whereEqualTo("userId", user.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Toast.makeText(context, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addSnapshotListener
                }

                val docs = snapshot?.documents ?: emptyList()

                // Launch a coroutine to fetch members for each list
                coroutineScope.launch {
                    val updatedLists = docs.mapNotNull { doc ->
                        try {
                            // Get household members if household exists
                            val members = mutableListOf<Member>()
                            val householdId = doc.getString("householdId")

                            if (!householdId.isNullOrBlank()) {
                                try {
                                    // 🔹 FIX: await() inside coroutine
                                    val householdDoc = db.collection("households")
                                        .document(householdId)
                                        .get()
                                        .await()

                                    val userIds = householdDoc.get("userIds") as? List<String> ?: emptyList()

                                    // Create member objects from userIds
                                    members.addAll(userIds.mapIndexed { index, userId ->
                                        val colorIndex = index % memberColors.size
                                        Member(
                                            id = userId,
                                            name = "User ${userId.take(4)}",
                                            initials = "U${index + 1}",
                                            color = memberColors[colorIndex]
                                        )
                                    })
                                } catch (e: Exception) {
                                    // Couldn't fetch household, continue without members
                                }
                            }

                            GroceryList(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                listName = doc.getString("name") ?: "",
                                userId = doc.getString("userId") ?: user.uid,
                                householdId = householdId,
                                householdName = doc.getString("householdName"),
                                items = (doc.get("items") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                                priority = when (doc.getString("priority")) {
                                    "High" -> Priority.HIGH
                                    "Low" -> Priority.LOW
                                    else -> Priority.MEDIUM
                                },
                                dueDate = doc.getString("dueDate"),
                                createdAt = (doc.get("createdAt") as? Timestamp)?.toDate()?.toString() ?: "",
                                isCompleted = doc.getBoolean("isCompleted") ?: false,
                                members = members
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }

                    lists = updatedLists
                    isLoading = false
                }
            }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        MyListsScreen(
            lists = lists,
            onOpenList = onOpenList,
            onEditList = onEditList,
            onDeleteList = { list ->
                db.collection("lists").document(list.id).delete()
                    .addOnSuccessListener {
                        Toast.makeText(context, "List deleted", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            },
            onBackClick = onBackClick,
            onMyListsTabClick = {},
            onHouseholdTabClick = onHouseholdTabClick
        )
    }
}