package com.proxyrack.control

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// This is allows dagger hilt to inject Application into deps in AppModule.kt
@HiltAndroidApp
class MyApp: Application() {
}