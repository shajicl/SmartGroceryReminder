package week11.st339556.finalProject.model

import com.google.firebase.firestore.DocumentId

data class Item (
    val itemName : String? = "",
    val itemQty: Int? = 0,
    val itemBrand: String? = "", //optional

    @DocumentId
    val itemId: String = ""
)