package com.proxyrack.control.domain

import com.proxyrack.control.data.repository.ConnectionRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IPRotatorImpl (
    private val connectionRepo: ConnectionRepo,
    private val ap: AirplaneMode,
    private val connLauncher: ConnectionServiceLauncher,
    private val scope: CoroutineScope,
    private val apToggleDelay: Long = 1000,
    private val internetConnectivityDelay: Long = 10000
    ): IPRotator {

    private var _job: Job? = null

    override var job: Job? = null
        get() { // for testing
            return _job
        }

    private var rotationIntervalMillis: Long = 0

    override fun setRotationInterval(minutes: Int) {
        if (minutes == 0) {
            _job?.cancel()
        }

        rotationIntervalMillis = (minutes*60*1000).toLong()
    }

    override fun startRotationJob() {
        if (rotationIntervalMillis == 0.toLong()) {
            println("rotationIntervalMillis uninitialized!!")
            return
        }

        _job?.cancel()
        _job = scope.launch {
            while (true) {
                delay(rotationIntervalMillis)
                doRotation()
            }
        }
    }

    override fun rotate() {
        val hadPendingJob = _job != null
        _job?.cancel()

        scope.launch {
            doRotation()

            if (hadPendingJob) {
                startRotationJob()
            }
        }
    }

    override fun stopRotationJob() {
        _job?.cancel()
    }

    private suspend fun doRotation() {
        disconnect()

        ap.enable()
        delay(apToggleDelay)
        ap.disable()

        connectionRepo.addLogMessage("Waiting for internet connection")
        delay(internetConnectivityDelay) // todo: confirm we have an internet connection rather than just waiting
        connectionRepo.addLogMessage("Connecting...")

        connect()
    }

    private fun connect() {
        connLauncher.connect()
    }

    private fun disconnect() {
        connectionRepo.updateConnectionStatus(ConnectionStatus.Connecting)
        connLauncher.disconnect()
    }
}


