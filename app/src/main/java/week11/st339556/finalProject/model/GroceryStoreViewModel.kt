package week11.st339556.finalProject.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st339556.finalProject.data.GroceryStoreRepository

class GroceryStoreViewModel : ViewModel() {

    private val repo = GroceryStoreRepository()

    private val _stores = MutableStateFlow<List<GroceryStore>>(emptyList())
    val stores: StateFlow<List<GroceryStore>> = _stores

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        observeStores()
    }

    private fun observeStores() {
        viewModelScope.launch {
            repo.getAllGroceryStores().collect { list ->
                _stores.value = list
            }
        }
    }

    fun addStore(store: GroceryStore) {
        viewModelScope.launch {
            _message.value = repo.addGroceryStore(store)
        }
    }

    fun updateStore(store: GroceryStore) {
        viewModelScope.launch {
            _message.value = repo.updateGroceryStore(store)
        }
    }

    fun deleteStore(storeId: String) {
        viewModelScope.launch {
            _message.value = repo.deleteGroceryStore(storeId)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}