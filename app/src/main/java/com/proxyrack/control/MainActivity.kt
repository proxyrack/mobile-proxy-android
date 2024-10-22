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
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.proxyrack.control.ui.theme.ProxyControlTheme
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.core.content.ContextCompat
import com.proxyrack.control.domain.ConnectionStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    @OptIn(ExperimentalMaterial3Api::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProxyControlTheme {
                val viewModel: MainViewModel by viewModels()
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.logo),
                                        contentDescription = "Logo",
                                    )
                                    Text(" Proxyrack")
                            } },
                        )
                    }
                ) { contentPadding ->
                    Column(
                        modifier = Modifier.fillMaxSize().padding(contentPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Mobile Proxy Control",
                            fontSize = 26.sp,
                        )
                        TitledColumn("Settings") {
                            // Server IP Field
                            StyledTextField(
                                "Account Username",
                                value = viewModel.username,
                                onValueChange = {
                                    viewModel.updateUsername(it)
                                },
                                onDone = {
                                    viewModel.saveUsername()
                                },
                                modifier = Modifier.padding(start = 16.dp, top = 35.dp, end = 16.dp).fillMaxWidth()
                            )
                            StyledTextField(
                                "Your Device ID",
                                value = viewModel.deviceID,
                                onValueChange = {
                                    viewModel.updateDeviceID(it)
                                },
                                onDone = {
                                    viewModel.saveDeviceID()
                                },
                                modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp).fillMaxWidth())
                            // Device IP Display Field
                            StyledTextField(
                                "Your Device IP",
                                value = viewModel.deviceIP,
                                onValueChange = {

                                },
                                enabled = false,
                                modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp).fillMaxWidth())
                            SetupInstructionsLink(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp, bottom = 30.dp))
                        }
                        val connectionStatus by viewModel.connectionStatus.collectAsState()

                        val buttonColor = when (connectionStatus) {
                            ConnectionStatus.Connecting -> colorFromHex("#49de7d")
                            ConnectionStatus.Connected -> colorFromHex("#f5524c")
                            ConnectionStatus.Disconnected -> colorFromHex("#49de7d")
                        }

                        val username by viewModel.username.collectAsState()
                        val deviceID by viewModel.deviceID.collectAsState()

                        Button(
                            onClick = {
                                if (username.isEmpty() || deviceID.isEmpty()) {
                                    viewModel.updateShowFormError(true)
                                    return@Button
                                }
                                viewModel.connectionButtonClicked()
                            },
                            modifier = Modifier
                                .padding(start = 28.dp, end = 28.dp)
                                .fillMaxWidth()
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors().copy(containerColor = buttonColor)
                        ) {


                            val buttonText = when (connectionStatus) {
                                ConnectionStatus.Connecting -> "Connecting..."
                                ConnectionStatus.Connected -> "Disconnect"
                                ConnectionStatus.Disconnected -> "Connect"
                            }

                            Text(
                                buttonText,
                                style = TextStyle(
                                    fontSize = 19.sp
                                ),
                                )
                        }

                        val showFormError by viewModel.showFormError.collectAsState()
                        if (showFormError) {
                            Text(
                                "Username and Device ID are required",
                                color = Color.Red,
                                modifier = Modifier.padding(top = 5.dp)
                            )
                        }

                        val logMessages by viewModel.logMessages.collectAsState()
                        LogsColumn(
                            title = "Logs",
                            logMessages = logMessages,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                }
            }
        }

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
}

// https://stackoverflow.com/a/69549929/6716264
@Composable
fun SetupInstructionsLink(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val annotatedString = buildAnnotatedString {
        pushStringAnnotation(tag = "link", annotation = "https://proxyrack.com/mobile-proxies/")
        withStyle(
            // It seems the underline style is being overridden somehow.
            // When the app is opened in the emulator, the underline is visible for a split second.
            style = SpanStyle(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
                fontSize = 18.sp,
            )
        ) {
            append("Setup Instructions")
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "link", start = offset, end = offset).firstOrNull()?.let {
                // Log.d("link URL", it.item)

                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it.item))
                context.startActivity(intent)
            }
    })
}

@Composable
fun TitledColumn(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier.padding(16.dp)
    ) {
        Column(
            modifier = modifier
                .padding(10.dp)
                .border(
                    width = 2.dp,
                    color = Color.Black,
                    shape = RoundedCornerShape(5.dp)
                )
        ) {
            content()
        }
        Box(
            // Having 2 calls to 'padding' looks a bit confusing.
            // But remember that each modifier method call operates on the
            // result of the previous call.
            modifier = Modifier
                .padding(start = 20.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 5.dp, end = 5.dp)
        ) {
            Text(title)
        }
    }
}

@Composable
fun LogsColumn(
    title: String,
    modifier: Modifier = Modifier,
    logMessages: List<String>
) {
    // Create a LazyListState to control the scroll position
    val listState = rememberLazyListState()

    // Launch a coroutine to scroll to the bottom whenever items change
    LaunchedEffect(logMessages.size) {
        // Scroll to the last item
        if (logMessages.isNotEmpty()) {
            listState.animateScrollToItem(logMessages.size - 1)
        }
    }

    Box(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 30.dp)
    ) {

        Box(
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp).border(
                width = 2.dp,
                color = Color.Black,
                shape = RoundedCornerShape(5.dp)
            )
        ) {
            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = modifier
                    .padding(top = 20.dp)

            ) {
                itemsIndexed(logMessages) { index, msg ->
                    //val topPadding = if (index == 0) 15.dp else 0.dp
                    Text(msg, modifier = Modifier.padding(
                        //top = topPadding,
                        start = 10.dp,
                        end = 10.dp))
                }
            }
        }

        Box(
            // Having 2 calls to 'padding' looks a bit confusing.
            // But remember that each modifier method call operates on the
            // result of the previous call.
            modifier = Modifier
                .padding(start = 20.dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(start = 5.dp, end = 5.dp)
        ) {
            Text(title, modifier = Modifier.padding(vertical = 0.dp))
        }
    }
}

@Composable
fun StyledTextField(
    label: String,
    value: StateFlow<String>,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {}, // should be a function that saves the value to store
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
                    ) {

    // Set colors so that even if a text field is disabled, it will
    // have the same colors as an enabled text field.
    var colors = TextFieldDefaults.colors()
    if (!enabled) {
        colors = TextFieldDefaults.colors(
            disabledTextColor = colors.unfocusedTextColor,
            disabledLabelColor = colors.unfocusedLabelColor,
        )
    }

    val keyboardOptions = keyboardOptions.copy(imeAction = ImeAction.Done)

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val textValue by value.collectAsState()

    TextField(
        modifier = modifier.focusRequester(focusRequester),
        onValueChange = onValueChange,
        value = textValue,
        label = { Text(label) },
        colors = colors,
        enabled = enabled,
        singleLine = true,
        keyboardOptions = keyboardOptions,
        keyboardActions = KeyboardActions(
            onDone = {
                // Removes focus from the TextField after the enter key is pressed on the keyboard
                focusManager.clearFocus()
                onDone()
            }
        )
    )
}

fun colorFromHex(hex: String): Color {
    // Remove the hash if it's there
    val cleanHex = hex.removePrefix("#")
    // Parse the color, assuming it's in the format RRGGBB or AARRGGBB
    val colorInt = cleanHex.toLong(16)
    return if (cleanHex.length == 6) {
        // If it's RRGGBB, add the alpha value
        Color(colorInt or 0x00000000FF000000)
    } else {
        // If it's AARRGGBB, use it directly
        Color(colorInt)
    }
}