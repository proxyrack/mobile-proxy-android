package com.proxyrack.control

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.proxyrack.control.ui.theme.ProxyControlTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.proxyrack.control.ui.navigation.Screen
import com.proxyrack.control.ui.screens.HomeScreen
import com.proxyrack.control.ui.screens.HomeViewModel
import com.proxyrack.control.ui.screens.SettingsScreen
import com.proxyrack.control.ui.screens.UpdatesViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = javaClass.simpleName

    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private var permissionResult = CompletableDeferred<Boolean>()

    private lateinit var requestBatteryLauncher: ActivityResultLauncher<Intent>
    private var batteryResult = CompletableDeferred<Unit>()

    val analyticsDialogShowing = MutableStateFlow<Boolean>(false)

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            // Makes status bar icons white
            statusBarStyle = SystemBarStyle.dark(0)
        )

        val updatesViewModel: UpdatesViewModel by viewModels()

        setContent {
            ProxyControlTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                var canNavigateBack by remember { mutableStateOf(false)}

                LaunchedEffect(backStackEntry) {
                    canNavigateBack = navController.previousBackStackEntry != null
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),

                ) { contentPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Home.route,
                        modifier = Modifier.padding(bottom = contentPadding.calculateBottomPadding())
                    ) {
                        composable(Screen.Home.route) { HomeScreen(navController) }
                        composable(Screen.Settings.route) { SettingsScreen(navController) }
                        // Add more composable destinations as needed
                    }
                }

                val showAnalyticsDialog by analyticsDialogShowing.collectAsState()
                if (showAnalyticsDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            analyticsDialogShowing.value = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    analyticsDialogShowing.value = false
                                }
                            ) {
                                Text("OK")
                            }
                        },
                        title = {
                            Text(text = "Analytics")
                        },
                        text = {
                            Text("This app uses anonymous analytics. You can opt out in the settings page.")
                        }
                    )

                }

                val showUpdateDialog by updatesViewModel.updateDialogShowing.collectAsState()
                if (showUpdateDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            updatesViewModel.ignoreUpdate()
                        },
                        dismissButton = {
                            TextButton(
                                onClick = {
                                    updatesViewModel.ignoreUpdate()
                                }
                            ) {
                                Text("No")
                            }
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    updatesViewModel.installUpdate()
                                }
                            ) {
                                Text("Yes")
                            }
                        },
                        title = {
                            Text(text = "Update Available")
                        },
                        text = {
                            Text("An update is available. It can be installed automatically. \n\nThis app will request permission to install apps. \n\nDo you want to install the update now?")
                        }
                    )

                }
            }
        }

        // These launchers are initialized here because they must be initialized before the
        // activity reaches the STARTED state. Registering them in a coroutine results in
        // them being registered too late.
        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            permissionResult.complete(isGranted)
            println("permission granted: $isGranted")
        }

        requestBatteryLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            batteryResult.complete(Unit)
            println("battery result done")
        }

        lifecycleScope.launch(Dispatchers.IO) {
            initializationTasks() // shows analytics dialog

            // wait for analytics dialog to be dismissed
            analyticsDialogShowing.first { it == false }

            if (!hasNotificationPermission()) {
                // possibly shows notification permissions dialog
                // and doesn't return until dialog is dismissed
                requestNotificationPermission()
            }

            // possibly redirect to battery optimizations settings and doesn't return
            // until complete.
            requestIgnoreBatteryOptimizations(this@MainActivity)

            // This must run on main thread since it's possible that this is the first time that
            // the delegate that creates the UpdatesViewModel lazily has been called.
            withContext(Dispatchers.Main) {
                updatesViewModel.checkForUpdate()
            }
        }

    }

    private fun hasNotificationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    }

    private suspend fun requestNotificationPermission() {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                // previous versions don't require permission to show notifications
                return
            }

            // Reset the CompletableDeferred for new permission requests
            if (permissionResult.isCompleted) {
                permissionResult = CompletableDeferred()
            }

            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            permissionResult.await()
    }

    @SuppressLint("BatteryLife")
    private suspend fun requestIgnoreBatteryOptimizations(activity: Activity) {
        // https://stackoverflow.com/a/33114136/6716264

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            // PowerManager.isIgnoringBatteryOptimizations is not available in api versions < 23
            return
        }

        val pm : PowerManager = activity.getSystemService(Context.POWER_SERVICE) as PowerManager
        if (pm.isIgnoringBatteryOptimizations(activity.packageName)) {
            Log.i(TAG, "Already ignoring battery optimizations")
            return
        }

        val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
            data = Uri.parse("package:${activity.packageName}")
        }

        // reset for new requests
        if (batteryResult.isCompleted) {
            batteryResult = CompletableDeferred()
        }

        requestBatteryLauncher.launch(intent)
        batteryResult.await()
    }

    // Checks if this is the first time the app has been run. If yes,
    // then a device ID is generated and the analytics dialog are shown.
    suspend fun initializationTasks() {
        val viewModel: HomeViewModel by viewModels()

        val previouslyInitialized = viewModel.previouslyInitialized()
        if (!previouslyInitialized) {
            analyticsDialogShowing.value = true
            val deviceID = UUID.randomUUID().toString()
            viewModel.saveDeviceID(deviceID)
            viewModel.setPreviouslyInitialized()
        }

        viewModel.initializationTasksFinished()
    }
}

