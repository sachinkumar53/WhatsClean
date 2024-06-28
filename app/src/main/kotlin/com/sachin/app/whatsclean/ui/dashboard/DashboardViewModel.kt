package com.sachin.app.whatsclean.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.sachin.app.whatsclean.data.model.Card
import com.sachin.app.whatsclean.data.model.MediaType
import com.sachin.app.whatsclean.data.repositories.DashboardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import il.co.theblitz.observablecollections.lists.ObservableLinkedList
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "DashboardViewModel"

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: DashboardRepository
) : ViewModel() {

    var lastScrollPosition: Int = 0
    val loadStatus = repository.getLoadStatus().asLiveData()
    val totalSizeAndCount = repository.getTotalSizeAndCount().asLiveData()
    val cardList = repository.getCards().asLiveData()

    fun refresh() = repository.reloadAllFiles()

    val selectedCardList = ObservableLinkedList<Card>()


    fun isCardSelected(card: Card): Boolean {
        return selectedCardList.find { it.type == card.type } != null
    }

    fun toggleCardSelection(card: Card): Boolean {
        return if (isCardSelected(card)) {
            selectedCardList.remove(card)
            false
        } else {
            selectedCardList.add(card)
            true
        }
    }

    fun deleteSelectedCards(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllFilesOfType(selectedCardList.map { it.type })
            onComplete()
        }
    }

    fun deleteCardOfType(type: MediaType, onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllFilesOfType(listOf(type))
            onComplete()
        }
    }

    fun clearCardSelection() {
        selectedCardList.clear()
    }
}