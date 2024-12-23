package com.proxyrack.control.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proxyrack.control.data.repository.AnalyticsStatusNotifier
import com.proxyrack.control.domain.repository.SettingsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepo: SettingsRepo,
    private val analyticsStatus: AnalyticsStatusNotifier): ViewModel() {

    private val _analyticsEnabled = MutableStateFlow(true)
    val analyticsEnabled = _analyticsEnabled.asStateFlow()

    init {
        viewModelScope.launch {
           _analyticsEnabled.emit(settingsRepo.analyticsEnabled.get() != "false")
        }
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        var text = "true"
        if (!enabled) {
            text = "false"
        }

        viewModelScope.launch(Dispatchers.IO) {
            _analyticsEnabled.emit(enabled) // updates UI
            settingsRepo.analyticsEnabled.set(text) // saves in shared prefs
            analyticsStatus.notifyStatus(enabled) // disables / enables analytics lib
        }
    }

}