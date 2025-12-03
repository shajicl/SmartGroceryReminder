package week11.st339556.finalProject.lists


import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.DocumentId

data class MemberAvatar(
    val initials: String,
    val color: Color
)

enum class Priority { LOW, MEDIUM, HIGH }

data class GroceryList(
    @DocumentId
    val id: String = "",
    val name: String = "",
    val listName: String = "",
    val userId: String = "",
    val householdId: String? = null,
    val householdName: String? = null,
    val items: List<String> = emptyList(), // Simple string items for now
    val priority: Priority = Priority.MEDIUM,
    val dueDate: String? = null,
    val createdAt: String = "",
    val isCompleted: Boolean = false,
    val members: List<Member> = emptyList() // Add members here
)

// Member model for UI display
data class Member(
    val id: String = "",
    val name: String = "",
    val initials: String = "",
    val color: Color = Color(0xFF8E44FF)
)