package week11.st339556.finalProject.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import week11.st339556.finalProject.data.HouseholdRepository

class HouseholdViewModel : ViewModel() {

    private val repo = HouseholdRepository()

    private val _households = MutableStateFlow<List<Household>>(emptyList())
    val households: StateFlow<List<Household>> = _households

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message

    init {
        observeHouseholds()
    }

    private fun observeHouseholds() {
        viewModelScope.launch {
            repo.getAllHouseholds().collect { list ->
                _households.value = list
            }
        }
    }

    fun addHousehold(household: Household) {
        viewModelScope.launch {
            _message.value = repo.addHousehold(household)
        }
    }

    fun updateHousehold(household: Household) {
        viewModelScope.launch {
            _message.value = repo.updateHousehold(household)
        }
    }

    fun deleteHousehold(householdId: String) {
        viewModelScope.launch {
            _message.value = repo.deleteHousehold(householdId)
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}