package com.sachin.app.whatsclean.ui.sortby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sachin.app.whatsclean.data.SettingsManager
import com.sachin.app.whatsclean.data.model.SortType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SortTypeViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val sortTypeFlow: Flow<SortType> = settingsManager.getSortType()
    private val _selectedSortTypeFlow = MutableStateFlow(SortType.NEWEST_FIRST)
    val selectedSortTypeFlow = _selectedSortTypeFlow.asStateFlow()

    fun onSortTypeChanged(sortType: SortType) {
        _selectedSortTypeFlow.value = sortType
        settingsManager.setSortType(sortType)
    }

    init {
        viewModelScope.launch {
            sortTypeFlow.collect {
                _selectedSortTypeFlow.value = it
            }
        }
    }
}