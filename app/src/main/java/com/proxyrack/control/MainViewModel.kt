package com.proxyrack.control

import android.app.Application
import android.content.Intent
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

    val deviceIP: StateFlow<String>
        get() = connectionRepo.deviceIP

    val connectionStatus: StateFlow<ConnectionStatus>
        get() = connectionRepo.connectionStatus

    val logMessages: StateFlow<List<String>>
        get() = connectionRepo.logMessages

    private val _initialFormValues: MutableStateFlow<InitialFormValues?> = MutableStateFlow(null)
    val initialFormValues = _initialFormValues.asStateFlow()

    init {
        viewModelScope.launch {
            val savedUsername = settingsRepo.username.get()
            var savedDeviceID = settingsRepo.deviceID.get()

            connectionRepo.updateUsername(savedUsername)
            connectionRepo.updateDeviceID(savedDeviceID)

            // If this is the first time the app has been run, generate a random device ID
            val previouslyInitialized = settingsRepo.initialized.get().isNotEmpty()
            if (!previouslyInitialized) {
                settingsRepo.initialized.set("true")
                savedDeviceID = UUID.randomUUID().toString()
                connectionRepo.updateDeviceID(savedDeviceID)
                settingsRepo.deviceID.set(savedDeviceID)
            }

            _initialFormValues.value = InitialFormValues(savedUsername, savedDeviceID)
        }
    }

    fun saveUsername(username: String) {
        connectionRepo.updateUsername(username)
        viewModelScope.launch {
            settingsRepo.username.set(username)
        }
    }

    fun saveDeviceID(id: String) {
        connectionRepo.updateDeviceID(id)
        viewModelScope.launch {
            settingsRepo.deviceID.set(id)
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

data class InitialFormValues(val username: String, val deviceID: String)
