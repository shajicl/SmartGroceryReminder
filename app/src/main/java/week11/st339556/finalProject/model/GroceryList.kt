package week11.st339556.finalProject.model
import com.google.firebase.firestore.DocumentId
import week11.st339556.finalProject.data.Priority

data class GroceryList (
    val listName: String? = "",
    val items: List<Item> = emptyList(),
    val priority: Priority = Priority.LOW,
    val householdId: String? = null,


    @DocumentId
    val groceryListId: String = ""
)