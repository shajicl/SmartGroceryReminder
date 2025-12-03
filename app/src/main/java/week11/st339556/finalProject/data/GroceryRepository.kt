package week11.st339556.finalProject.data

import androidx.compose.runtime.snapshotFlow
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import week11.st339556.finalProject.model.GroceryList
import week11.st339556.finalProject.model.Item

class GroceryRepository {

    //TODO- link to user

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseFirestore.getInstance()
    private val groceryRef = db.collection("groceryLists")

    //creating a list
    suspend fun addGroceryList(
        listName: String,
        priority: Priority,
        householdId: String?,
        items: List<Item>
    ) : String {
        //val user = auth.currentUser ?: return "User not logged in"

        return try {
            val newGroceryList = GroceryList(
                listName = listName,
                priority = priority,
                householdId = householdId,
                items = items
            )

            groceryRef.add(newGroceryList).await()
            "List created"
        } catch (e: Exception) {
            "List could not be created: ${e.localizedMessage}"
        }
    }

    //get all lists
    fun getAllGroceryLists(): Flow<List<GroceryList>> = callbackFlow {
        val listener = groceryRef
            .addSnapshotListener { snapshot, _ ->
                val list = snapshot?.toObjects(GroceryList::class.java) ?: emptyList()
                trySend(list)
            }
        awaitClose { listener.remove() }
    }

    //deleting a list
    suspend fun deleteGroceryList(listId: String) : String {
        return try {
            groceryRef.document(listId).delete().await()
            "List deleted"
        } catch (e: Exception) {
            "List could not be deleted: ${e.localizedMessage}"
        }
    }

    //adding item to list
    suspend fun addItemList(listId: String, item: Item) : String {
        return try {
            val doc = groceryRef.document(listId).get().await()
            val list = doc.toObject(GroceryList::class.java)
                ?: return "List not found"

            val updatedItems = list.items + item

            groceryRef.document(listId)
                .update("items", updatedItems)
                .await()

            "Item added to list"
        } catch (e: Exception) {
            "Item could not be added to list: ${e.localizedMessage}"
        }
    }

    //deleting item from list
    suspend fun deleteItem(listId: String, itemId: String): String {
        return try {
            val doc = groceryRef.document(listId).get().await()
            val list = doc.toObject(GroceryList::class.java)
                ?: return "List not found"

            val newItems = list.items.filter { it.itemId != itemId }

            groceryRef.document(listId)
                .update("items", newItems)
                .await()

            "Item removed from list"
        } catch (e: Exception) {
            "Item could not be removed from list: ${e.localizedMessage}"
        }
    }

    //updating item in list
    suspend fun updateItem(listId: String, updatedItem: Item): String {
        return try {
            val doc = groceryRef.document(listId).get().await()
            val list = doc.toObject(GroceryList::class.java)
                ?: return "List not found"

            val newItems = list.items.map {
                if (it.itemId == updatedItem.itemId) updatedItem else it
            }

            groceryRef.document(listId)
                .update("items", newItems)
                .await()

            "Item updated"
        } catch (e: Exception) {
            "Item could not be updated: ${e.localizedMessage}"
        }
    }
}