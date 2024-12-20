package com.proxyrack.control.domain

interface IPRotator {
    fun setRotationInterval(minutes: Int)
    fun startRotationJob()
    fun rotate()
    fun stopRotationJob()
}