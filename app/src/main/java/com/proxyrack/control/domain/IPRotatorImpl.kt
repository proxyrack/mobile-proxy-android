package com.proxyrack.control.domain

import com.proxyrack.control.data.repository.ConnectionRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.sync.Mutex
import java.util.concurrent.atomic.AtomicLong

class IPRotatorImpl (
    private val connectionRepo: ConnectionRepo,
    private val ap: AirplaneMode,
    private val connLauncher: ConnectionServiceLauncher,
    private val scope: CoroutineScope,
    private val apToggleDelay: Long = 1000,
    private val internetConnectivityDelay: Long = 10000
    ): IPRotator {

    private val rotateCh = Channel<Unit>()

    private var _job: Job? = null
    private val jobMutex = Mutex()

    override var job: Job? = null
        get() { // for testing
            return _job
        }

    private var rotationIntervalMillis = AtomicLong(0)

    init {
        scope.launch {
            for (msg in rotateCh) {
                doRotation()

                // User may have disabled rotation while we were rotating.
                // Only restart the timer if rotation is still enabled.
                val rotationEnabled = rotationIntervalMillis.get() != 0.toLong()
                if (rotationEnabled) {
                    startJob()
                }
            }
        }
    }

    override fun setRotationInterval(minutes: Int) {
        rotationIntervalMillis.set(minutes*60*1000.toLong())
    }

    override fun startRotationJob() {
        if (rotationIntervalMillis.get() == 0.toLong()) {
            println("rotationIntervalMillis uninitialized!!")
            return
        }

        scope.launch {
            startJob()
        }
    }

    override fun rotateOffSchedule() {
        scope.launch {
            jobMutex.withLock {
                _job?.cancel()
            }
            rotateCh.trySend(Unit)
        }
    }

    override fun stopRotationJob() {
        scope.launch {
            jobMutex.withLock {
                _job?.cancel()
            }
        }
    }

    private suspend fun startJob() {
        jobMutex.withLock {
            _job?.cancel()
            _job = scope.launch {
                delay(rotationIntervalMillis.get())
                rotateCh.trySend(Unit)
            }
        }
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


