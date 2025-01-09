package com.proxyrack.control.domain.updates

import java.io.File

interface APKInstaller {
    fun install(file: File)
}