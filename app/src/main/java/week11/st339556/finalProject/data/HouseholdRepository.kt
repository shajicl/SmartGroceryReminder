package week11.st339556.finalProject.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import week11.st339556.finalProject.model.Household

class HouseholdRepository  {

    private val db = FirebaseFirestore.getInstance()
    private val householdRef = db.collection("households")

    // Add a new household
    suspend fun addHousehold(household: Household): String {
        return try {
            val docRef = householdRef.add(household).await()
            docRef.id
        } catch (e: Exception) {
            "Could not create household: ${e.localizedMessage}"
        }
    }

    // Get all households (admin purposes)
    fun getAllHouseholds(): Flow<List<Household>> = callbackFlow {
        val listener = householdRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(emptyList())
                return@addSnapshotListener
            }
            val list = snapshot?.toObjects(Household::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    // Get households for a specific user
    fun getUserHouseholds(userId: String): Flow<List<Household>> = callbackFlow {
        val listener = householdRef
            .whereArrayContains("userIds", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }
                val households = snapshot?.toObjects(Household::class.java) ?: emptyList()
                trySend(households)
            }
        awaitClose { listener.remove() }
    }

    // Get a single household by ID
    suspend fun getHouseholdById(householdId: String): Household? {
        return try {
            val snapshot = householdRef.document(householdId).get().await()
            snapshot.toObject(Household::class.java)?.copy(id = householdId)
        } catch (e: Exception) {
            null
        }
    }

    // Update household
    suspend fun updateHousehold(household: Household): String {
        return try {
            if (household.id.isEmpty()) return "Household ID missing"
            householdRef.document(household.id).set(household).await()
            "Household updated"
        } catch (e: Exception) {
            "Could not update household: ${e.localizedMessage}"
        }
    }

    // Delete household
    suspend fun deleteHousehold(householdId: String): String {
        return try {
            householdRef.document(householdId).delete().await()
            "Household deleted"
        } catch (e: Exception) {
            "Could not delete household: ${e.localizedMessage}"
        }
    }

    // Ensure user has at least one household (create default if not)
    suspend fun ensureUserHousehold(userId: String): Household? {
        return try {
            // Get all households where the userId is in the userIds list
            val snapshot = householdRef
                .whereArrayContains("userIds", userId)
                .limit(1)
                .get()
                .await()

            val households = snapshot.toObjects(Household::class.java)

            if (households.isNotEmpty()) {
                households.first().copy(id = snapshot.documents.first().id)
            } else {
                // No household found for this user, create a default one
                val newHousehold = Household(
                    id = "", // Will be set by Firestore
                    householdName = "My Household",
                    userIds = listOf(userId),
                    groceryListIds = emptyList(),
                    creatorId = userId
                )
                val docRef = householdRef.add(newHousehold).await()
                newHousehold.copy(id = docRef.id)
            }
        } catch (e: Exception) {
            null
        }
    }

    // Add a user to a household
    suspend fun addUserToHousehold(householdId: String, userId: String): String {
        return try {
            val household = getHouseholdById(householdId)
            if (household == null) {
                return "Household not found"
            }

            if (household.userIds.contains(userId)) {
                return "User already in household"
            }

            val updatedUserIds = household.userIds.toMutableList().apply {
                add(userId)
            }

            householdRef.document(householdId)
                .update("userIds", updatedUserIds)
                .await()

            "User added to household"
        } catch (e: Exception) {
            "Could not add user to household: ${e.localizedMessage}"
        }
    }

    // Remove a user from a household
    suspend fun removeUserFromHousehold(householdId: String, userId: String): String {
        return try {
            val household = getHouseholdById(householdId)
            if (household == null) {
                return "Household not found"
            }

            if (!household.userIds.contains(userId)) {
                return "User not in household"
            }

            val updatedUserIds = household.userIds.toMutableList().apply {
                remove(userId)
            }

            householdRef.document(householdId)
                .update("userIds", updatedUserIds)
                .await()

            "User removed from household"
        } catch (e: Exception) {
            "Could not remove user from household: ${e.localizedMessage}"
        }
    }

    // Check if user can delete household (only creator can delete)
    suspend fun canUserDeleteHousehold(householdId: String, userId: String): Boolean {
        return try {
            val household = getHouseholdById(householdId)
            household?.creatorId == userId
        } catch (e: Exception) {
            false
        }
    }

    // Add a grocery list to household
    suspend fun addGroceryListToHousehold(householdId: String, listId: String): String {
        return try {
            val household = getHouseholdById(householdId)
            if (household == null) {
                return "Household not found"
            }

            if (household.groceryListIds.contains(listId)) {
                return "List already in household"
            }

            val updatedListIds = household.groceryListIds.toMutableList().apply {
                add(listId)
            }

            householdRef.document(householdId)
                .update("groceryListIds", updatedListIds)
                .await()

            "List added to household"
        } catch (e: Exception) {
            "Could not add list to household: ${e.localizedMessage}"
        }
    }

    // Remove a grocery list from household
    suspend fun removeGroceryListFromHousehold(householdId: String, listId: String): String {
        return try {
            val household = getHouseholdById(householdId)
            if (household == null) {
                return "Household not found"
            }

            if (!household.groceryListIds.contains(listId)) {
                return "List not in household"
            }

            val updatedListIds = household.groceryListIds.toMutableList().apply {
                remove(listId)
            }

            householdRef.document(householdId)
                .update("groceryListIds", updatedListIds)
                .await()

            "List removed from household"
        } catch (e: Exception) {
            "Could not remove list from household: ${e.localizedMessage}"
        }
    }
}