package com.proxyrack.control

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proxyrack.control.data.repository.ConnectionRepo
import com.proxyrack.control.data.repository.IpInfoRepository
import com.proxyrack.control.domain.ConnectionStatus
import com.proxyrack.control.domain.repository.SettingsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.flow.update


@HiltViewModel
class MainViewModel @Inject constructor(
    application: Application,
    private val settingsRepo: SettingsRepo,
    private val connectionRepo: ConnectionRepo): AndroidViewModel(application) {

//    private val _serverIP = MutableStateFlow("")
//    val serverIP = _serverIP.asStateFlow()

//    private val _deviceIP = MutableStateFlow("")
//    val deviceIP = _deviceIP.asStateFlow()

//    private val _deviceID = MutableStateFlow("")
//    val deviceID = _deviceID.asStateFlow()

//    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
//    val connectionStatus = _connectionStatus.asStateFlow()

//    private val _logMessages = MutableStateFlow<List<String>>(emptyList())
//    val logMessages = _logMessages.asStateFlow()

    val serverIP: StateFlow<String>
        get() = connectionRepo.serverIP

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

    //private val proxyManager: Manager;

    init {
        viewModelScope.launch {
            connectionRepo.updateServerIP(settingsRepo.serverIP.get())
            connectionRepo.updateDeviceID(settingsRepo.deviceID.get())
        }

//        val pid = android.os.Process.myPid().toLong()
//        val androidApiVersion = Build.VERSION.SDK_INT.toString()
//        val cpuArch = getCPUArchitecture()
//        Log.d("MyApp androidApiVersion", androidApiVersion)
//        Log.d("MyApp pid", pid.toString())
//        Log.d("MyApp cpu arch", cpuArch)
//        proxyManager = newManager(pid, "55", androidApiVersion, cpuArch)
//
//        proxyManager.registerOnConnectCallback {
//            // todo: Get IP address and display in "Your Device IP" field
//            _connectionStatus.value = ConnectionStatus.Connected
//            Log.d("VM", "registerOnConnectCallback called")
//
//            ipInfoRepo.getIpInfo { info, error ->
//                if (info != null) {
//                    Log.d("VM", "IP: ${info.ip}")
//                    _deviceIP.value = info.ip
//                } else if (error != null) {
//                    Log.d("VM", error.toString())
//                }
//            }
//        }
//
//        proxyManager.registerOnDisconnectCallback {
//            _connectionStatus.value = ConnectionStatus.Disconnected
//            Log.d("VM", "registerOnDisconnectCallback called")
//        }
//
//        proxyManager.registerOnLogEntryCallback { msg ->
//            _logMessages.update { currentMessages ->
//                currentMessages + msg.removeSuffix("\n").replaceFirstChar { it.uppercaseChar() }
//            }
//        }
    }

    fun updateShowFormError(show: Boolean) {
        _showFormError.value = show
    }

    fun updateServerIP(ip: String) {
        Log.d("sip", "updating server ip $ip")
        connectionRepo.updateServerIP(ip)
        maybeClearFormError()
    }

    private fun maybeClearFormError() {
        Log.d("VM", "serverIP value: ${serverIP.value.isNotEmpty()}")
        Log.d("VM", "deviceID value: ${deviceIP.value.isNotEmpty()}")
        Log.d("VM", "showFormError value: ${showFormError.value}")
        if (serverIP.value.isNotEmpty() && deviceID.value.isNotEmpty() && showFormError.value) {
            updateShowFormError(false)
            Log.d("VM", "cleared for error")
        }
    }

    fun saveServerIP() {
        viewModelScope.launch {
            settingsRepo.serverIP.set(serverIP.value)
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

//    private fun disconnect() {
//        viewModelScope.launch(Dispatchers.Default) {
//            proxyManager.disconnect()
//        }
//    }
//
//    private fun connect() {
//        // todo: Cancel connection attempt after a certain amount of time
//        // todo: Run in a service
//        viewModelScope.launch(Dispatchers.Default) { // Dispatchers.Default runs on a new thread
//            try {
//                proxyManager.connect(serverIP.value, 443,deviceID.value)
//            } catch (e: Exception) {
//                Log.d("VM", "Failed to connect")
//                _connectionStatus.value = ConnectionStatus.Disconnected
//            }
//
//        }
//    }

//    private fun getCPUArchitecture(): String {
//        // Using the supported ABIs to determine the CPU architecture
//        val supportedABIs = Build.SUPPORTED_ABIS
//
//        if (supportedABIs.isNotEmpty()) {
//            // Typically, the first one is the ABI of the current device.
//            val abi = supportedABIs[0]
//            return when {
//                abi.startsWith("arm64") -> "arm64"
//                abi.startsWith("armeabi") -> "armeabi"
//                abi.startsWith("x86_64") -> "x86_64"
//                abi.startsWith("x86") -> "x86"
//                else -> "unknown"
//            }
//        }
//        return "unknown"
//    }
}
