package week11.st339556.finalProject.auth

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import week11.st339556.finalProject.model.Household
import week11.st339556.finalProject.model.HouseholdViewModel
import androidx.compose.runtime.rememberCoroutineScope

private val PurpleMain = Color(0xFF8E44FF)
private val PurpleLightBg = Color(0xFFF4ECFF)
private val TextFieldBg = Color(0xFFF5F5F5)

@Composable
fun SignUpScreenUi(
    onSignUpSuccess: (userId: String, householdId: String) -> Unit = { _, _ -> },
    onSignInClick: () -> Unit = {}
) {
    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val coroutineScope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var householdId by remember { mutableStateOf("") }
    var householdName by remember { mutableStateOf("") }

    var joinExisting by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var householdIdError by remember { mutableStateOf<String?>(null) }

    val householdViewModel: HouseholdViewModel = viewModel()

    fun validate(): Boolean {
        var hasError = false

        // Clear previous errors
        nameError = null
        emailError = null
        passwordError = null
        confirmPasswordError = null
        householdIdError = null

        if (name.isBlank()) {
            nameError = "Name required";
            hasError = true
        }

        if (email.isBlank()) {
            emailError = "Email required";
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Invalid email";
            hasError = true
        }

        if (password.length < 6) {
            passwordError = "At least 6 characters";
            hasError = true
        }

        if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match";
            hasError = true
        }

        // Validate household fields
        if (joinExisting && householdId.isBlank()) {
            householdIdError = "Household ID required"
            hasError = true
        }

        return !hasError
    }

    suspend fun createNewHousehold(userId: String): String {
        val finalName = if (householdName.isNotBlank()) householdName else "$name's Household"

        val doc = db.collection("households").document() // generate ID

        val household = Household(
            id = doc.id,
            householdName = finalName,
            creatorId = userId,
            userIds = listOf(userId),
            groceryListIds = emptyList()
        )

        doc.set(household).await()
        return doc.id
    }

    suspend fun joinExistingHousehold(userId: String, joinId: String): String {
        val doc = db.collection("households").document(joinId).get().await()
        if (!doc.exists()) {
            throw Exception("Household ID does not exist. Please check the ID and try again.")
        }

        // Check if user is already in the household
        val existingUserIds = doc.get("userIds") as? List<String> ?: emptyList()
        if (existingUserIds.contains(userId)) {
            throw Exception("You are already a member of this household.")
        }

        db.collection("households").document(joinId)
            .update("userIds", FieldValue.arrayUnion(userId))
            .await()

        return joinId
    }

    fun handleSignUp() {
        if (!validate()) return

        isLoading = true

        // Firebase create user
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val firebaseUser = result.user
                if (firebaseUser == null) {
                    Toast.makeText(context, "Error creating user", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    return@addOnSuccessListener
                }

                val userId = firebaseUser.uid

                // Save basic user profile
                val userData = mapOf(
                    "uid" to userId,
                    "name" to name,
                    "email" to email,
                    "createdAt" to com.google.firebase.Timestamp.now()
                )

                db.collection("users").document(userId).set(userData)
                    .addOnSuccessListener {
                        // Now create or join household using coroutine scope
                        coroutineScope.launch {
                            try {
                                val finalHouseholdId = if (joinExisting && householdId.isNotBlank()) {
                                    joinExistingHousehold(userId, householdId)
                                } else {
                                    createNewHousehold(userId)
                                }

                                // Also save household reference to user document
                                db.collection("users").document(userId)
                                    .update("householdId", finalHouseholdId)
                                    .await()

                                // Call success callback on main thread
                                onSignUpSuccess(userId, finalHouseholdId)

                            } catch (e: Exception) {
                                e.printStackTrace()
                                // Delete the user since household creation/joining failed
                                auth.currentUser?.delete()?.await()

                                // Show error message
                                with(kotlinx.coroutines.Dispatchers.Main) {
                                    Toast.makeText(context, e.message ?: "Failed to setup household", Toast.LENGTH_LONG).show()
                                    isLoading = false
                                }
                            }
                        }
                    }
                    .addOnFailureListener { e ->
                        // Delete the Firebase auth user if user document creation failed
                        auth.currentUser?.delete()
                        Toast.makeText(context, "Failed to save user profile: ${e.message}", Toast.LENGTH_SHORT).show()
                        isLoading = false
                    }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Sign up failed: ${e.message}", Toast.LENGTH_SHORT).show()
                isLoading = false
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleLightBg),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Top circular icon with cart
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(
                            color = PurpleMain,
                            shape = RoundedCornerShape(32.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🛒",
                        fontSize = 28.sp
                    )
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Create Account",
                    style = TextStyle(
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PurpleMain
                    )
                )
                Text(
                    text = "Join Smart Grocery Reminder",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                )

                Spacer(Modifier.height(24.dp))

                // ----- Name -----
                Text(
                    text = "Name",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Start
                )
                TextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Your name") },
                    shape = RoundedCornerShape(8.dp),
                    isError = nameError != null,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextFieldBg,
                        unfocusedContainerColor = TextFieldBg,
                        focusedIndicatorColor = if (nameError != null) Color.Red else PurpleMain,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    enabled = !isLoading
                )
                nameError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ----- Email -----
                Text(
                    text = "Email",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Start
                )
                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        emailError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("your.email@example.com") },
                    shape = RoundedCornerShape(8.dp),
                    isError = emailError != null,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextFieldBg,
                        unfocusedContainerColor = TextFieldBg,
                        focusedIndicatorColor = if (emailError != null) Color.Red else PurpleMain,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    enabled = !isLoading
                )
                emailError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ----- Password -----
                Text(
                    text = "Password",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Start
                )
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("At least 6 characters") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp),
                    isError = passwordError != null,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextFieldBg,
                        unfocusedContainerColor = TextFieldBg,
                        focusedIndicatorColor = if (passwordError != null) Color.Red else PurpleMain,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    enabled = !isLoading
                )
                passwordError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ----- Confirm Password -----
                Text(
                    text = "Confirm Password",
                    modifier = Modifier.fillMaxWidth(),
                    style = TextStyle(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Start
                )
                TextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        confirmPasswordError = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("Re-enter your password") },
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(8.dp),
                    isError = confirmPasswordError != null,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = TextFieldBg,
                        unfocusedContainerColor = TextFieldBg,
                        focusedIndicatorColor = if (confirmPasswordError != null) Color.Red else PurpleMain,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    enabled = !isLoading
                )
                confirmPasswordError?.let {
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 11.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(12.dp))

                // ----- Household Options -----
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = PurpleLightBg.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "Household Setup",
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = PurpleMain
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilterChip(
                                selected = !joinExisting,
                                onClick = {
                                    if (!isLoading) {
                                        joinExisting = false
                                        householdId = ""
                                        householdIdError = null
                                    }
                                },
                                label = { Text("Create New") },
                                enabled = !isLoading
                            )
                            FilterChip(
                                selected = joinExisting,
                                onClick = {
                                    if (!isLoading) {
                                        joinExisting = true
                                        householdName = ""
                                    }
                                },
                                label = { Text("Join Existing") },
                                enabled = !isLoading
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        if (joinExisting) {
                            // Join existing household
                            Text(
                                text = "Household ID",
                                modifier = Modifier.fillMaxWidth(),
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                textAlign = TextAlign.Start
                            )
                            TextField(
                                value = householdId,
                                onValueChange = {
                                    householdId = it
                                    householdIdError = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Enter household ID") },
                                shape = RoundedCornerShape(8.dp),
                                isError = householdIdError != null,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = TextFieldBg,
                                    unfocusedContainerColor = TextFieldBg,
                                    focusedIndicatorColor = if (householdIdError != null) Color.Red else PurpleMain,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                enabled = !isLoading
                            )
                            householdIdError?.let {
                                Text(
                                    text = it,
                                    color = Color.Red,
                                    fontSize = 11.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            Text(
                                text = "Ask your household admin for the ID",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Create new household
                            Text(
                                text = "Household Name",
                                modifier = Modifier.fillMaxWidth(),
                                style = TextStyle(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                textAlign = TextAlign.Start
                            )
                            TextField(
                                value = householdName,
                                onValueChange = { householdName = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("e.g., Smith Family Home") },
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = TextFieldBg,
                                    unfocusedContainerColor = TextFieldBg,
                                    focusedIndicatorColor = PurpleMain,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedTextColor = Color.Black,
                                    unfocusedTextColor = Color.Black
                                ),
                                enabled = !isLoading
                            )
                            Text(
                                text = "Leave blank for default: \"Your Name's Household\"",
                                style = TextStyle(
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // ----- Create Account button -----
                Button(
                    onClick = { handleSignUp() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleMain
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Create Account",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Bottom "Already have an account? Sign In"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = Color.Gray
                        )
                    )
                    Text(
                        text = "Sign In",
                        modifier = Modifier.clickable(enabled = !isLoading) { onSignInClick() },
                        style = TextStyle(
                            fontSize = 13.sp,
                            color = PurpleMain,
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = TextDecoration.Underline
                        )
                    )
                }
            }
        }
    }
}