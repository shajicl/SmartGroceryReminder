package week11.st339556.finalProject.lists

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.vector.ImageVector

@Preview(
    showBackground = true,
    showSystemUi = true,
    device = Devices.PIXEL_4
)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateListScreenUi(
    onBackClick: () -> Unit = {},
    onCreateListClick: (String, String, List<String>, String, String?) -> Unit = { _, _, _, _, _ -> },
    onMyListsTabClick: () -> Unit = {},
    onHouseholdTabClick: () -> Unit = {},

    // FIXED: Changed from List<String?> to List<String>
    households: List<String> = emptyList(),
    selectedHousehold: String = "Personal List",
    onHouseholdSelected: (String) -> Unit = {},
    dropdownExpanded: Boolean = false,
    onDropdownExpandedChange: (Boolean) -> Unit = {},

    initialListName: String = "",
    initialHousehold: String = "None",
    initialItems: List<String> = emptyList(),
    initialPriority: String = "Medium",
    initialDueDate: String? = null
) {
    // ---------- STATE ----------
    var listName by remember { mutableStateOf(initialListName) }

    val householdOptions = listOf("Personal List") + households
    var localSelectedHousehold by remember { mutableStateOf(selectedHousehold) }
    var isHouseholdMenuExpanded by remember { mutableStateOf(dropdownExpanded) }

    var currentItem by remember { mutableStateOf("") }
    var currentItemQty by remember { mutableStateOf("") }
    var currentItemBrand by remember { mutableStateOf("") }
    var items by remember { mutableStateOf(initialItems) }

    val priorities = listOf("Low", "Medium", "High")
    var selectedPriority by remember { mutableStateOf(initialPriority) }

    var dueDate by remember { mutableStateOf(initialDueDate ?: "") }

    val accentPurple = Color(0xFF8E44FF)
    val gradientPurple = Brush.horizontalGradient(
        listOf(Color(0xFF8E44FF), Color(0xFFBC70FF))
    )

    LaunchedEffect(selectedHousehold) {
        localSelectedHousehold = selectedHousehold
    }

    LaunchedEffect(dropdownExpanded) {
        isHouseholdMenuExpanded = dropdownExpanded
    }

    // ---------- SCREEN ----------
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF4F1FF)),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // ---------- TOP BAR ----------
                Row(
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
                        text = if (initialListName.isEmpty()) "Create New List" else "Edit List",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Main white card content
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // -------- List Name --------
                        Text(
                            text = "List Name",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = listName,
                            onValueChange = { listName = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("e.g. \"Groceries\", \"Party Prep\"") },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5)
                            )
                        )

                        Spacer(Modifier.height(16.dp))

                        // -------- Assign to Household (Dropdown) --------
                        Text(
                            text = "Assign to Household",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))

                        // FIXED: Correct dropdown structure
                        ExposedDropdownMenuBox(
                            expanded = isHouseholdMenuExpanded,
                            onExpandedChange = {
                                isHouseholdMenuExpanded = it
                                onDropdownExpandedChange(it)
                            }
                        ) {
                            OutlinedTextField(
                                value = localSelectedHousehold,
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = "Select household"
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                    focusedContainerColor = Color(0xFFF5F5F5)
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = isHouseholdMenuExpanded,
                                onDismissRequest = {
                                    isHouseholdMenuExpanded = false
                                    onDropdownExpandedChange(false)
                                }
                            ) {
                                householdOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            localSelectedHousehold = option
                                            isHouseholdMenuExpanded = false
                                            onHouseholdSelected(option)
                                            onDropdownExpandedChange(false)
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // -------- Add Items --------
                        Text(
                            text = "Add Items",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // for item name
                            OutlinedTextField(
                                value = currentItem,
                                onValueChange = { currentItem = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Item") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                    focusedContainerColor = Color(0xFFF5F5F5)
                                )
                            )

                            // for quantity
                            OutlinedTextField(
                                value = currentItemQty,
                                onValueChange = { currentItemQty = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Quantity") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                    focusedContainerColor = Color(0xFFF5F5F5)
                                )
                            )

                            // for brand (optional)
                            OutlinedTextField(
                                value = currentItemBrand,
                                onValueChange = { currentItemBrand = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Brand (optional)") },
                                shape = RoundedCornerShape(10.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFFF5F5F5),
                                    focusedContainerColor = Color(0xFFF5F5F5)
                                )
                            )

                            Spacer(Modifier.width(8.dp))

                            Button(
                                onClick = {
                                    val itemName = currentItem.trim()
                                    val itemQty = currentItemQty.trim()
                                    val itemBrand = currentItemBrand.trim()

                                    if (itemName.isNotEmpty()) {
                                        val item = buildString {
                                            append(itemName)
                                            if (itemQty.isNotEmpty()) append(" (x$itemQty)")
                                            if (itemBrand.isNotEmpty()) append(" - $itemBrand")
                                        }
                                        items = items + item

                                        currentItem = ""
                                        currentItemQty = ""
                                        currentItemBrand = ""
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = accentPurple
                                ),
                                modifier = Modifier.height(48.dp)
                            ) {
                                Text("Add")
                            }
                        }

                        // Chips for items
                        if (items.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items.forEach { item ->
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color(0xFFEDE9FF)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .padding(
                                                    horizontal = 10.dp,
                                                    vertical = 4.dp
                                                ),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = item,
                                                fontSize = 12.sp,
                                                color = Color(0xFF3B3B3B)
                                            )
                                            Spacer(Modifier.width(4.dp))
                                            Text(
                                                text = "x",
                                                modifier = Modifier.clickable {
                                                    items = items - item
                                                },
                                                fontSize = 12.sp,
                                                color = accentPurple
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // -------- Priority --------
                        Text(
                            text = "Priority",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            priorities.forEach { level ->
                                val isSelected = level == selectedPriority
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .clickable { selectedPriority = level },
                                    shape = RoundedCornerShape(20.dp),
                                    color = if (isSelected) Color(0xFFEDE9FF) else Color.White,
                                    border = if (isSelected)
                                        BorderStroke(2.dp, accentPurple)
                                    else
                                        BorderStroke(1.dp, Color(0xFFE0E0E0))
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Text(
                                            text = level,
                                            fontSize = 13.sp,
                                            color = if (isSelected) accentPurple else Color(0xFF555555)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        // -------- Due Date (simple field + icon) --------
                        Text(
                            text = "Due Date (Optional)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(
                            value = dueDate,
                            onValueChange = { dueDate = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = { Text("Select Due Date") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Calendar"
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFFF5F5F5),
                                focusedContainerColor = Color(0xFFF5F5F5)
                            )
                        )

                        Spacer(Modifier.height(20.dp))

                        // -------- Create/Update List button --------
                        Button(
                            onClick = {
                                onCreateListClick(
                                    listName,
                                    localSelectedHousehold,
                                    items,
                                    selectedPriority,
                                    dueDate.ifBlank { null }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Transparent
                            ),
                            contentPadding = PaddingValues()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        gradientPurple,
                                        RoundedCornerShape(26.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (initialListName.isEmpty()) "Create List" else "Update List",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))


            }
        }
    }
}
