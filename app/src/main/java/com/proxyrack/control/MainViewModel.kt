package com.proxyrack.control

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import com.proxyrack.control.domain.repository.SettingsRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val settingsRepo: SettingsRepo): ViewModel() {
    private val sharedPrefsName = "MyPreferences"

    private val _serverIP = MutableStateFlow("")
    val serverIP = _serverIP.asStateFlow()

    private val _deviceIP = MutableStateFlow("")
    val deviceIP = _deviceIP.asStateFlow()

    private val _deviceID = MutableStateFlow("")
    val deviceID = _deviceID.asStateFlow()

    private val _connected = MutableStateFlow(false)
    val connected = _connected.asStateFlow()

    init {

    }

    fun updateServerIP(ip: String) {
        Log.d("sip", "updating server ip $ip")
        _serverIP.value = ip
    }

    fun saveServerIP(context: Context) {
        saveToSharedPrefs(context, "serverIP", this.serverIP.value)
    }

    fun updateDeviceIP(ip: String) {
        _deviceIP.value = ip
    }

    fun updateDeviceID(id: String) {
        _deviceID.value = id
    }

    fun saveDeviceID(context: Context) {
        saveToSharedPrefs(context, "deviceID", this.deviceIP.value)
    }

    fun updateConnected(connected: Boolean) {
        _connected.value = connected
    }

    private fun saveToSharedPrefs(context: Context, key: String, value: String) {

        val sharedPreferences = context.getSharedPreferences(this.sharedPrefsName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    private fun restoreFromSharedPrefs(context: Context) {

    }
}