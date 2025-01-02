package com.proxyrack.control.domain.repository

import com.proxyrack.control.domain.model.ReleaseInfo

interface GithubReleasesRepo {
    suspend fun getLatestRelease(): ReleaseInfo
}