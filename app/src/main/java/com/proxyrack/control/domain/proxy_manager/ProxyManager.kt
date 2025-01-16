package com.proxyrack.control.domain.proxy_manager


interface ProxyManager {
    fun connect(host: String, port: Long, deviceID: String, username: String, sharingBandwidth: Boolean)
    fun disconnect()
    fun registerOnLogEntryCallback(callback: (String) -> Unit)
    fun registerOnConnectCallback(callback: () -> Unit)
    fun registerOnDisconnectCallback(callback: () -> Unit)
    fun unregisterOnDisconnectCallback()
}