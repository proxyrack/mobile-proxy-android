package com.proxyrack.control

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.proxyrack.control.data.repository.ConnectionRepo
import com.proxyrack.control.domain.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IPRotator(private val context: Context, private val connectionRepo: ConnectionRepo) {
    private val ap = AirplaneMode()
    private var job: Job? = null
    private var rotationIntervalMillis: Long = 0

    fun setRotationInterval(minutes: Int) {
        if (minutes == 0) {
            job?.cancel()
        }

        rotationIntervalMillis = (minutes*60*1000).toLong()
    }

    fun startRotationJob() {
        if (rotationIntervalMillis == 0.toLong()) {
            println("rotationIntervalMillis uninitialized!!")
            return
        }

        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(rotationIntervalMillis)
                doRotation()
            }
        }
    }

    fun rotate() {
        val hadPendingJob = job != null
        job?.cancel()

        CoroutineScope(Dispatchers.IO).launch {
            doRotation()

            if (hadPendingJob) {
                startRotationJob()
            }
        }
    }

    fun stopRotationJob() {
        job?.cancel()
    }

    private suspend fun doRotation() {
        disconnect()

        ap.enable()
        delay(1000)
        ap.disable()

        connectionRepo.addLogMessage("Waiting for internet connection")
        delay(10000) // todo: confirm we have an internet connection
        connectionRepo.addLogMessage("Connecting...")

        connect()
    }

    private fun connect() {
        val serviceIntent = Intent(context, ConnectionService::class.java)
        ContextCompat.startForegroundService(context, serviceIntent)
    }

    private fun disconnect() {
        connectionRepo.updateConnectionStatus(ConnectionStatus.Connecting)
        val serviceIntent = Intent(context, ConnectionService::class.java)
        context.stopService(serviceIntent)
    }
}


