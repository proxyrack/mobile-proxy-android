package com.proxyrack.control

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proxyrack.control.domain.ConnectionStatus
import com.proxyrack.control.domain.repository.SettingsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android_main.Android_main
import android_main.Manager
import android_main.OnConnectCallback
import android_main.OnDisconnectCallback

@HiltViewModel
class MainViewModel @Inject constructor(private val settingsRepo: SettingsRepo): ViewModel() {
    private val sharedPrefsName = "MyPreferences"

    private val _serverIP = MutableStateFlow("")
    val serverIP = _serverIP.asStateFlow()

    private val _deviceIP = MutableStateFlow("")
    val deviceIP = _deviceIP.asStateFlow()

    private val _deviceID = MutableStateFlow("")
    val deviceID = _deviceID.asStateFlow()

    private val _connectionStatus = MutableStateFlow(ConnectionStatus.Disconnected)
    val connectionStatus = _connectionStatus.asStateFlow()

    private val proxyManager: Manager;

    init {
        viewModelScope.launch {
            _serverIP.value = settingsRepo.serverIP.get()
            _deviceID.value = settingsRepo.deviceID.get()
        }

        val pid = android.os.Process.myPid().toLong()
        val androidApiVersion = Build.VERSION.SDK_INT.toString()
        val cpuArch = getCPUArchitecture()
        Log.d("MyApp androidApiVersion", androidApiVersion)
        Log.d("MyApp pid", pid.toString())
        Log.d("MyApp cpu arch", cpuArch)
        proxyManager = Android_main.newManager(pid, "50", androidApiVersion, cpuArch)
        proxyManager.registerOnConnectCallback(OnConnectCallback {
            Log.d("VM", "lambda in registerOnConnectCallback called")
        })
        proxyManager.registerOnDisconnectCallback(OnDisconnectCallback {
            Log.d("VM", "lambda in registerOnDisconnectCallback called")
        })
    }

    fun updateServerIP(ip: String) {
        Log.d("sip", "updating server ip $ip")
        _serverIP.value = ip
    }

    fun saveServerIP() {
        viewModelScope.launch {
            settingsRepo.serverIP.set(serverIP.value)
        }
    }

    fun updateDeviceIP(ip: String) {
        _deviceIP.value = ip
    }

    fun updateDeviceID(id: String) {
        _deviceID.value = id
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
                _connectionStatus.value = ConnectionStatus.Connecting
                connect()
            }
        }
    }

    private fun disconnect() {
        viewModelScope.launch(Dispatchers.Default) {
            proxyManager.disconnect()
        }
    }

    private fun connect() {
        // todo: Cancel connection attempt after a certain amount of time
        // todo: Run in a service
        viewModelScope.launch(Dispatchers.Default) { // Dispatchers.Default runs on a new thread
            try {
                proxyManager.connect(serverIP.value, 443,deviceID.value)
            } catch (e: Exception) {
                Log.d("VM", "Failed to connect")
                _connectionStatus.value = ConnectionStatus.Disconnected
            }

        }
    }

    private fun getCPUArchitecture(): String {
        // Using the supported ABIs to determine the CPU architecture
        val supportedABIs = Build.SUPPORTED_ABIS

        if (supportedABIs.isNotEmpty()) {
            // Typically, the first one is the ABI of the current device.
            val abi = supportedABIs[0]
            return when {
                abi.startsWith("arm64") -> "arm64"
                abi.startsWith("armeabi") -> "armeabi"
                abi.startsWith("x86_64") -> "x86_64"
                abi.startsWith("x86") -> "x86"
                else -> "unknown"
            }
        }
        return "unknown"
    }
}

class ConnectCallback(private val connectionStatus: MutableStateFlow<ConnectionStatus>): OnConnectCallback {
    override fun onConnect() {
        Log.d("VM", "onConnect called")
        connectionStatus.value = ConnectionStatus.Connected
    }
}

class DisconnectCallback(private val connectionStatus: MutableStateFlow<ConnectionStatus>): OnDisconnectCallback {
    override fun onDisconnect() {
        Log.d("VM", "onDisconnect called")
        connectionStatus.value = ConnectionStatus.Disconnected
    }
}