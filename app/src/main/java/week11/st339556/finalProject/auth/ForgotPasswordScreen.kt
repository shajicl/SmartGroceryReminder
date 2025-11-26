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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel

private val PurpleMain = Color(0xFF8E44FF)
private val PurpleLightBg = Color(0xFFF4ECFF)   // light purple background
private val TextFieldBg = Color(0xFFF5F5F5)

@Composable
fun ForgotPasswordScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState
    var successMsg by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PurpleLightBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Title
                Text(
                    text = "Reset Password",
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text = "Enter your email and we'll send you a reset link.",
                    style = TextStyle(fontSize = 14.sp, color = Color.Gray),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(24.dp))

                // Email text field
                OutlinedTextField(
                    value = state.email,
                    onValueChange = {
                        // update ViewModel + clear old messages
                        viewModel.onEmailChange(it)
                        successMsg = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Email") },
                    shape = RoundedCornerShape(10.dp),
                    isError = state.errorMessage != null,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = TextFieldBg,
                        focusedContainerColor = TextFieldBg,
                        focusedBorderColor = PurpleMain,
                        unfocusedBorderColor = Color.Gray.copy(alpha = 0.4f),
                        cursorColor = PurpleMain
                    )
                )

                Spacer(Modifier.height(8.dp))

                // Error message (from ViewModel validation / Firebase)
                state.errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp
                    )
                }

                // Success message
                successMsg?.let {
                    Text(
                        text = it,
                        color = PurpleMain,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Reset Button
                Button(
                    onClick = {
                        // ViewModel already validates empty email + calls Firebase
                        viewModel.sendPasswordReset {
                            successMsg = "Password reset email sent. Check your inbox."
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleMain
                    ),
                    enabled = !state.isLoading
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Send Reset Link")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Back to login
                Text(
                    text = "Back to Login",
                    modifier = Modifier.clickable {
                        navController.popBackStack()
                    },
                    style = TextStyle(
                        fontSize = 14.sp,
                        color = PurpleMain,
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}
