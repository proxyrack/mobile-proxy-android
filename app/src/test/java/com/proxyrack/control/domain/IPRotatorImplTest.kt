package com.proxyrack.control.domain

import com.proxyrack.control.data.repository.ConnectionRepo
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest

import org.junit.Test

class IPRotatorImplTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `set rotation interval to 0 and job gets canceled`() = runTest {
        // start the job
        val ipRotator = IPRotatorImpl(ConnectionRepo(), AirplaneModeFake(), ConnLauncherFake(), this)
        ipRotator.setRotationInterval(1)
        ipRotator.startRotationJob()
        advanceTimeBy(1000)

        // set interval to 0 and expect that the job has been canceled
        ipRotator.setRotationInterval(0)
        advanceUntilIdle()
        assertTrue(ipRotator.job?.isCancelled == true)

    }
}