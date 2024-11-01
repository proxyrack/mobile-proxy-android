package com.proxyrack.control

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.proxyrack.control.data.repository.ConnectionRepo
import com.proxyrack.control.data.repository.IpInfoRepository
import com.proxyrack.control.domain.ConnectionStatus
import com.proxyrack.proxylib.android.Android.newManager
import com.proxyrack.proxylib.android.Manager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.util.concurrent.atomic.AtomicBoolean


@AndroidEntryPoint
class ConnectionService : Service() {

    private val TAG = "ConnectionService"

    @Inject
    lateinit var connectionRepo: ConnectionRepo

    @Inject
    lateinit var ipInfoRepo: IpInfoRepository

    private val CHANNEL_ID = "ProxyControlForegroundServiceChannel"

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    private lateinit var proxyManager: Manager;

    // If a disconnect happens and it was the result of an error or temporary
    // loss of internet connection, and not at the request of the user,
    // we will attempt to automatically reconnect.
    private var disconnectRequestedByUser = AtomicBoolean(false);

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
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

        setupProxyManager()
        connect()

        return START_STICKY
    }

    private fun setupProxyManager() {
        val pid = android.os.Process.myPid().toLong()
        val androidApiVersion = Build.VERSION.SDK_INT.toString()
        val cpuArch = getCPUArchitecture()

        Log.d(TAG, "androidApiVersion $androidApiVersion")
        Log.d(TAG,"MyApp pid ${pid}")
        Log.d(TAG,"MyApp cpu arch $cpuArch")

        proxyManager = newManager(pid, "55", androidApiVersion, cpuArch)

        proxyManager.registerOnConnectCallback {

            connectionRepo.updateConnectionStatus(ConnectionStatus.Connected)
            Log.d(TAG, "registerOnConnectCallback called")

            ipInfoRepo.getIpInfo { info, error ->
                if (info != null) {
                    Log.d(TAG, "IP: ${info.ip}")
                    connectionRepo.updateDeviceIP(info.ip)
                } else if (error != null) {
                    Log.d(TAG, error.toString())
                }
            }
        }

        proxyManager.registerOnDisconnectCallback {
            connectionRepo.updateConnectionStatus(ConnectionStatus.Disconnected)
            Log.d(TAG, "registerOnDisconnectCallback called")

            if (!disconnectRequestedByUser.get()) {
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

        proxyManager.registerOnLogEntryCallback { msg ->
            connectionRepo.addLogMessage(msg)
        }
    }

    private fun connect() {
        // todo: Cancel connection attempt after a certain amount of time.
        // Really probably better to do that in the go library. Just ensure
        // there's a timeout after which an err is returned.
        serviceScope.launch {
            try {
                proxyManager.connect(BuildConfig.SERVER_IP, 443, connectionRepo.deviceID.value, connectionRepo.username.value)
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
        disconnectRequestedByUser.set(true)
        proxyManager.disconnect()
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
