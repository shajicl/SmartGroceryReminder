package week11.st339556.finalProject.model

import com.google.firebase.firestore.DocumentId

data class Household (
    val householdName: String? = "",
    val userIds: List<String> = emptyList(),
    val groceryListIds: List<String> = emptyList(),

    @DocumentId
    val id: String = ""

)


