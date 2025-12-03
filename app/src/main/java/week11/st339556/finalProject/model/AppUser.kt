package week11.st339556.finalProject.model

import com.google.firebase.firestore.DocumentId

data class AppUser (
    val userEmail : String? = "",
    val userName : String? = "",

    //ustyrer can belong to a household or not
    val householdId: String? = null,

    @DocumentId
    val userId: String = ""
)