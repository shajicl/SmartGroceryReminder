package week11.st339556.finalProject.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun SignUpScreen(
    navController: NavController,
    viewModel: AuthViewModel = viewModel()
) {
    val state by viewModel.uiState

    SignUpScreenUi(
        onCreateAccountClick = { name, email, password, householdId ->
            // Put the email + password into the ViewModel state
            viewModel.onEmailChange(email)
            viewModel.onPasswordChange(password)
            viewModel.register(name = name, householdId = householdId)

            // Call ViewModel.register – this actually talks to Firebase
            viewModel.register(name = name, householdId = householdId)
        },
        onSignInClick = {
            // Go back to login screen
            navController.popBackStack()
        }
    )

    // OPTIONAL: you can also show state.errorMessage here as a Snackbar/Toast
    // if you want global error handling.
}
