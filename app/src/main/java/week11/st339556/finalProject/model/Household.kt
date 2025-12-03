package week11.st339556.finalProject.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Household(
    val householdName: String = "",
    val userIds: List<String> = emptyList(),
    val creatorId: String = "",
    val groceryListIds: List<String> = emptyList(),
    val creatorId: String = "",
    val createdAt: String = "" ,

    @ServerTimestamp
    val createdAt: Timestamp? = null,

    @ServerTimestamp
    val updatedAt: Timestamp? = null,

    @DocumentId
    val id: String = ""
) {
    fun isCreator(userId: String): Boolean {
        return creatorId == userId || (userIds.isNotEmpty() && userIds[0] == userId)
    }

    fun isMember(userId: String): Boolean {
        return userId in userIds
    }

    fun canDelete(userId: String): Boolean {
        return isCreator(userId)
    }
}