package com.proxyrack.control.domain

class AirplaneModeImpl: AirplaneMode {

    override fun enable() {
        try {
            Runtime.getRuntime().exec("su -c settings put global airplane_mode_on 1 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")
        } catch (e: Exception) {
            // Not rooted or super user privileges not granted
            println(e)
        }
    }

    override fun disable() {
        try {
            Runtime.getRuntime().exec("su -c settings put global airplane_mode_on 0 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false")
        } catch (e: Exception) {
            // Not rooted or super user privileges not granted
            println(e)
        }
    }

}