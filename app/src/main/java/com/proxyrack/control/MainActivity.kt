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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = javaClass.simpleName

    private val showAnalyticsDialog = mutableStateOf(false)

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            // Makes status bar icons white
            statusBarStyle = SystemBarStyle.dark(0)
        )
        setContent {
            ProxyControlTheme {
                val viewModel: HomeViewModel by viewModels()
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
                        composable(Screen.Home.route) { HomeScreen(navController, viewModel) }
                        composable(Screen.Settings.route) { SettingsScreen(navController) }
                        // Add more composable destinations as needed
                    }
                }

                val showDialog by showAnalyticsDialog
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showAnalyticsDialog.value = false
                        },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    showAnalyticsDialog.value = false
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
            }
        }

        initializationTasks()
        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // previous versions don't require permission to show notifications
            return
        }

        // Initialize the permission launcher
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (!isGranted) {
                // Permission is granted. You can show notifications.
            } else {
                // Permission is denied. Handle accordingly.
            }

            // We want to request to ignore battery optimizations after requesting notifications
            // permission regardless of whether the user clicked yes or no.
            requestIgnoreBatteryOptimizations(this@MainActivity)
        }

        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED -> {
                // Permission is already granted. Can show notification.

                // We want to request to ignore battery optimizations regardless even
                // if the user already granted notification permission on a previous app run.
                requestIgnoreBatteryOptimizations(this@MainActivity)
            }
            else -> {
                // Request the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

    }

    // Returns whether the app is already ignoring battery optimizations
    @SuppressLint("BatteryLife")
    fun requestIgnoreBatteryOptimizations(activity: Activity) {
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

        val intent = Intent()
        intent.action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
        val uriString = "package:${activity.packageName}"
        Log.d(TAG, "uri string: $uriString")
        intent.data = Uri.parse(uriString)

        activity.startActivity(intent)
    }

    // Checks if this is the first time the app has been run. If yes,
    // then a device ID is generated and the analytics dialog are shown.
    fun initializationTasks() {
        val viewModel: HomeViewModel by viewModels()
        lifecycleScope.launch(Dispatchers.IO) {
            val previouslyInitialized = viewModel.previouslyInitialized()
            if (!previouslyInitialized) {
                runOnUiThread {
                    showAnalyticsDialog.value = true
                }
                val deviceID = UUID.randomUUID().toString()
                viewModel.saveDeviceID(deviceID)
                viewModel.setPreviouslyInitialized()
            }

            viewModel.initializationTasksFinished()
        }
    }
}

