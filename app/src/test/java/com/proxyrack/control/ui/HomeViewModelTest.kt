package com.proxyrack.control.ui

import android.app.Application
import com.proxyrack.control.data.repository.ConnectionRepo
import com.proxyrack.control.domain.ConnectionServiceLauncher
import com.proxyrack.control.domain.ConnectionStatus
import com.proxyrack.control.domain.IPRotator
import com.proxyrack.control.domain.repository.DataAccessor
import com.proxyrack.control.domain.repository.SettingsRepo
import com.proxyrack.control.ui.screens.HomeViewModel
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.mockito.Mockito.verify
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.spy

class HomeViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `setIPRotationInterval starts rotation job when passed text has a positive integer as first character, and status is Connecting or Connected`() = runTest {
        val connRepo = ConnectionRepo()
        connRepo.updateConnectionStatus(ConnectionStatus.Connecting)

        val settingsRepo = mock<SettingsRepo>()
        val ipRotator = mock<IPRotator>()

        val ipRotationInterval = mock<DataAccessor>()
        `when`(settingsRepo.ipRotationInterval).thenReturn(ipRotationInterval)
        // define that the 'set' method of 'ipRotationInterval' should do nothing
        `when`(runBlocking { ipRotationInterval.set(anyString()) }).thenAnswer { }

        val vm = spy(HomeViewModel(mock<ConnectionServiceLauncher>(), settingsRepo, connRepo, ipRotator))
        val intervalText = "1 min"
        vm.setIPRotationInterval(intervalText)
        advanceUntilIdle()

        verify(ipRotator).setRotationInterval(1)
        verify(ipRotator).stopRotationJob()
        verify(ipRotator).startRotationJob()
        verify(ipRotationInterval).set(intervalText)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `setIPRotationInterval stops rotation job when passed text does not have an integer as first character`() = runTest {
        val connRepo = ConnectionRepo()
        connRepo.updateConnectionStatus(ConnectionStatus.Connecting)

        val settingsRepo = mock<SettingsRepo>()
        val ipRotator = mock<IPRotator>()

        val ipRotationInterval = mock<DataAccessor>()
        `when`(settingsRepo.ipRotationInterval).thenReturn(ipRotationInterval)
        // define that the 'set' method of 'ipRotationInterval' should do nothing
        `when`(runBlocking { ipRotationInterval.set(anyString()) }).thenAnswer { }

        val vm = spy(HomeViewModel(mock<ConnectionServiceLauncher>(), settingsRepo, connRepo, ipRotator))
        val intervalText = "Disabled"
        vm.setIPRotationInterval(intervalText)
        advanceUntilIdle()

        verify(ipRotator).setRotationInterval(0)
        verify(ipRotator).stopRotationJob()
        verify(ipRotator, never()).startRotationJob()
        verify(ipRotationInterval).set(intervalText)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `setIPRotationInterval does not start rotation job when connection status is Disconnected`() = runTest {
        val connRepo = ConnectionRepo()
        connRepo.updateConnectionStatus(ConnectionStatus.Disconnected)

        val settingsRepo = mock<SettingsRepo>()
        val ipRotator = mock<IPRotator>()

        val ipRotationInterval = mock<DataAccessor>()
        `when`(settingsRepo.ipRotationInterval).thenReturn(ipRotationInterval)
        // define that the 'set' method of 'ipRotationInterval' should do nothing
        `when`(runBlocking { ipRotationInterval.set(anyString()) }).thenAnswer { }

        val vm = spy(HomeViewModel(mock<ConnectionServiceLauncher>(), settingsRepo, connRepo, ipRotator))
        val intervalText = "1 min"
        vm.setIPRotationInterval(intervalText)
        advanceUntilIdle()

        verify(ipRotator).setRotationInterval(1)
        verify(ipRotator).stopRotationJob()
        verify(ipRotator, never()).startRotationJob()
        verify(ipRotationInterval).set(intervalText)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `connectionButtonClicked sets connection status to Disconnected if called while connection status is Connected`() = runTest {
        val connRepo = ConnectionRepo()
        connRepo.updateConnectionStatus(ConnectionStatus.Connected)

        val settingsRepo = mock<SettingsRepo>()
        val ipRotator = mock<IPRotator>()

        val vm = spy(HomeViewModel(mock<ConnectionServiceLauncher>(), settingsRepo, connRepo, ipRotator))
        vm.connectionButtonClicked()

        assertTrue(connRepo.connectionStatus.value == ConnectionStatus.Disconnected)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `connectionButtonClicked starts rotation job when connection status is Disconnected`() = runTest {
        val connRepo = ConnectionRepo()
        connRepo.updateConnectionStatus(ConnectionStatus.Disconnected)

        val settingsRepo = mock<SettingsRepo>()
        val ipRotator = mock<IPRotator>()

        val vm = spy(HomeViewModel(mock<ConnectionServiceLauncher>(), settingsRepo, connRepo, ipRotator))
        vm.connectionButtonClicked()

        verify(ipRotator).startRotationJob()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `connectionButtonClicked stops rotation job when connection status is Connected`() = runTest {
        val connRepo = ConnectionRepo()
        connRepo.updateConnectionStatus(ConnectionStatus.Connected)

        val settingsRepo = mock<SettingsRepo>()
        val ipRotator = mock<IPRotator>()

        val vm = spy(HomeViewModel(mock<ConnectionServiceLauncher>(), settingsRepo, connRepo, ipRotator))
        vm.connectionButtonClicked()

        verify(ipRotator).stopRotationJob()
    }
}