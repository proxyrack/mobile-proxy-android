package com.proxyrack.control.domain

import com.proxyrack.control.data.repository.ConnectionRepo
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.junit.Test
import org.mockito.kotlin.mock

class IPRotatorImplTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `rotation occurs after specified time period`() = runTest {
        val connLauncher = mock<ConnectionServiceLauncherImpl>()
        val ipRotator = spy(IPRotatorImpl(ConnectionRepo(), mock<AirplaneMode>(), connLauncher, this.backgroundScope, 0, 0))

        ipRotator.setRotationInterval(1) // 1 minute
        ipRotator.startRotationJob()

        advanceTimeBy(1000*61)

        verify(connLauncher).disconnect()
        verify(connLauncher).connect()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `rotation stops properly`() = runTest {
        val ipRotator = spy(IPRotatorImpl(ConnectionRepo(), mock<AirplaneMode>(), mock<ConnectionServiceLauncherImpl>(), this.backgroundScope, 0, 0))

        ipRotator.setRotationInterval(1) // 1 minute
        ipRotator.startRotationJob()
        ipRotator.stopRotationJob()
        advanceTimeBy(1000)

        assertTrue(ipRotator.job?.isCancelled == true)
    }
}