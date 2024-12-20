package com.proxyrack.control.domain

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.proxyrack.control.data.repository.ConnectionRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IPRotatorImpl (
    private val context: Context,
    private val connectionRepo: ConnectionRepo,
    private val ap: AirplaneMode,
    ): IPRotator {

    private var job: Job? = null
    private var rotationIntervalMillis: Long = 0

    override fun setRotationInterval(minutes: Int) {
        if (minutes == 0) {
            job?.cancel()
        }

        rotationIntervalMillis = (minutes*60*1000).toLong()
    }

    override fun startRotationJob() {
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

    override fun rotate() {
        val hadPendingJob = job != null
        job?.cancel()

        CoroutineScope(Dispatchers.IO).launch {
            doRotation()

            if (hadPendingJob) {
                startRotationJob()
            }
        }
    }

    override fun stopRotationJob() {
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


