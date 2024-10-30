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

    suspend fun previouslyInitialized(): Boolean {
        return settingsRepo.initialized.get().isNotEmpty()
    }

    suspend fun setPreviouslyInitialized() {
        settingsRepo.initialized.set("true")
    }

    // To be called when main activity has finished its initialization tasks
    suspend fun initializationTasksFinished() {
        val savedUsername = settingsRepo.username.get()
        var savedDeviceID = settingsRepo.deviceID.get()
        connectionRepo.updateUsername(savedUsername)
        connectionRepo.updateDeviceID(savedDeviceID)
        _initialFormValues.value = InitialFormValues(savedUsername, savedDeviceID)
    }

    suspend fun saveUsername(username: String) {
        connectionRepo.updateUsername(username)
        settingsRepo.username.set(username)
    }

    suspend fun saveDeviceID(id: String) {
        connectionRepo.updateDeviceID(id)
        settingsRepo.deviceID.set(id)
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
