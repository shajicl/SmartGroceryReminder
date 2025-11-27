package week11.st339556.finalProject.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st339556.finalProject.data.GroceryRepository
import week11.st339556.finalProject.data.Priority

class GroceryListViewModel : ViewModel() {

    private val repo = GroceryRepository()

    private val _lists = MutableStateFlow<List<GroceryList>>(emptyList())
    val lists: StateFlow<List<GroceryList>> = _lists

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        observeLists()
    }

    private fun observeLists() {
        viewModelScope.launch {
            repo.getAllGroceryLists().collect { list ->
                _lists.value = list
            }
        }
    }

    //creating a list
    fun addGroceryList(
        listName: String,
        household: String?,
        items: List<Item>,
        priority: Priority
    ){
        viewModelScope.launch {
            _message.value = repo.addGroceryList(listName, priority, household, items)
        }
    }

    //deleting a list
    fun deleteGroceryList(listId: String){
        viewModelScope.launch {
            _message.value = repo.deleteGroceryList(listId)
        }
    }

    //adding item to list
    fun addItem(
        listId: String,
        item: Item
    ){
        viewModelScope.launch {
            _message.value = repo.addItemList(listId, item)
        }
    }

    //editing item from list
    fun editItem(
        listId: String,
        updatedItem: Item
    ){
        viewModelScope.launch {
            _message.value = repo.updateItem(listId, updatedItem)
        }
    }

    //deleting item from list
    fun deleteItem(
        listId: String,
        itemId: String
    ){
        viewModelScope.launch {
            _message.value = repo.deleteItem(listId, itemId)
        }
    }

    fun clearMessage() { _message.value = null }
}