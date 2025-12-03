package week11.st339556.finalProject.model

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint

data class GroceryStore(
    val groceryName: String? = "",
    val groceryLocation: GeoPoint? = null, // Added geolocation

    @DocumentId
    val groceryStoreId: String = ""
)