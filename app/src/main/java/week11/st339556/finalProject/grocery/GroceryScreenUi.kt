package week11.st339556.finalProject.grocery

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.firestore.GeoPoint
import week11.st339556.finalProject.model.GroceryStore
import week11.st339556.finalProject.model.GroceryStoreViewModel
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreListScreen(
    onBackClick: () -> Unit
) {
    val viewModel: GroceryStoreViewModel = viewModel()
    val stores by viewModel.stores.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }

    // Handle messages
    LaunchedEffect(message) {
        message?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Stores",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Store")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Store count
            Text(
                text = "${stores.size} stores",
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            if (stores.isEmpty()) {
                // Show empty state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Store,
                            contentDescription = "No stores",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No stores yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add your first grocery store",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { showAddDialog = true }) {
                            Text("Add Store")
                        }
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = padding,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    items(stores) { groceryStore ->
                        // Create display Store from GroceryStore
                        val displayStore = Store(
                            name = groceryStore.groceryName ?: "Unnamed Store",
                            address = formatLocation(groceryStore.groceryLocation),
                            hours = "Hours: 8:00 AM – 10:00 PM"
                        )

                        StoreItem(
                            store = displayStore,
                            onDelete = {
                                viewModel.deleteStore(groceryStore.groceryStoreId)
                            },
                            groceryStore = groceryStore // Pass the actual GroceryStore for location data
                        )
                    }
                }
            }
        }
    }

    // Add Store Dialog
    if (showAddDialog) {
        AddStoreDialog(
            onDismiss = { showAddDialog = false },
            onAddStore = { name, location ->
                val newStore = GroceryStore(
                    groceryName = name,
                    groceryLocation = location
                )
                viewModel.addStore(newStore)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun StoreItem(
    store: Store,
    onDelete: () -> Unit = {},
    groceryStore: GroceryStore? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = store.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                // Menu button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Menu",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        // Add map options to menu
                        groceryStore?.groceryLocation?.let { location ->
                            DropdownMenuItem(
                                text = { Text("Open in Maps") },
                                onClick = {
                                    showMenu = false
                                    MapUtils.openStoreInMaps(context, store.name, location.latitude, location.longitude)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Map, contentDescription = "Map")
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Get Directions") },
                                onClick = {
                                    showMenu = false
                                    MapUtils.navigateToStore(context, store.name, location.latitude, location.longitude)
                                },
                                leadingIcon = {
                                    Icon(Icons.Default.Directions, contentDescription = "Directions")
                                }
                            )
                        }

                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = {
                                showMenu = false
                                // TODO: Implement edit functionality
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                        )

                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Address
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = store.address,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Hours
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = store.hours,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            // Map link (if coordinates available)
            groceryStore?.groceryLocation?.let { location ->
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = {
                        MapUtils.openStoreInMaps(context, store.name, location.latitude, location.longitude)
                    },
                    modifier = Modifier.padding(start = 22.dp, top = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Map",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "View on Map",
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

// Helper function to format GeoPoint to readable address
private fun formatLocation(geoPoint: GeoPoint?): String {
    return if (geoPoint != null) {
        String.format("Lat: %.4f, Long: %.4f", geoPoint.latitude, geoPoint.longitude)
    } else {
        "Address not specified"
    }
}

// Keep your existing Store data class
data class Store(
    val name: String,
    val address: String,
    val hours: String
)

@Composable
fun AddStoreDialog(
    onDismiss: () -> Unit,
    onAddStore: (String, GeoPoint?) -> Unit
) {
    var storeName by remember { mutableStateOf("") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Store") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = storeName,
                    onValueChange = { storeName = it },
                    label = { Text("Store Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Location (Optional)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = latitude,
                        onValueChange = { latitude = it },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = longitude,
                        onValueChange = { longitude = it },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f)
                    )
                }

                if (latitude.isNotBlank() && longitude.isNotBlank()) {
                    Text(
                        text = "Example: 37.7749, -122.4194 (San Francisco)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val geoPoint = if (latitude.isNotBlank() && longitude.isNotBlank()) {
                        try {
                            GeoPoint(latitude.toDouble(), longitude.toDouble())
                        } catch (e: NumberFormatException) {
                            null
                        }
                    } else {
                        null
                    }
                    onAddStore(storeName, geoPoint)
                },
                enabled = storeName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}