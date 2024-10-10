package com.proxyrack.control.data.repository

import android.content.Context
import com.proxyrack.control.domain.repository.DataAccessor
import com.proxyrack.control.domain.repository.SettingsRepo

class SettingsRepoImpl(
    override val deviceID: DataAccessor,
    override val serverIP: DataAccessor,
    private val context: Context,
    ) : SettingsRepo {
}