package week11.st339556.finalProject.data

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st339556.finalProject.model.GroceryStore

class GroceryStoreRepository {

    //TODO- link to user

    private val db = FirebaseFirestore.getInstance()
    private val groceryRef = db.collection("groceryStores")

    // create grocery store to add
    suspend fun addGroceryStore(store: GroceryStore): String {
        return try {
            groceryRef.add(store).await()
            "Store created"
        } catch (e: Exception) {
            "Could not create store: ${e.localizedMessage}"
        }
    }

    //get all stores
    fun getAllGroceryStores(): Flow<List<GroceryStore>> = callbackFlow {
        val listener = groceryRef.addSnapshotListener { snapshot, _ ->
            val list = snapshot?.toObjects(GroceryStore::class.java) ?: emptyList()
            trySend(list)
        }
        awaitClose { listener.remove() }
    }

    //update store details
    suspend fun updateGroceryStore(store: GroceryStore): String {
        return try {
            if (store.groceryStoreId.isEmpty()) return "Store ID missing"
            groceryRef.document(store.groceryStoreId).set(store).await()
            "Store updated"
        } catch (e: Exception) {
            "Could not update store: ${e.localizedMessage}"
        }
    }

    //delete store
    suspend fun deleteGroceryStore(storeId: String): String {
        return try {
            groceryRef.document(storeId).delete().await()
            "Store deleted"
        } catch (e: Exception) {
            "Could not delete store: ${e.localizedMessage}"
        }
    }
}