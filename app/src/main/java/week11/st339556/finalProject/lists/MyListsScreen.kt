package week11.st339556.finalProject.lists

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.List
import androidx.compose.material.icons.outlined.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MyListsScreen(
    lists: List<GroceryList>,
    onOpenList: (GroceryList) -> Unit,
    onEditList: (GroceryList) -> Unit,
    onDeleteList: (GroceryList) -> Unit,
    onMyListsTabClick: () -> Unit = {},
    onHouseholdTabClick: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val purple = Color(0xFF8E44FF)
    val background = Color(0xFF111111)
    val cardBackground = Color(0xFFF4F1FF)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(cardBackground),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // --- Top bar with Back + Title ---
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "My Lists",
                        style = TextStyle(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ---- Search bar (UI only for now) ----
                OutlinedTextField(
                    value = "",
                    onValueChange = { /* hook to search later */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(14.dp),
                    placeholder = { Text("Search lists...") },
                    singleLine = true
                )

                Spacer(Modifier.height(16.dp))

                // ---- List of cards ----
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                ) {
                    items(lists) { list ->
                        ListSummaryCard(
                            list = list,
                            purple = purple,
                            onClick = { onOpenList(list) },
                            onEdit = { onEditList(list) },
                            onDelete = { onDeleteList(list) }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }


            }
        }
    }
}

@Composable
private fun ListSummaryCard(
    list: GroceryList,
    purple: Color,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: name + priority pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = list.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                PriorityChip(priority = list.priority)
            }

            Spacer(Modifier.height(6.dp))

            // Row 2: info line
            Text(
                text = "${list.items.size} items   •   Due: ${list.dueDate ?: "—"}",
                fontSize = 12.sp,
                color = Color.Gray
            )

            // Show household if exists
            if (!list.householdName.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Household: ${list.householdName}",
                    fontSize = 12.sp,
                    color = Color(0xFF8E44FF)
                )
            }

            Spacer(Modifier.height(10.dp))

            // Row 3: Member avatars (only show if there are members)
            if (list.members.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Display member avatars (max 3 with overlap)
                    Row {
                        list.members.take(3).forEachIndexed { index, member ->
                            Box(
                                modifier = Modifier
                                    .offset(x = (-8 * index).dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(member.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = member.initials,
                                    fontSize = 10.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Show +X if there are more than 3 members
                        if (list.members.size > 3) {
                            Box(
                                modifier = Modifier
                                    .offset(x = (-8 * 3).dp)
                                    .size(26.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${list.members.size - 3}",
                                    fontSize = 10.sp,
                                    color = Color.DarkGray,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(Modifier.width(12.dp))

                    // Member count text
                    Text(
                        text = "${list.members.size} member${if (list.members.size != 1) "s" else ""}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(Modifier.height(10.dp))
            }

            // Row 4: Edit / Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Outlined.Edit,
                        contentDescription = "Edit",
                        tint = purple
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Edit", color = purple)
                }

                TextButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Outlined.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE95B5B)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text("Delete", color = Color(0xFFE95B5B))
                }
            }
        }
    }
}
@Composable
private fun PriorityChip(priority: Priority) {
    val (bg, text, label) = when (priority) {
        Priority.HIGH -> Triple(Color(0xFFFFE5E9), Color(0xFFE74C3C), "high")
        Priority.MEDIUM -> Triple(Color(0xFFFFF5D6), Color(0xFFF39C12), "medium")
        Priority.LOW -> Triple(Color(0xFFE8FFF0), Color(0xFF27AE60), "low")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = text,
            fontWeight = FontWeight.Medium
        )
    }
}

