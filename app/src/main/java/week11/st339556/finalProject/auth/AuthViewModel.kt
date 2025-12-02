package week11.st339556.finalProject.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import week11.st339556.finalProject.model.Household

class AuthViewModel(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

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
            try {
                // 1. Create user in Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val user = authResult.user ?: throw Exception("User creation failed")

                // 2. Update user profile with name
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(name)
                    .build()

                user.updateProfile(profileUpdates).await()

                // 3. Create or join household
                val finalHouseholdId = if (householdId != null && householdId.isNotBlank()) {
                    // Join existing household
                    joinExistingHousehold(user.uid, householdId)
                } else {
                    // Create new household
                    createNewHousehold(user.uid, name)
                }

                // 4. Save user info to Firestore
                saveUserToFirestore(user.uid, name, email, finalHouseholdId)

                uiState.value = uiState.value.copy(
                    isLoading = false,
                    isLoginSuccessful = true,
                    errorMessage = null,
                    householdId = finalHouseholdId
                )

            } catch (e: Exception) {
                uiState.value = uiState.value.copy(
                    isLoading = false,
                    isLoginSuccessful = false,
                    errorMessage = e.localizedMessage ?: "Registration failed"
                )
            }
        }
    }

    // FIXED: Proper Firestore update for joining household
    private suspend fun joinExistingHousehold(userId: String, householdId: String): String {
        return try {
            // Fetch the household document
            val householdDoc = db.collection("households").document(householdId).get().await()

            if (!householdDoc.exists()) {
                throw Exception("Household not found. Please check the ID.")
            }

            // Get current userIds - handle different possible types
            val currentUserIds: List<String> = when (val field = householdDoc.get("userIds")) {
                is List<*> -> field.filterIsInstance<String>()
                is ArrayList<*> -> field.filterIsInstance<String>()
                else -> emptyList()
            }

            // Create new list with added user
            val updatedUserIds = currentUserIds.toMutableList().apply {
                if (!contains(userId)) {
                    add(userId)
                }
            }

            // Update household with new user
            // Use set() with merge instead of update() to avoid type issues
            db.collection("households").document(householdId).set(
                mapOf(
                    "userIds" to updatedUserIds,
                    "updatedAt" to com.google.firebase.Timestamp.now()
                ),
                com.google.firebase.firestore.SetOptions.merge()
            ).await()

            householdId

        } catch (e: Exception) {
            // If joining fails, create a new household instead
            createNewHousehold(userId, "My Household")
        }
    }

    // Helper function to create new household
    private suspend fun createNewHousehold(userId: String, name: String): String {
        val householdName = if (name.isNotBlank()) "$name's Household" else "My Household"

        val household = Household(
            householdName = householdName,
            userIds = listOf(userId),
            creatorId = userId
        )

        // Add household to Firestore
        val docRef = db.collection("households").add(household).await()

        // Return the created household ID
        return docRef.id
    }

    // Save user info to Firestore for easier queries
    private suspend fun saveUserToFirestore(
        userId: String,
        name: String,
        email: String,
        householdId: String
    ) {
        val userData = hashMapOf(
            "name" to name,
            "email" to email,
            "householdId" to householdId,
            "createdAt" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(userId).set(userData).await()
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