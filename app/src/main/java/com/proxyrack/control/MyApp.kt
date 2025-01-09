package com.proxyrack.control

import android.app.Application
import android.util.Log
import com.posthog.PostHog
import dagger.hilt.android.HiltAndroidApp
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig
import com.proxyrack.control.data.repository.AnalyticsStatusNotifier
import com.proxyrack.control.domain.repository.SettingsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

// This is allows dagger hilt to inject Application into deps in AppModule.kt
@HiltAndroidApp
class MyApp: Application() {
    private val TAG = this.javaClass.simpleName

    @Inject lateinit var settingsRepo: SettingsRepo

    @Inject lateinit var analyticsStatusNotifier: AnalyticsStatusNotifier

    companion object {
        const val POSTHOG_API_KEY = "phc_OMtM4aGLx55rXhhy3tnUB3ZjEaHrGK5CNvTPvbW0786"
        // usually 'https://us.i.posthog.com' or 'https://eu.i.posthog.com'
        const val POSTHOG_HOST = "https://us.i.posthog.com"
    }

    override fun onCreate() {
        super.onCreate()
        setupAnalytics()
    }

    private fun setupAnalytics() {
        val config = PostHogAndroidConfig(
            apiKey = POSTHOG_API_KEY,
            host = POSTHOG_HOST,
        ).apply {
            optOut = true
        }
        PostHogAndroid.setup(this@MyApp, config)

        CoroutineScope(Dispatchers.IO).launch {
            analyticsStatusNotifier.analyticsStatus.collect { enabled ->
                if (enabled && !BuildConfig.DEBUG) {
                    PostHog.optIn()
                    PostHog.capture(event = "Analytics enabled")
                    Log.i(TAG, "Analytics enabled")
                } else {
                    PostHog.optOut()
                    Log.i(TAG, "Analytics disabled")
                }
            }
        }

        CoroutineScope(Dispatchers.IO).launch {
            // check if user has analytics enabled
            if (settingsRepo.analyticsEnabled.get().lowercase() == "false") {
                Log.i(TAG, "Analytics disabled")
                analyticsStatusNotifier.notifyStatus(false)
            } else {
                Log.i(TAG, "Analytics enabled")
                analyticsStatusNotifier.notifyStatus(true)
            }
        }
    }

}