package com.proxyrack.control.domain

import kotlinx.coroutines.Job

interface IPRotator {
    var job: Job?
    fun setRotationInterval(minutes: Int)
    fun startRotationJob()
    fun rotateOffSchedule()
    fun stopRotationJob()
}