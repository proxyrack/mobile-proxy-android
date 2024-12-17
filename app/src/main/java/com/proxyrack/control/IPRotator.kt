package com.proxyrack.control

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class IPRotator {
    private val ap = AirplaneMode()
    private var job: Job? = null
    private var rotationIntervalMillis: Long = 0

    fun setRotationInterval(minutes: Int) {
        job?.cancel()
        rotationIntervalMillis = (minutes*60*1000).toLong()
        startRotationJob()
    }

    private fun startRotationJob() {
        job = CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                delay(rotationIntervalMillis)
                ap.toggleOnOff()
            }
        }
    }

    fun rotate() {
        val hadPendingJob = job != null
        job?.cancel()

        CoroutineScope(Dispatchers.IO).launch {
            ap.toggleOnOff()

            if (hadPendingJob) {
                startRotationJob()
            }
        }
    }

    fun disableRotationInterval() {
        job?.cancel()
    }
}


