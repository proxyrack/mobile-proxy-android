package com.proxyrack.control.data.repository

import com.proxyrack.control.domain.ConnectionStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ConnectionRepo {
    private val _username = MutableStateFlow("")
    val username = _username.asStateFlow()

    private val _deviceID = MutableStateFlow("")
    val deviceID = _deviceID.asStateFlow()

    private val _deviceIP = MutableStateFlow("")
    val deviceIP = _deviceIP.asStateFlow()

    private val _sharingBandwidth = MutableStateFlow(false)
    val sharingBandwidth = _sharingBandwidth.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
    val logMessages = _logMessages.asStateFlow()

    fun updateSharingBandwidth(enabled: Boolean) {
        _sharingBandwidth.value = enabled
    }

    fun updateConnectionStatus(status: ConnectionStatus) {
        _connectionStatus.value = status
    }

    fun updateDeviceIP(ip: String) {
        _deviceIP.value = ip
    }

    fun updateUsername(name: String) {
        _username.value = name
    }

    fun updateDeviceID(id: String) {
        _deviceID.value = id
    }

    fun addLogMessage(msg: String) {
        _logMessages.update { currentMessages ->
            currentMessages + msg.removeSuffix("\n").replaceFirstChar { it.uppercaseChar() }
        }
    }
}