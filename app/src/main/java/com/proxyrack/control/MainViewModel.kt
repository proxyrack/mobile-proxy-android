package com.proxyrack.control

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.proxyrack.control.data.repository.ConnectionRepo
import com.proxyrack.control.domain.ConnectionStatus
import com.proxyrack.control.domain.repository.SettingsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val settingsRepo: SettingsRepo,
    private val connectionRepo: ConnectionRepo): AndroidViewModel(application) {

    val username: StateFlow<String>
        get() = connectionRepo.username

    val deviceID: StateFlow<String>
        get() = connectionRepo.deviceID

    val deviceIP: StateFlow<String>
        get() = connectionRepo.deviceIP

    val connectionStatus: StateFlow<ConnectionStatus>
        get() = connectionRepo.connectionStatus

    val logMessages: StateFlow<List<String>>
        get() = connectionRepo.logMessages

    private val _showFormError = MutableStateFlow<Boolean>(false)
    val showFormError = _showFormError.asStateFlow()

    init {
        viewModelScope.launch {
            connectionRepo.updateUsername(settingsRepo.username.get())
            connectionRepo.updateDeviceID(settingsRepo.deviceID.get())

            // If this is the first time the app has been run, generate a random device ID
            val previouslyInitialized = settingsRepo.initialized.get().isNotEmpty()
            if (!previouslyInitialized) {
                settingsRepo.initialized.set("true")
                val uuid = UUID.randomUUID().toString()
                connectionRepo.updateDeviceID(uuid)
                settingsRepo.deviceID.set(uuid)
            }
        }
    }

    fun updateShowFormError(show: Boolean) {
        _showFormError.value = show
    }

    fun updateUsername(ip: String) {
        Log.d("sip", "updating server ip $ip")
        connectionRepo.updateUsername(ip)
        maybeClearFormError()
    }

    private fun maybeClearFormError() {
        Log.d("VM", "username value: ${username.value.isNotEmpty()}")
        Log.d("VM", "deviceID value: ${deviceIP.value.isNotEmpty()}")
        Log.d("VM", "showFormError value: ${showFormError.value}")
        if (username.value.isNotEmpty() && deviceID.value.isNotEmpty() && showFormError.value) {
            updateShowFormError(false)
            Log.d("VM", "cleared for error")
        }
    }

    fun saveUsername() {
        viewModelScope.launch {
            settingsRepo.username.set(username.value)
        }
    }

    fun updateDeviceID(id: String) {
        connectionRepo.updateDeviceID(id)
        maybeClearFormError()
    }

    fun saveDeviceID() {
        viewModelScope.launch {
            settingsRepo.deviceID.set(deviceID.value)
        }
    }

    fun connectionButtonClicked() {
        when (connectionStatus.value) {
            ConnectionStatus.Connecting -> return
            ConnectionStatus.Connected -> disconnect()
            ConnectionStatus.Disconnected -> {
                connectionRepo.updateConnectionStatus(ConnectionStatus.Connected)
                connect()
            }
        }
    }

    private fun connect() {
        val context = getApplication<Application>().applicationContext
        val serviceIntent = Intent(context, ConnectionService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    private fun disconnect() {
        val context = getApplication<Application>().applicationContext
        val serviceIntent = Intent(context, ConnectionService::class.java)
        context.stopService(serviceIntent)
    }

}
