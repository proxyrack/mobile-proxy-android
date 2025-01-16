package com.proxyrack.control.domain.proxy_manager

import com.proxyrack.proxylib.android.Manager

// Wraps proxylib Manager to fulfill ProxyManager interface
class ProxyManagerAdapter(private val manager: Manager): ProxyManager {
    override fun connect(
        host: String,
        port: Long,
        deviceID: String,
        username: String,
        sharingBandwidth: Boolean
    ) {
        manager.connect(host, port, deviceID, username, sharingBandwidth)
    }

    override fun disconnect() {
        manager.disconnect()
    }

    override fun registerOnLogEntryCallback(callback: (String) -> Unit) {
        manager.registerOnLogEntryCallback(callback)
    }

    override fun registerOnConnectCallback(callback: () -> Unit) {
        manager.registerOnConnectCallback(callback)
    }

    override fun registerOnDisconnectCallback(callback: () -> Unit) {
        manager.registerOnDisconnectCallback(callback)
    }

    override fun unregisterOnDisconnectCallback() {
        manager.unregisterOnDisconnectCallback()
    }
}