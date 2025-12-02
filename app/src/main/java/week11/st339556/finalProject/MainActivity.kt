package week11.st339556.finalProject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import week11.st339556.finalProject.home.HomeScreenUi
import week11.st339556.finalProject.lists.CreateListScreenUi
import week11.st339556.finalProject.lists.ListInfoScreenUi
import week11.st339556.finalProject.lists.MyListsRoute
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
                            SignUpScreen(navController = navController)

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
<<<<<<< Updated upstream
                                onHouseholdClick = { /* TODO */ },
                                onStoresClick = { /* TODO */ },
                                onSettingsClick = { /* TODO */ },
=======
                                onHouseholdClick = { navController.navigate("household") },
                                onStoresClick = { navController.navigate("stores")},
                                onSettingsClick = { navController.navigate("settings") },
>>>>>>> Stashed changes
                                onViewProfileClick = { /* TODO */ }
                            )
                        }

                        composable("createList") {
                            val context = LocalContext.current
                            val db = FirebaseFirestore.getInstance()
                            val auth = FirebaseAuth.getInstance()

                            CreateListScreenUi(
                                onBackClick = { navController.popBackStack() },
                                onCreateListClick = { listName, household, items, priority, dueDate ->

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

                                    val listData = hashMapOf(
                                        "userId" to user.uid,
                                        "name" to listName,
                                        "household" to household,
                                        "items" to items, // List<String>
                                        "priority" to priority, // "Low"/"Medium"/"High"
                                        "dueDate" to (dueDate ?: "")
                                    )

                                    db.collection("lists")
                                        .add(listData)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "List created", Toast.LENGTH_SHORT).show()
                                            // go to My Lists after success
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
                                    // later
                                }
                            )
                        }

                        composable("myLists") {
                            MyListsRoute(
                                onBackClick = { navController.popBackStack() },
                                onOpenList = { list ->
                                    // open in read-only or edit screen
                                    navController.navigate("listInfo/${list.id}")
                                },
                                onEditList = { list ->
                                    navController.navigate("listInfo/${list.id}")
                                },
                                onHouseholdTabClick = {
                                    // later
                                }
                            )
                        }

<<<<<<< Updated upstream
=======


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


>>>>>>> Stashed changes
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
