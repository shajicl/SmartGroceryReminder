package week11.st339556.finalProject.lists


import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.material3.CircularProgressIndicator

import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

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

    var isLoading by remember { mutableStateOf(true) }
    var lists by remember { mutableStateOf<List<GroceryList>>(emptyList()) }

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
                lists = docs.map { doc ->
                    GroceryList(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        household = doc.getString("household") ?: "",
                        items = (doc.get("items") as? List<*>)?.mapNotNull { it as? String }?.toMutableList()
                            ?: mutableListOf(),
                        priority = when (doc.getString("priority")) {
                            "High" -> Priority.HIGH
                            "Low" -> Priority.LOW
                            else -> Priority.MEDIUM
                        },
                        dueDate = doc.getString("dueDate").takeUnless { it.isNullOrBlank() },
                        members = emptyList()  // later if you want
                    )
                }
                isLoading = false
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
            },
            onBackClick = onBackClick,
            onHouseholdTabClick = onHouseholdTabClick
        )
    }
}
