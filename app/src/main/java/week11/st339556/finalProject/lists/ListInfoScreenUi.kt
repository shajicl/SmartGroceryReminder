package week11.st339556.finalProject.lists

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun ListInfoScreenUi(
    listId: String,
    onBackClick: () -> Unit,
    db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    val context = LocalContext.current

    var isLoading by remember { mutableStateOf(true) }
    var listName by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<String>>(emptyList()) }

    // completed items (synced with Firestore)
    var completedSet by remember { mutableStateOf(setOf<String>()) }

    var newItemText by remember { mutableStateOf("") }
    var newItemQty by remember { mutableStateOf("") } // just visual for now

    // -------- LOAD LIST FROM FIRESTORE --------
    LaunchedEffect(listId) {
        db.collection("lists").document(listId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    listName = doc.getString("name") ?: ""
                    items = (doc.get("items") as? List<String>) ?: emptyList()

                    // load completed items from Firestore
                    val completedFromDb =
                        (doc.get("completedItems") as? List<*>)?.mapNotNull { it as? String }?.toSet()
                            ?: emptySet()
                    completedSet = completedFromDb

                    isLoading = false
                } else {
                    Toast.makeText(context, "List not found", Toast.LENGTH_SHORT).show()
                    onBackClick()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                onBackClick()
            }
    }

    if (isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val completedCount = completedSet.size
    val totalCount = items.size.coerceAtLeast(1) // avoid division by 0
    val progress = completedCount.toFloat() / totalCount.toFloat()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {

                // ---- Top bar (back + delete) ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        db.collection("lists").document(listId)
                            .delete()
                            .addOnSuccessListener {
                                Toast.makeText(context, "List deleted", Toast.LENGTH_SHORT).show()
                                onBackClick()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete list",
                            tint = Color(0xFFE74C3C)
                        )
                    }
                }

                // ---- Title + progress text ----
                Text(
                    text = listName,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$completedCount of ${items.size} completed",
                    fontSize = 13.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(12.dp))

                // ---- Progress bar ----
                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    trackColor = Color(0xFFEDEDED),
                    color = Color(0xFF8E44FF)
                )

                Spacer(Modifier.height(16.dp))

                // ---- Items list ----
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // show incomplete items first, then completed
                    val orderedItems = items.sortedBy { completedSet.contains(it) }

                    orderedItems.forEach { itemName ->
                        val isDone = completedSet.contains(itemName)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    // toggle completion in UI
                                    val newSet = if (isDone) {
                                        completedSet - itemName
                                    } else {
                                        completedSet + itemName
                                    }
                                    completedSet = newSet

                                    // save to Firestore
                                    db.collection("lists").document(listId)
                                        .update("completedItems", newSet.toList())
                                        .addOnFailureListener { e ->
                                            Toast.makeText(
                                                context,
                                                "Error: ${e.message}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(Color(0xFFF7F7F7))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // check circle
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isDone) Color(0xFF8E44FF)
                                            else Color.White
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Text("✓", color = Color.White, fontSize = 14.sp)
                                    }
                                }

                                Spacer(Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = itemName,
                                        fontSize = 15.sp,
                                        fontWeight = if (isDone)
                                            FontWeight.Medium
                                        else
                                            FontWeight.Normal,
                                        color = if (isDone) Color.Gray else Color.Black,
                                        textDecoration = if (isDone)
                                            TextDecoration.LineThrough
                                        else
                                            TextDecoration.None
                                    )
                                    Text(
                                        text = "Qty: --",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                // ---- Add new item area (bottom) ----
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newItemText,
                        onValueChange = { newItemText = it },
                        modifier = Modifier.weight(1.7f),
                        singleLine = true,
                        placeholder = { Text("Add new item...") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    OutlinedTextField(
                        value = newItemQty,
                        onValueChange = { newItemQty = it },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        placeholder = { Text("Qty") },
                        shape = RoundedCornerShape(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmed = newItemText.trim()
                            if (trimmed.isNotEmpty()) {
                                val updated = items + trimmed
                                items = updated

                                db.collection("lists").document(listId)
                                    .update("items", updated)
                                    .addOnSuccessListener {
                                        newItemText = ""
                                        newItemQty = ""
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(
                                            context,
                                            "Error: ${e.message}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                            }
                        },
                        modifier = Modifier.size(44.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8E44FF)
                        )
                    ) {
                        Text("+")
                    }
                }
            }
        }
    }
}
