package week11.st339556.finalProject.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.launch


class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    var uiState = androidx.compose.runtime.mutableStateOf(AuthUiState())
        private set

    fun onEmailChange(newEmail: String) {
        uiState.value = uiState.value.copy(email = newEmail, errorMessage = null)
    }

    fun onPasswordChange(newPassword: String) {
        uiState.value = uiState.value.copy(password = newPassword, errorMessage = null)
    }

    private fun validateInputs(isLogin: Boolean = true): Boolean {
        val email = uiState.value.email.trim()
        val password = uiState.value.password

        if (email.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = "Email cannot be empty")
            return false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            uiState.value = uiState.value.copy(errorMessage = "Please enter a valid email")
            return false
        }
        if (password.length < 6) {
            uiState.value = uiState.value.copy(errorMessage = "Password must be at least 6 characters")
            return false
        }
        return true
    }

    fun login() {
        if (!validateInputs(isLogin = true)) return

        val email = uiState.value.email.trim()
        val password = uiState.value.password

        uiState.value = uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        uiState.value = uiState.value.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            errorMessage = null
                        )
                    } else {
                        uiState.value = uiState.value.copy(
                            isLoading = false,
                            isLoginSuccessful = false,
                            errorMessage = task.exception?.localizedMessage
                                ?: "Login failed"
                        )
                    }
                }
        }
    }

    fun register(name: String, householdId: String?) {
        if (name.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = "Name cannot be empty")
            return
        }

        if (!validateInputs(isLogin = false)) return

        val email = uiState.value.email.trim()
        val password = uiState.value.password

        uiState.value = uiState.value.copy(isLoading = true, errorMessage = null)

        viewModelScope.launch {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser

                        // ✅ This is what makes the name appear in Firebase Authentication console
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()

                        user?.updateProfile(profileUpdates)

                        // (Optional) if later you use Firestore, you can also save householdId there

                        uiState.value = uiState.value.copy(
                            isLoading = false,
                            isLoginSuccessful = true,
                            errorMessage = null
                        )
                    } else {
                        uiState.value = uiState.value.copy(
                            isLoading = false,
                            isLoginSuccessful = false,
                            errorMessage = task.exception?.localizedMessage
                                ?: "Registration failed"
                        )
                    }
                }
        }
    }

    fun sendPasswordReset(onEmailSent: () -> Unit) {
        val email = uiState.value.email.trim()
        if (email.isBlank()) {
            uiState.value = uiState.value.copy(errorMessage = "Enter your email first")
            return
        }

        uiState.value = uiState.value.copy(isLoading = true, errorMessage = null)

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                uiState.value = uiState.value.copy(isLoading = false)
                if (task.isSuccessful) {
                    onEmailSent()
                } else {
                    uiState.value = uiState.value.copy(
                        errorMessage = task.exception?.localizedMessage
                            ?: "Failed to send reset email"
                    )
                }
            }
    }

    fun clearLoginSuccessFlag() {
        uiState.value = uiState.value.copy(isLoginSuccessful = false)
    }
}


