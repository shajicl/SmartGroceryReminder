package week11.st339556.finalProject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import week11.st339556.finalProject.auth.ForgotPasswordScreen
import week11.st339556.finalProject.auth.LoginScreenUi   // <— your login screen
import week11.st339556.finalProject.auth.SignUpScreen
import week11.st339556.finalProject.data.HouseholdRepository
import week11.st339556.finalProject.home.HomeScreenUi
import week11.st339556.finalProject.household.HouseholdScreenUi
import week11.st339556.finalProject.lists.CreateListScreenUi
import week11.st339556.finalProject.lists.ListInfoScreenUi
import week11.st339556.finalProject.lists.MyListsRoute
import week11.st339556.finalProject.lists.Priority
import week11.st339556.finalProject.model.Household
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import week11.st339556.finalProject.auth.SignUpScreenUi
import week11.st339556.finalProject.grocery.StoreListScreen
import week11.st339556.finalProject.settings.SettingsScreen

import week11.st339556.finalProject.ui.theme.SmartGroceryReminderTheme
// ^ change the theme name if your template used a different one

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SmartGroceryReminderTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    NavHost(
                        navController = navController,
                        startDestination = "login"
                    ) {
                        // Login
                        composable("login") {
                            // Make sure your LoginScreenUi has these callbacks or adjust as needed
                            LoginScreenUi(
                                navController = navController
                            )
                        }

                        // Sign up
                        composable("signup") {
                            SignUpScreenUi(
                                onSignUpSuccess = { userId, householdId ->
                                    // Your navigation after signup
                                    navController.navigate("home")
                                },
                                onSignInClick = {
                                    navController.navigate("login")
                                }
                            )
                        }


                        // Forgot password
                        composable("forgotPassword") {
                            ForgotPasswordScreen(navController = navController)
                        }

                        composable("home") {
                            HomeScreenUi(
                                onCreateNewListClick = {
                                    navController.navigate("createList")
                                },
                                onMyListsClick = {
                                    navController.navigate("myLists")
                                },
                                onHouseholdClick = { navController.navigate("household") },
                                onStoresClick = { navController.navigate("stores")},
                                onSettingsClick = { /* TODO */ },



                                onViewProfileClick = { /* TODO */ }
                            )
                        }

                        composable("myLists") {
                            MyListsRoute(
                                onBackClick = { navController.popBackStack() },
                                onOpenList = { list ->
                                    // Handle opening a list
                                },
                                onEditList = { list ->
                                    // Navigate to edit screen
                                    navController.navigate("editList/${list.id}")
                                },
                                onHouseholdTabClick = {
                                    navController.navigate("household")
                                }
                            )
                        }

                        composable("createList") {
                            val context = LocalContext.current
                            val auth = FirebaseAuth.getInstance()
                            val householdRepository = remember { HouseholdRepository() }
                            val coroutineScope = rememberCoroutineScope()

                            val currentUser = auth.currentUser
                            val userId = currentUser?.uid ?: ""

                            // State for households and selected household
                            var households by remember { mutableStateOf<List<Household>>(emptyList()) }
                            var selectedHousehold by remember { mutableStateOf<Household?>(null) }
                            var dropdownExpanded by remember { mutableStateOf(false) }

                            // Load user's households
                            LaunchedEffect(userId) {
                                if (userId.isNotEmpty()) {
                                    householdRepository.getUserHouseholds(userId).collect { householdList ->
                                        households = householdList
                                    }
                                }
                            }

                            CreateListScreenUi(
                                onBackClick = { navController.popBackStack() },
                                onCreateListClick = { listName, _, items, priority, dueDate ->
                                    // Note: The second parameter (_) is the manual input which we're no longer using

                                    val user = auth.currentUser
                                    if (user == null) {
                                        Toast.makeText(context, "You must be logged in", Toast.LENGTH_SHORT).show()
                                        navController.navigate("login")
                                        return@CreateListScreenUi
                                    }

                                    if (listName.isBlank()) {
                                        Toast.makeText(context, "List name cannot be empty", Toast.LENGTH_SHORT).show()
                                        return@CreateListScreenUi
                                    }

                                    // Get household ID if selected, otherwise null for personal list
                                    val householdId = selectedHousehold?.id ?: ""
                                    val householdName = selectedHousehold?.householdName ?: ""

                                    val listData = hashMapOf(
                                        "userId" to user.uid,
                                        "name" to listName,
                                        "householdId" to householdId,
                                        "householdName" to householdName,
                                        "items" to items,
                                        "priority" to priority,
                                        "dueDate" to (dueDate ?: ""),
                                        "createdAt" to com.google.firebase.Timestamp.now(),
                                        "isCompleted" to false
                                    )

                                    FirebaseFirestore.getInstance()
                                        .collection("lists")
                                        .add(listData)
                                        .addOnSuccessListener { documentReference ->
                                            // If a household is selected, also add the list to the household's groceryListIds
                                            if (selectedHousehold != null) {
                                                coroutineScope.launch {  // 🔹 FIX: Call suspend function in coroutine
                                                    householdRepository.addGroceryListToHousehold(
                                                        selectedHousehold!!.id,
                                                        documentReference.id
                                                    )
                                                }
                                            }

                                            Toast.makeText(context, "List created", Toast.LENGTH_SHORT).show()
                                            navController.navigate("myLists") {
                                                popUpTo("home") { inclusive = false }
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                },
                                onMyListsTabClick = {
                                    navController.navigate("myLists")
                                },
                                onHouseholdTabClick = {
                                    navController.navigate("household")
                                },
                                // Pass households data to UI
                                households = households.map { it.householdName } as List<String>,
                                selectedHousehold = selectedHousehold?.householdName ?: "Personal List",
                                onHouseholdSelected = { householdName ->
                                    selectedHousehold = households.find { it.householdName == householdName }
                                },
                                dropdownExpanded = dropdownExpanded,
                                onDropdownExpandedChange = { expanded ->
                                    dropdownExpanded = expanded
                                }
                            )
                        }
                        composable("household") {
                            HouseholdScreenUi(
                                navController = navController
                            )
                        }





                        composable("myLists") {
                            MyListsRoute(
                                onBackClick = { navController.popBackStack() },
                                onOpenList = { list ->
                                    navController.navigate("listInfo/${list.id}")
                                },
                                onEditList = { list ->
                                    // For now, navigate to list info which can handle editing
                                    navController.navigate("listInfo/${list.id}")
                                },
                                onHouseholdTabClick = {
                                    navController.navigate("household")
                                }
                            )
                        }



                        composable("household") {
                            HouseholdScreenUi(
                                navController = navController
                            )
                        }

                        composable("stores") {
                            StoreListScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }


                        composable("settings") {
                            SettingsScreen(
                                onBackClick = { navController.popBackStack() }
                            )
                        }


                        composable("listInfo/{listId}") { backStackEntry ->
                            val listId = backStackEntry.arguments?.getString("listId") ?: return@composable

                            ListInfoScreenUi(
                                listId = listId,
                                onBackClick = { navController.popBackStack() },
                                db = FirebaseFirestore.getInstance()
                            )
                        }



                    }
                }
            }
        }
    }
}