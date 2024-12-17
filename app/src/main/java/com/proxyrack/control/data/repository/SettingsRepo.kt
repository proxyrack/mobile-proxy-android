package com.proxyrack.control.data.repository

import com.proxyrack.control.domain.repository.DataAccessor
import com.proxyrack.control.domain.repository.SettingsRepo

class SettingsRepoImpl(
    override val deviceID: DataAccessor,
    override val username: DataAccessor,
    override val initialized: DataAccessor,
    override val analyticsEnabled: DataAccessor,
    override val ipRotationInterval: DataAccessor,
    ) : SettingsRepo {
}