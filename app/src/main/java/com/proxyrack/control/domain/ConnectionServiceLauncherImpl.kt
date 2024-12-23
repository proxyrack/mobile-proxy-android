package com.proxyrack.control.domain

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class ConnectionServiceLauncherImpl (private val context: Context): ConnectionServiceLauncher {
    override fun connect() {
        val serviceIntent = Intent(context, ConnectionService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    override fun disconnect() {
        val serviceIntent = Intent(context, ConnectionService::class.java)
        context.stopService(serviceIntent)
    }
}