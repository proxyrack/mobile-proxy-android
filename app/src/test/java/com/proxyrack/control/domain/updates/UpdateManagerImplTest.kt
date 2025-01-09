package com.proxyrack.control.domain.updates

import com.proxyrack.control.domain.model.ReleaseInfo
import com.proxyrack.control.domain.repository.GithubReleasesRepo
import io.github.z4kn4fein.semver.toVersion
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import java.io.File
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

class UpdateManagerImplTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `checkForUpdate determines greater version is greater`() = runTest {
        val releasesRepo = mock<GithubReleasesRepo>()
        val apkInstaller = mock<APKInstaller>()

        `when`(releasesRepo.getLatestRelease()).thenReturn(ReleaseInfo("1.0.2", "update_url"))

        val currentVersion = "1.0.1".toVersion(strict = false)
        val manager = UpdateManagerImpl(currentVersion, releasesRepo, apkInstaller, File("/"))

        val result = manager.checkForUpdate()
        assertTrue("update should be available", result.available)
        assertTrue("url should not be empty", result.url != "")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `checkForUpdate determines lower version is lower`() = runTest {
        val releasesRepo = mock<GithubReleasesRepo>()
        val apkInstaller = mock<APKInstaller>()

        `when`(releasesRepo.getLatestRelease()).thenReturn(ReleaseInfo("1.0.0", "update_url"))

        val currentVersion = "1.0.1".toVersion(strict = false)
        val manager = UpdateManagerImpl(currentVersion, releasesRepo, apkInstaller, File("/"))

        val result = manager.checkForUpdate()
        assertTrue("update should not be available", !result.available)
        assertTrue("url should be empty", result.url == "")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `checkForUpdate determines equal version results in no update available`() = runTest {
        val releasesRepo = mock<GithubReleasesRepo>()
        val apkInstaller = mock<APKInstaller>()

        `when`(releasesRepo.getLatestRelease()).thenReturn(ReleaseInfo("1.0.0", "update_url"))

        val currentVersion = "1.0.0".toVersion(strict = false)
        val manager = UpdateManagerImpl(currentVersion, releasesRepo, apkInstaller, File("/"))

        val result = manager.checkForUpdate()
        assertTrue("update should not be available", !result.available)
        assertTrue("url should be empty", result.url == "")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `checkForUpdate indicates that no update is available if a check was performed less than 24 hours ago`() = runTest {
        val releasesRepo = mock<GithubReleasesRepo>()
        val apkInstaller = mock<APKInstaller>()

        `when`(releasesRepo.getLatestRelease()).thenReturn(ReleaseInfo("1.0.1", "update_url"))

        val currentVersion = "1.0.0".toVersion(strict = false)
        val timeSource = TestTimeSource()
        val manager = UpdateManagerImpl(currentVersion, releasesRepo, apkInstaller, File("/"), timeSource)

        var result = manager.checkForUpdate()
        assertTrue("update should be available", result.available)

        timeSource.addElapsedTime(5.minutes)

        result = manager.checkForUpdate()
        assertTrue("update should not be available", !result.available)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `checkForUpdate indicates that an update is available if a check was performed greater than 24 hours ago and 'ignoreCache' is true`() = runTest {
        val releasesRepo = mock<GithubReleasesRepo>()
        val apkInstaller = mock<APKInstaller>()

        `when`(releasesRepo.getLatestRelease()).thenReturn(ReleaseInfo("1.0.1", "update_url"))

        val currentVersion = "1.0.0".toVersion(strict = false)
        val timeSource = TestTimeSource()
        val manager = UpdateManagerImpl(currentVersion, releasesRepo, apkInstaller, File("/"), timeSource)

        var result = manager.checkForUpdate()
        assertTrue("update should be available", result.available)

        timeSource.addElapsedTime(5.minutes)

        result = manager.checkForUpdate(true)
        assertTrue("update should be available", result.available)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `checkForUpdate indicates that an update is available even if the update was previously ignored if 'ignoreCache' is true`() = runTest {
        val releasesRepo = mock<GithubReleasesRepo>()
        val apkInstaller = mock<APKInstaller>()

        `when`(releasesRepo.getLatestRelease()).thenReturn(ReleaseInfo("1.0.1", "update_url"))

        val currentVersion = "1.0.0".toVersion(strict = false)
        val timeSource = TestTimeSource()
        val manager = UpdateManagerImpl(currentVersion, releasesRepo, apkInstaller, File("/"), timeSource)

        var result = manager.checkForUpdate()
        assertTrue("update should be available", result.available)

        manager.ignoreUpdate(result.version)
        timeSource.addElapsedTime(5.minutes)

        result = manager.checkForUpdate(true)
        assertTrue("update should be available", result.available)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `checkForUpdate indicates that an update is NOT available if the update was previously ignored`() = runTest {
        val releasesRepo = mock<GithubReleasesRepo>()
        val apkInstaller = mock<APKInstaller>()

        val latestVersion = "1.0.1"

        `when`(releasesRepo.getLatestRelease()).thenReturn(ReleaseInfo(latestVersion, "update_url"))

        val currentVersion = "1.0.0".toVersion(strict = false)
        val timeSource = TestTimeSource()
        val manager = UpdateManagerImpl(currentVersion, releasesRepo, apkInstaller, File("/"), timeSource)

        manager.ignoreUpdate(latestVersion)

        val result = manager.checkForUpdate()
        assertTrue("update should not be available", !result.available)
    }

}

@OptIn(ExperimentalTime::class)
class TestTimeSource(var currentTime: Duration = Duration.ZERO) : TimeSource {
    override fun markNow(): TimeMark {
        return object : TimeMark {
            override fun elapsedNow(): Duration = currentTime
        }
    }
    fun addElapsedTime(duration: Duration) {
        currentTime += duration
    }
}