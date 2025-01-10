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
import java.io.File
import java.io.IOException

// Use this to inject current version:
// BuildConfig.VERSION_NAME.toVersion(strict = false)

class UpdateManagerImpl (
    private val currentVersion: Version,
    private val releasesRepo: GithubReleasesRepo,
    private val apkInstaller: APKInstaller,
    private val storagePath: File, // should be cache dir. inject it so that we don't need to pass a context to a method.
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
                version = "0", // good practise to always have a value to prevent version parsing lib from crashing
                url = "",
            )
        }

        lastCheckedAt = timeSource.markNow()

        val latestReleaseVersion = releaseInfo.version.toVersion(strict = false)
        Log.d(javaClass.simpleName, "about to check against previouslyIgnoredVersion: $previouslyIgnoredVersion")
        if (!ignoreCache && latestReleaseVersion == previouslyIgnoredVersion.toVersion(strict = false)) {
            return UpdateDetails(
                available = false,
                version = "0", // good practise to always have a value to prevent version parsing lib from crashing
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

    // Recommended to pass context.getCacheDir() for storagePath
    // Tries to download update file and offer to install it.
    // @throws IOException if file fails to download
    override fun installUpdate(url: String, version: String) {
        val outputFile = File(storagePath, "proxy-control-${version}.apk")

        if (!outputFile.exists()) {
            Log.d(javaClass.simpleName, "File does not exist. downloading.")
            try {
                downloadFile(url, outputFile)
                println("File downloaded successfully to ${outputFile.absolutePath}")
            } catch (e: IOException) {
                println("Failed to download file: ${e.message}")
                throw e
            }
        } else {
            Log.d(javaClass.simpleName, "File already existed. not downloading.")
        }

        apkInstaller.install(outputFile)
    }

    override fun ignoreUpdate(version: String) {
        Log.d(javaClass.simpleName, "ignoring version: $version")
        previouslyIgnoredVersion = version
    }

}

