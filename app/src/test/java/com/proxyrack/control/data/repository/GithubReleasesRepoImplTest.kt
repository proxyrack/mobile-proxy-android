package com.proxyrack.control.data.repository

import com.proxyrack.control.data.network.GithubReleasesService
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class GithubReleasesRepoImplTest {

    private lateinit var mockWebServer: MockWebServer
    private lateinit var apiService: GithubReleasesService
    private lateinit var repo: GithubReleasesRepoImpl

    @Before
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()

        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(GithubReleasesService::class.java)
        repo = GithubReleasesRepoImpl(apiService)
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
    }

    @Test
    fun `test getLatestRelease returns correct ReleaseInfo`() = runBlocking {
        // Arrange
        val mockResponse = MockResponse()
            .setResponseCode(200)
            .setBody("[{\"tag_name\": \"v1.2.3\"}]")
        mockWebServer.enqueue(mockResponse)

        // Act
        val releaseInfo = repo.getLatestRelease()

        // Assert
        assertNotNull(releaseInfo)
        assertEquals("1.2.3", releaseInfo.version)
        assertEquals("https://github.com/proxyrack/mobile-proxy-android/releases/download/v1.2.3/app-release.apk", releaseInfo.url)
    }
}

