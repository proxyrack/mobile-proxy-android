package com.proxyrack.control.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proxyrack.control.data.repository.AnalyticsStatusNotifier
import com.proxyrack.control.data.repository.ConnectionRepo
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
    private val analyticsStatus: AnalyticsStatusNotifier,
    private val connectionRepo: ConnectionRepo,
    ): ViewModel() {

    private val _analyticsEnabled = MutableStateFlow(true)
    val analyticsEnabled = _analyticsEnabled.asStateFlow()

    private val _bandwidthSharingEnabled = MutableStateFlow(false)
    val bandwidthSharingEnabled = _bandwidthSharingEnabled.asStateFlow()

    init {
        viewModelScope.launch {
           _analyticsEnabled.emit(settingsRepo.analyticsEnabled.get() != "false")
            _bandwidthSharingEnabled.emit(settingsRepo.bandwidthSharingEnabled.get() == "true")
        }
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _analyticsEnabled.emit(enabled) // updates UI
            settingsRepo.analyticsEnabled.set(boolToText(enabled)) // saves in shared prefs
            analyticsStatus.notifyStatus(enabled) // disables / enables analytics lib
        }
    }

    fun setSharingBandwidthEnabled(enabled: Boolean) {
        _bandwidthSharingEnabled.value = enabled
        viewModelScope.launch(Dispatchers.IO) {
            connectionRepo.updateSharingBandwidth(enabled)
            settingsRepo.bandwidthSharingEnabled.set(boolToText(enabled))
        }
    }

    private fun boolToText(v: Boolean): String {
        var text = "true"
        if (!v) {
            text = "false"
        }
        return text
    }
}