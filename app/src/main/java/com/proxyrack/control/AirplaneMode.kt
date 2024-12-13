package com.proxyrack.control

class AirplaneMode {

    fun enable() {
         Runtime.getRuntime().exec("su -c settings put global airplane_mode_on 1")
         Runtime.getRuntime().exec("su -c am broadcast -a android.intent.action.AIRPLANE_MODE --ez state true")
    }

    fun disable() {
        Runtime.getRuntime().exec("su -c settings put global airplane_mode_on 0")
        Runtime.getRuntime().exec("su -c am broadcast -a android.intent.action.AIRPLANE_MODE --ez state false")
    }

}