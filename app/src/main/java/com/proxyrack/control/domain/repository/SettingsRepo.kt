package com.proxyrack.control.domain.repository

interface SettingsRepo {
    val deviceID: DataAccessor
    val serverIP: DataAccessor
}


