package week11.st339556.finalProject.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st339556.finalProject.model.Household

class HouseholdRepository {

    private val db = FirebaseFirestore.getInstance()
    private val householdRef = db.collection("households")
    private val auth = FirebaseAuth.getInstance()

    // Helper function to safely convert documents to Household objects
    private fun QuerySnapshot.toHouseholdList(): List<Household> {
        return this.documents.mapNotNull { document ->
            try {
                // Safely parse userIds field
                val userIds = when (val field = document.get("userIds")) {
                    is List<*> -> field.filterIsInstance<String>().toList()
                    is String -> if (field.isNotBlank()) listOf(field) else emptyList()
                    else -> emptyList()
                }

                // Safely parse groceryListIds field
                val groceryListIds = when (val field = document.get("groceryListIds")) {
                    is List<*> -> field.filterIsInstance<String>().toList()
                    else -> emptyList()
                }

                Household(
                    householdName = document.getString("householdName") ?: "",
                    userIds = userIds,
                    creatorId = document.getString("creatorId") ?: "",
                    groceryListIds = groceryListIds,
                    createdAt = document.getTimestamp("createdAt"),
                    updatedAt = document.getTimestamp("updatedAt"),
                    id = document.id // Firestore document ID
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    // Add current user ID to household
    suspend fun addHousehold(household: Household): String {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return "User not authenticated"
            }

            // Create household with creator information
            val householdToAdd = household.copy(
                creatorId = currentUser.uid,
                userIds = if (!household.userIds.contains(currentUser.uid)) {
                    household.userIds + currentUser.uid
                } else {
                    household.userIds
                }
            )

            val docRef = householdRef.add(householdToAdd).await()
            docRef.id
        } catch (e: Exception) {
            "Could not create household: ${e.localizedMessage}"
        }
    }

    // List all households - using safe converter
    fun getAllHouseholds(): Flow<List<Household>> = callbackFlow {
        val listener = householdRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Send empty list on error
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.toHouseholdList() ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // Update households - check if user is member
    suspend fun updateHousehold(household: Household): String {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return "User not authenticated"
            }

            if (household.id.isEmpty()) return "Household ID missing"

            // Verify user is a member of this household before updating
            val docRef = householdRef.document(household.id)
            val snapshot = docRef.get().await()
            val existingHousehold = snapshot.toHousehold()

            if (existingHousehold == null) {
                return "Household not found"
            }

            if (!existingHousehold.userIds.contains(currentUser.uid)) {
                return "Access denied: You are not a member of this household"
            }

            // Update the household with current timestamp
            val updatedHousehold = household.copy(
                updatedAt = com.google.firebase.Timestamp.now()
            )

            docRef.set(updatedHousehold).await()
            "Household updated"
        } catch (e: Exception) {
            "Could not update household: ${e.localizedMessage}"
        }
    }

    // Helper to convert single document
    private fun com.google.firebase.firestore.DocumentSnapshot.toHousehold(): Household? {
        return try {
            if (!exists()) return null

            // Safely parse userIds field
            val userIds = when (val field = get("userIds")) {
                is List<*> -> field.filterIsInstance<String>().toList()
                is String -> if (field.isNotBlank()) listOf(field) else emptyList()
                else -> emptyList()
            }

            // Safely parse groceryListIds field
            val groceryListIds = when (val field = get("groceryListIds")) {
                is List<*> -> field.filterIsInstance<String>().toList()
                else -> emptyList()
            }

            Household(
                householdName = getString("householdName") ?: "",
                userIds = userIds,
                creatorId = getString("creatorId") ?: "",
                groceryListIds = groceryListIds,
                createdAt = getTimestamp("createdAt"),
                updatedAt = getTimestamp("updatedAt"),
                id = id
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Delete household - only creator can delete
    suspend fun deleteHousehold(householdId: String): String {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                return "User not authenticated"
            }

            // Get the household to check permissions
            val snapshot = householdRef.document(householdId).get().await()
            val household = snapshot.toHousehold()

            if (household == null) {
                return "Household not found"
            }

            // Check if user is the creator
            if (!household.isCreator(currentUser.uid)) {
                return "Access denied: Only the household creator can delete this household"
            }

            householdRef.document(householdId).delete().await()
            "Household deleted"
        } catch (e: Exception) {
            "Could not delete household: ${e.localizedMessage}"
        }
    }

    // Ensure at least one household exists; returns the user's first household or creates one
    suspend fun ensureUserHousehold(userId: String): Household? {
        return try {
            // Get all households where the userId is in the userIds list
            val snapshot = householdRef.whereArrayContains("userIds", userId).get().await()
            val households = snapshot.toHouseholdList()

            if (households.isNotEmpty()) {
                households.first()
            } else {
                // No household found for this user, create a default one
                val newHousehold = Household(
                    householdName = "My Household",
                    userIds = listOf(userId),
                    creatorId = userId,
                    groceryListIds = emptyList()
                )
                val docRef = householdRef.add(newHousehold).await()
                newHousehold.copy(id = docRef.id)
            }
        } catch (e: Exception) {
            null
        }
    }

    // Get user households using safe converter
    fun getUserHouseholds(userId: String): Flow<List<Household>> = callbackFlow {
        val listener = householdRef
            .whereArrayContains("userIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val list = snapshot?.toHouseholdList() ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }
}