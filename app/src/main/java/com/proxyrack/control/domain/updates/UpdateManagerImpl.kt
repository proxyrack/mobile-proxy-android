package com.proxyrack.control.domain.updates

import android.util.Log
import com.proxyrack.control.domain.model.ReleaseInfo
import com.proxyrack.control.domain.repository.GithubReleasesRepo
import io.github.z4kn4fein.semver.Version
import io.github.z4kn4fein.semver.toVersion
import kotlin.time.Duration.Companion.hours
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource


// Use this to inject current version:
// BuildConfig.VERSION_NAME.toVersion(strict = false)

class UpdateManagerImpl (
    private val currentVersion: Version,
    private val releasesRepo: GithubReleasesRepo,
    private val apkInstaller: APKInstaller,
    private val timeSource: TimeSource = TimeSource.Monotonic,
    ): UpdateManager {

    private var lastCheckedAt: TimeMark? = null
    private var previouslyIgnoredVersion: String = "0"

    // If bypassIgnoredVersionCheck is true, then this method will return an update regardless
    // of whether that update has been previously ignored.
    // If ignoreCache is true, then available updates will always be returned even
    // if we already checked less than 24 hours ago.
    // Both args are false by default.
    @OptIn(ExperimentalTime::class)
    override suspend fun checkForUpdate(ignoreCache: Boolean): UpdateDetails {
        if (!ignoreCache && lastCheckedAt != null && lastCheckedAt!!.elapsedNow() < 24.hours) {
            return UpdateDetails(available = false, version = "", url = "")
        }

        val releaseInfo: ReleaseInfo
        try {
            releaseInfo = releasesRepo.getLatestRelease()
        } catch (e: Exception) {
            Log.e(javaClass.simpleName, "failed update check: $e")
            return UpdateDetails(
                available = false,
                version = "",
                url = "",
            )
        }

        lastCheckedAt = timeSource.markNow()

        val latestReleaseVersion = releaseInfo.version.toVersion(strict = false)

        if (!ignoreCache && latestReleaseVersion == previouslyIgnoredVersion.toVersion(strict = false)) {
            return UpdateDetails(
                available = false,
                version = "",
                url = "",
            )
        }

        var updateAvailable = false
        if (latestReleaseVersion > currentVersion) {
            updateAvailable = true
        }

        var url = ""
        if (updateAvailable) {
            url = releaseInfo.url
        }

        return UpdateDetails(available = updateAvailable, version = releaseInfo.version, url)
    }

    override fun installUpdate(url: String) {
        // download update

        //apkInstaller.install()
    }

    override fun ignoreUpdate(version: String) {
        previouslyIgnoredVersion = version
    }

}

