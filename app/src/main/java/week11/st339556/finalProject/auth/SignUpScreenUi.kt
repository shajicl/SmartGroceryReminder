package week11.st339556.finalProject.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import week11.st339556.finalProject.model.Household
import week11.st339556.finalProject.model.HouseholdViewModel

// Same purple theme as Login / Forgot Password
private val PurpleMain = Color(0xFF8E44FF)
private val PurpleLightBg = Color(0xFFF4ECFF)
private val TextFieldBg = Color(0xFFF5F5F5)

@Composable
fun SignUpScreenUi(
    onSignUpSuccess: (userId: String, householdId: String) -> Unit = { _, _ -> },
    onSignInClick: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var householdId by remember { mutableStateOf("") }
    var householdName by remember { mutableStateOf("") }

    // per-field error messages
    var nameError by remember { mutableStateOf<String?>(null) }
    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    // ViewModel for household operations
    val householdViewModel: HouseholdViewModel = viewModel()
    val coroutineScope = rememberCoroutineScope()

    fun validate(): Boolean {
        var hasError = false

        if (name.isBlank()) {
            nameError = "Name is required"
            hasError = true
        }
        if (email.isBlank()) {
            emailError = "Email is required"
            hasError = true
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError = "Enter a valid email"
            hasError = true
        }
        if (password.length < 6) {
            passwordError = "Password must be at least 6 characters"
            hasError = true
        }
        if (confirmPassword != password) {
            confirmPasswordError = "Passwords do not match"
            hasError = true
        }

        return !hasError
    }

    fun handleSignUp() {
        if (validate()) {
            // Here you would normally:
            // 1. Create user with Firebase Auth
            // 2. Create household for the user
            // 3. Call onSignUpSuccess with user ID and household ID

            // For now, let's simulate user creation and household creation
            coroutineScope.launch {
                // Simulate user creation (in real app, this would be Firebase Auth)
                val simulatedUserId = "user_${System.currentTimeMillis()}"

                // Create household
                val household = if (householdId.isNotBlank()) {
                    // Join existing household (you'd need to verify it exists)
                    Household(
                        householdName = "Joined Household", // You'd fetch this name
                        userIds = listOf(simulatedUserId),
                        creatorId = simulatedUserId
                    )
                } else {
                    // Create new household
                    val newHouseholdName = if (householdName.isNotBlank()) householdName else "$name's Household"
                    Household(
                        householdName = newHouseholdName,
                        userIds = listOf(simulatedUserId),
                        creatorId = simulatedUserId
                    )
                }

                // Add household using your repository
                householdViewModel.addHousehold(household)

                // Call success callback (in real app, wait for household to be created)
                onSignUpSuccess(simulatedUserId, household.id)
            }
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
                        unfocusedIndicatorColor = Color.Transparent
                    )
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
                        unfocusedIndicatorColor = Color.Transparent
                    )
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
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedIndicatorColor = if (passwordError != null) Color.Red else PurpleMain,
                        unfocusedIndicatorColor = Color.Transparent
                    )
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
                        unfocusedIndicatorColor = Color.Transparent
                    )
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
                var joinExisting by remember { mutableStateOf(false) }

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
                                onClick = { joinExisting = false },
                                label = { Text("Create New") }
                            )
                            FilterChip(
                                selected = joinExisting,
                                onClick = { joinExisting = true },
                                label = { Text("Join Existing") }
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
                                onValueChange = { householdId = it },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                placeholder = { Text("Enter household ID") },
                                shape = RoundedCornerShape(8.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = PurpleMain,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
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
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    focusedIndicatorColor = PurpleMain,
                                    unfocusedIndicatorColor = Color.Transparent
                                )
                            )
                            Text(
                                text = "You can change this later",
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
                    )
                ) {
                    Text(
                        text = "Create Account",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
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
                        modifier = Modifier.clickable { onSignInClick() },
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