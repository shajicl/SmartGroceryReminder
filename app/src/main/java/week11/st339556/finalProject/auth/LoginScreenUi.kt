package week11.st339556.finalProject.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun LoginScreenUi(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state.isLoginSuccessful) {
        if (state.isLoginSuccessful) {
            navController.navigate("home") {
                // remove login from back stack so user can't go back to it
                popUpTo("login") { inclusive = true }
            }
            viewModel.clearLoginSuccessFlag()
        }
    }

    // Outer dark background like your mock
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF111111)),
        contentAlignment = Alignment.Center
    ) {
        // Main white rounded card
        Card(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFDFDFD)
            ),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Spacer(Modifier.height(32.dp))

                // Purple rounded icon at top
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = Color(0xFF8E44FF),
                            shape = RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "F", // placeholder logo
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    )
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    text = "Family Organizer",
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Text(
                    text = "Welcome back",
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                )

                Spacer(Modifier.height(24.dp))

                // Email field (bound to ViewModel)
                OutlinedTextField(
                    value = state.email,
                    onValueChange = viewModel::onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Email") },
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(12.dp))

                // Password field with eye icon
                OutlinedTextField(
                    value = state.password,
                    onValueChange = viewModel::onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Password") },
                    shape = RoundedCornerShape(10.dp),
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None
                    else
                        PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Filled.VisibilityOff
                                else
                                    Icons.Filled.Visibility,
                                contentDescription = if (passwordVisible)
                                    "Hide password"
                                else
                                    "Show password"
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    )
                )

                Spacer(Modifier.height(4.dp))

                // Forgot password text on the right
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Forgot password?",
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { navController.navigate("forgotPassword") },
                        style = TextStyle(
                            color = Color(0xFF8E44FF),
                            fontSize = 13.sp
                        )
                    )
                }

                // Error message from ViewModel (if any)
                state.errorMessage?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = Color.Red,
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(20.dp))

                OutlinedButton(
                    onClick = { viewModel.login() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    border = ButtonDefaults.outlinedButtonBorder.copy(
                        width = 2.dp,
                        brush = androidx.compose.ui.graphics.SolidColor(Color(0xFF8E44FF))
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Text(
                        text = if (state.isLoading) "Logging in..." else "Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF8E44FF)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // Sign Up (filled purple button) -> navigate to signup screen
                Button(
                    onClick = { navController.navigate("signup") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF8E44FF)
                    )
                ) {
                    Text(
                        text = "Sign Up",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }



                Spacer(Modifier.height(16.dp))

                Text(
                    text = "or continue with",
                    style = TextStyle(
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                )

                Spacer(Modifier.height(16.dp))

                // Social buttons row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SocialIconButton(text = "G") { /* TODO: Google sign-in */ }
                    SocialIconButton(text = "") { /* TODO: Apple sign-in */ }
                    SocialIconButton(text = "f") { /* TODO: Facebook sign-in */ }
                }

                Spacer(Modifier.height(32.dp))

                // Terms & Privacy text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "By continuing, you agree to our",
                        style = TextStyle(
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    )
                    Row {
                        Text(
                            text = "Terms of Service",
                            modifier = Modifier.clickable { /* open link */ },
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color(0xFF8E44FF),
                                textDecoration = TextDecoration.Underline
                            )
                        )
                        Text(
                            text = " and ",
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        )
                        Text(
                            text = "Privacy Policy",
                            modifier = Modifier.clickable { /* open link */ },
                            style = TextStyle(
                                fontSize = 11.sp,
                                color = Color(0xFF8E44FF),
                                textDecoration = TextDecoration.Underline
                            )
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

