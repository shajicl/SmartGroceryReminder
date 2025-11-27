package week11.st339556.finalProject.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st339556.finalProject.model.Household

class HouseholdRepository {

    //TODO- link to user

    private val db = FirebaseFirestore.getInstance()
    private val householdRef = db.collection("households")

    // adding a new household
    suspend fun addHousehold(household: Household): String {
        return try {
            val docRef = householdRef.add(household).await()
            docRef.id // return the actual Firestore document ID
        } catch (e: Exception) {
            "Could not create household: ${e.localizedMessage}"
        }
    }


    //list all households
    fun getAllHouseholds(): Flow<List<Household>> = callbackFlow {
        val listener = householdRef.addSnapshotListener { snapshot, _ ->
            val list = snapshot?.toObjects(Household::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    //update households
    suspend fun updateHousehold(household: Household): String {
        return try {
            if (household.id.isEmpty()) return "Household ID missing"
            householdRef.document(household.id).set(household).await()
            "Household updated"
        } catch (e: Exception) {
            "Could not update household: ${e.localizedMessage}"
        }
    }

    //delete household
    //TODO - only creator can delete household
    suspend fun deleteHousehold(householdId: String): String {
        return try {
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
            val households = snapshot.toObjects(Household::class.java)

            if (households.isNotEmpty()) {
                households.first()
            } else {
                // No household found for this user, create a default one
                val newHousehold = Household(
                    householdName = "My Household",
                    userIds = listOf(userId),
                    groceryListIds = emptyList()
                )
                val docRef = householdRef.add(newHousehold).await()
                newHousehold.copy(id = docRef.id)
            }
        } catch (e: Exception) {
            null
        }
    }


   suspend fun getUserHouseholds(userId: String): Flow<List<Household>> = callbackFlow {
        val listener = householdRef
            .whereArrayContains("userIds", userId)
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(Household::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }


}