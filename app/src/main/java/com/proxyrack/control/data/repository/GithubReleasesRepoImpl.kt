package com.proxyrack.control.data.repository

import com.proxyrack.control.data.network.GithubReleasesService
import com.proxyrack.control.domain.model.ReleaseInfo
import com.proxyrack.control.domain.repository.GithubReleasesRepo

class GithubReleasesRepoImpl(private val apiService: GithubReleasesService): GithubReleasesRepo {
//    private val apiService: GithubReleasesService = GithubReleasesRetrofit.retrofit.create(
//        GithubReleasesService::class.java)

    override suspend fun getLatestRelease(): ReleaseInfo {
        try {
            val response = apiService.getReleases()
            if (response.isSuccessful) {
                val data = response.body()
                if (data == null || data.isEmpty()) {
                    throw Exception("Error: No release data returned")
                }

                val tagName = data[0].tag_name
                val latestVersion = tagName.substring(1) // strip the 'v'
                return ReleaseInfo(
                    version = latestVersion,
                    url = "https://github.com/proxyrack/mobile-proxy-android/releases/download/$tagName/app-release.apk")

            } else {
                throw Exception("Error: ${response.code()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }
}