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
            householdRef.add(household).await()
            "Household created"
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
            if (household.householdId.isEmpty()) return "Household ID missing"
            householdRef.document(household.householdId).set(household).await()
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
}