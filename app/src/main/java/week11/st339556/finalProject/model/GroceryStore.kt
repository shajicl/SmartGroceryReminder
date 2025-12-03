package week11.st339556.finalProject.model

import com.google.firebase.firestore.DocumentId

data class GroceryStore (
    val groceryName: String? = "",
    //TODO - needs to be added with geolocation
    val groceryLocation: String? = "",

    @DocumentId
    val groceryStoreId: String = ""
)