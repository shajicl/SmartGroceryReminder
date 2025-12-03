package week11.st339556.finalProject.model

import com.google.firebase.firestore.DocumentId

data class Household (
    val householdName: String? = "",
    val userIds: List<String> = emptyList(),
    val groceryListIds: List<String> = emptyList(),
    val creatorId: String = "",
    val createdAt: String = "" ,

    @DocumentId
    val id: String = ""

)

