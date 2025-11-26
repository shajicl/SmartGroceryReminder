package week11.st339556.finalProject.lists


import androidx.compose.ui.graphics.Color

data class MemberAvatar(
    val initials: String,
    val color: Color
)

enum class Priority { LOW, MEDIUM, HIGH }

data class GroceryList(
    val id: String,
    var name: String,
    var household: String,
    var items: MutableList<String>,
    var priority: Priority,
    var dueDate: String?,
    val members: List<MemberAvatar> = emptyList()   // optional for now
)
