package com.proxyrack.control

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import com.proxyrack.control.data.repository.ConnectionRepo
import com.proxyrack.control.domain.ConnectionStatus
import com.proxyrack.control.domain.repository.SettingsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
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

    suspend fun previouslyInitialized(): Boolean {
        return settingsRepo.initialized.get().isNotEmpty()
    }

    suspend fun setPreviouslyInitialized() {
        settingsRepo.initialized.set("true")
    }

    // To be called when main activity has finished its initialization tasks
    suspend fun initializationTasksFinished() {
        Log.d(javaClass.simpleName, "initializationTasksFinished")
        val savedUsername = settingsRepo.username.get()
        var savedDeviceID = settingsRepo.deviceID.get()
        Log.d(javaClass.simpleName, "saved username: $savedUsername saved deviceID: $savedDeviceID")
        connectionRepo.updateUsername(savedUsername)
        connectionRepo.updateDeviceID(savedDeviceID)
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
                connectionRepo.addLogMessage("Connecting...")
                connectionRepo.updateConnectionStatus(ConnectionStatus.Connecting)
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

