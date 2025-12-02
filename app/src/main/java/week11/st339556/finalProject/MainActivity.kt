package week11.st339556.finalProject

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import week11.st339556.finalProject.auth.ForgotPasswordScreen
import week11.st339556.finalProject.auth.SignUpScreenUi
import week11.st339556.finalProject.auth.LoginScreenUi   // <— your login screen
import week11.st339556.finalProject.auth.SignUpScreen
import week11.st339556.finalProject.grocery.StoreListScreen
import week11.st339556.finalProject.home.HomeScreenUi
import week11.st339556.finalProject.household.HouseholdScreenUi
import week11.st339556.finalProject.lists.CreateListScreenUi
import week11.st339556.finalProject.lists.GroceryList
import week11.st339556.finalProject.lists.ListInfoScreenUi
import week11.st339556.finalProject.lists.MyListsRoute
import week11.st339556.finalProject.lists.Priority


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
                                onHouseholdClick = { navController.navigate("household") },
                                onStoresClick = { navController.navigate("stores")},
                                onSettingsClick = { /* TODO */ },
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
                                    navController.navigate("household")
                                }
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
