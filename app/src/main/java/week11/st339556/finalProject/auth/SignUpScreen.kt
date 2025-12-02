package week11.st339556.finalProject.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue

import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import android.widget.Toast

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState
    val context = LocalContext.current

    // Handle registration success
    LaunchedEffect(state.isLoginSuccessful) {
        if (state.isLoginSuccessful) {
            Toast.makeText(
                context,
                "Account created successfully!",
                Toast.LENGTH_SHORT
            ).show()

            // Navigate to home screen
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    // Handle errors
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
        }
    }

    SignUpScreenUi(
        onSignUpSuccess = { userId, householdId ->


            Toast.makeText(
                context,
                "Account created! User ID: $userId, Household ID: $householdId",
                Toast.LENGTH_SHORT
            ).show()

            // Navigate to home
            navController.navigate("home") {
                popUpTo("login") { inclusive = true }
            }
        },
        onSignInClick = {
            navController.popBackStack()
        }
    )
}