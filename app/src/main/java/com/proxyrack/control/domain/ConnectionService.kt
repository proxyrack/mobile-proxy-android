package com.proxyrack.control.domain

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.util.Log
import androidx.core.app.NotificationCompat
import com.proxyrack.control.BuildConfig
import com.proxyrack.control.MainActivity
import com.proxyrack.control.R
import com.proxyrack.control.data.repository.ConnectionRepo
import com.proxyrack.control.data.repository.IpInfoRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.proxyrack.control.domain.proxy_manager.ProxyManager
import com.proxyrack.control.domain.proxy_manager.ProxyManagerProvider


@AndroidEntryPoint
class ConnectionService : Service() {

    private val TAG = "ConnectionService"

    @Inject
    lateinit var connectionRepo: ConnectionRepo

    @Inject
    lateinit var ipInfoRepo: IpInfoRepository

    @Inject
    lateinit var proxyManagerProvider: ProxyManagerProvider

    private val CHANNEL_ID = "ProxyControlForegroundServiceChannel"

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private var proxyManager: ProxyManager? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        setupProxyManager()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("connection service", "onStartCommand called")
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE)

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Proxy Control")
            .setContentText("Proxy service running")
            .setSmallIcon(R.drawable.logo)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        // need to re-register since it is unregistered when 'onDestroy' is called
        registerOnDisconnectCallback()

        connect()

        return START_NOT_STICKY
    }

    private fun setupProxyManager() {
        val pid = Process.myPid().toLong()
        val androidApiVersion = Build.VERSION.SDK_INT.toString()
        val cpuArch = getCPUArchitecture()

        Log.d(TAG, "androidApiVersion $androidApiVersion")
        Log.d(TAG,"MyApp pid ${pid}")
        Log.d(TAG,"MyApp cpu arch $cpuArch")

        proxyManager = proxyManagerProvider.new()

        proxyManager!!.registerOnLogEntryCallback { msg ->
            Log.d(TAG, "logEntryCallback msg: $msg")
            connectionRepo.addLogMessage(msg)
        }

        proxyManager!!.registerOnConnectCallback {

            connectionRepo.updateConnectionStatus(ConnectionStatus.Connected)
            Log.d(TAG, "registerOnConnectCallback called")

            ipInfoRepo.getIpInfo { ip, error ->
                if (ip != null) {
                    Log.d(TAG, "IP: $ip")
                    connectionRepo.updateDeviceIP(ip)
                } else if (error != null) {
                    Log.d(TAG, error.toString())
                }
            }
        }

        registerOnDisconnectCallback()

    }

    private fun registerOnDisconnectCallback() {
        // This callback is only called if we are involuntarily disconnected. The reason is
        // that in the 'onDestroy' method of this service, we call 'unregisterOnDisconnectCallback'
        // before calling 'disconnect'
        proxyManager!!.registerOnDisconnectCallback {
            connectionRepo.updateConnectionStatus(ConnectionStatus.Disconnected)
            Log.d(TAG, "registerOnDisconnectCallback called")

            // We were involuntarily disconnected for some reason. Perhaps an error occurred.
            // Try to reconnect
            serviceScope.launch {
                // Wait 5 seconds so that we don't end up with an infinite tight loop
                connectionRepo.addLogMessage("Will attempt to reconnect in 5 seconds")
                delay(5000L)

                val connectionStatus = connectionRepo.connectionStatus.value
                if (connectionStatus != ConnectionStatus.Disconnected) {
                    // The user manually reconnected before we could perform auto reconnect
                    return@launch
                }

                connectionRepo.updateConnectionStatus(ConnectionStatus.Connecting)
                connect()
            }

        }
    }

    private fun connect() {
        // todo: Cancel connection attempt after a certain amount of time.
        // Really probably better to do that in the go library. Just ensure
        // there's a timeout after which an err is returned.
        Log.d(javaClass.simpleName, "sharing bandwidth: ${connectionRepo.sharingBandwidth.value}")
        serviceScope.launch {
            try {
                proxyManager!!.connect(BuildConfig.SERVER_IP, 443, connectionRepo.deviceID.value, connectionRepo.username.value, connectionRepo.sharingBandwidth.value)
            } catch (e: Exception) {
                Log.d("VM", "Failed to connect")
                connectionRepo.updateConnectionStatus(ConnectionStatus.Disconnected)
            }
        }
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

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("connection service", "onDestroy called")

        proxyManager?.unregisterOnDisconnectCallback()
        proxyManager?.disconnect()

    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Proxy Control Foreground Service Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(serviceChannel)
        }
    }
}
