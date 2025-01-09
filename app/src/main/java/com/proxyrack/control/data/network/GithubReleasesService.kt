package com.proxyrack.control.data.network
import retrofit2.Response
import retrofit2.http.GET

data class GithubReleasesResponseData(
    val tag_name: String,
)

// download url: https://github.com/proxyrack/mobile-proxy-android/releases/download/v1.3.5/app-release.apk

interface GithubReleasesService {
    @GET("/repos/proxyrack/mobile-proxy-android/releases")
    suspend fun getReleases(): Response<List<GithubReleasesResponseData>>
}