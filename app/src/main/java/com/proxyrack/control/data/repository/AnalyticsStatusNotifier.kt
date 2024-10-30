package com.proxyrack.control.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

// This class serves to notify when user has
class AnalyticsStatusNotifier {
    private val _analyticsStatusChanged = MutableSharedFlow<Boolean>()
    val analyticsStatus = _analyticsStatusChanged.asSharedFlow()

    suspend fun notifyStatus(enabled: Boolean) {
        _analyticsStatusChanged.emit(enabled)
    }
}