package com.proxyrack.control.domain.repository

interface SettingsRepo {
    val deviceID: DataAccessor
    val username: DataAccessor
    val initialized: DataAccessor // whether the app has run at least once
    val analyticsEnabled: DataAccessor
    val ipRotationInterval: DataAccessor
    val bandwidthSharingEnabled: DataAccessor
}


