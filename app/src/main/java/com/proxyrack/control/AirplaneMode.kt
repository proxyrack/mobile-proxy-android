package com.proxyrack.control

class AirplaneMode {

    // Checks if phone is rooted, but not whether the app has been granted super user rights.
    fun isRooted(): Boolean {
        try {
            Runtime.getRuntime().exec("su")
        } catch (e: Exception) {
            return false
        }
        return true
    }

    fun enable() {
        try {
            Runtime.getRuntime().exec("su -c settings put global airplane_mode_on 1 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")
        } catch (e: Exception) {
            // Not rooted or super user privileges not granted
            println(e)
        }

    }

    fun disable() {
        try {
            Runtime.getRuntime().exec("su -c settings put global airplane_mode_on 0 && am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false")
        } catch (e: Exception) {
            // Not rooted or super user privileges not granted
            println(e)
        }

    }

}