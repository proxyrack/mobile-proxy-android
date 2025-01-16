package com.proxyrack.control.domain.proxy_manager

import android.os.Build
import android.os.Process

import com.proxyrack.proxylib.android.Android.newManager

class ProxyManagerProviderImpl: ProxyManagerProvider {
    val pid = Process.myPid().toLong()
    val androidApiVersion = Build.VERSION.SDK_INT.toString()
    val cpuArch = getCPUArchitecture()
    val version = "55"

    override fun new(): ProxyManager {
        return ProxyManagerAdapter(newManager(pid, version, androidApiVersion, cpuArch))
    }

    private fun getCPUArchitecture(): String {
        // Using the supported ABIs to determine the CPU architecture
        val supportedABIs = Build.SUPPORTED_ABIS

        if (supportedABIs.isNotEmpty()) {
            // Typically, the first one is the ABI of the current device.
            val abi = supportedABIs[0]
            return when {
                abi.startsWith("arm64") -> "arm64"
                abi.startsWith("armeabi") -> "armeabi"
                abi.startsWith("x86_64") -> "x86_64"
                abi.startsWith("x86") -> "x86"
                else -> "unknown"
            }
        }
        return "unknown"
    }
}