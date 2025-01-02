package com.proxyrack.control.domain.updates


/*
* Goals:
* - Check for update when app is launched. If an update is available, download the update, then show
* a dialog to the user offering to install it.
* - If the user refuses an update, show it in the settings.
* -
* */

data class UpdateDetails(val available: Boolean, val version: String, val url: String)

interface UpdateManager {
    suspend fun checkForUpdate(ignoreCache: Boolean = false): UpdateDetails
    fun installUpdate(url: String)
    fun ignoreUpdate(version: String)
}