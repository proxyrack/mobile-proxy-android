package com.proxyrack.control.ui.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.proxyrack.control.MainViewModel
import com.proxyrack.control.R
import com.proxyrack.control.domain.ConnectionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(navController: NavController, viewModel: MainViewModel) {
    val coroutineScope = rememberCoroutineScope()

    val painter: Painter = painterResource(id = R.drawable.header_bg)

    Column(modifier = Modifier.fillMaxSize()) {
        // Header Section
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(bottomStart = 25.dp, bottomEnd = 25.dp))
            ) {
                Image(
                    painter = painter,
                    contentDescription = "Header Background",
                    modifier = Modifier.fillMaxWidth()
                )

                // Header content on top of background
                Row(
                    modifier = Modifier.padding(10.dp).fillMaxWidth().align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {

                    Column {

                        // Logo
                        Image(
                            painter = painterResource(id = R.drawable.logo_full),
                            contentDescription = "Logo",
                        )


                        // Connected Status
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .padding(top = 8.dp)
                                //.width(150.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(color = Color(0xffF0F5F5))
                                .padding(4.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.wifi_disconnected),
                                contentDescription = "Disconnected",
                                modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                            )
                            Text("Disconnected",
                                color = Color(0x66232D42),
                                modifier = Modifier.padding(end = 5.dp)
                            )
                        }
                    }

                    // Settings icon here
                    SettingsIconButton()
                }
            }
        }

        // Main Content
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            var keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current;
            val connectionStatus by viewModel.connectionStatus.collectAsState()

            var username = rememberSaveable { mutableStateOf("") }
            var deviceID = rememberSaveable { mutableStateOf("") }

            // Loads the previously saved values into the form.
            LaunchedEffect(Unit) {
                viewModel.username.collect { value ->
                    Log.d("HOME", "username value: $value")
                    if (value.isNotEmpty()) {
                        username.value = value
                        return@collect
                    }
                }
            }
            LaunchedEffect(Unit) {
                viewModel.deviceID.collect { value ->
                    Log.d("HOME", "dID value: $value")
                    if (value.isNotEmpty()) {
                        deviceID.value = value
                        return@collect
                    }
                }
            }

            var usernameErrMsg = rememberSaveable { mutableStateOf("") }
            var usernameFieldDirty = rememberSaveable { mutableStateOf(false) } // whether field has been submitted at least once

            var deviceIdErrMsg = rememberSaveable { mutableStateOf("") }
            var deviceIdFieldDirty = rememberSaveable { mutableStateOf(false) } // whether field has been submitted at least once

//        Text(
//            "Mobile Proxy Control",
//            fontSize = 26.sp,
//        )

            fun runValidation(fieldText: MutableState<String>, fieldDirty: MutableState<Boolean>, errMsg: MutableState<String>, validator: (String) -> ValidationResult): Boolean {
                Log.d("MA", "running field validation")

                val result = validator(fieldText.value)

                if (fieldDirty.value && !result.isValid) {
                    errMsg.value = result.errMsg
                } else {
                    errMsg.value = ""
                }

                return result.isValid
            }
            fun runUsernameValidation(): Boolean {
                return runValidation(username, usernameFieldDirty, usernameErrMsg, ::usernameValidator)
            }
            fun runDeviceIdValidation(): Boolean {
                return runValidation(deviceID, deviceIdFieldDirty, deviceIdErrMsg, ::deviceIdValidator)
            }
            TitledColumn("Settings") {
                StyledTextField(
                    "Account Username",
                    value = username.value,
                    onValueChange = {
                        username.value = it
                        runUsernameValidation()
                    },
                    onDone = {
                        usernameFieldDirty.value = true
                        runUsernameValidation()
                    },
                    enabled = connectionStatus == ConnectionStatus.Disconnected,
                    isError = usernameErrMsg.value.isNotEmpty(),
                    modifier = Modifier.padding(start = 16.dp, top = 35.dp, end = 16.dp).fillMaxWidth(),
                )
                if (usernameErrMsg.value.isNotEmpty()) {
                    Text(
                        usernameErrMsg.value,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                }
                StyledTextField(
                    "Your Device ID",
                    value = deviceID.value,
                    onValueChange = {
                        deviceID.value = it
                        runDeviceIdValidation()
                    },
                    onDone = {
                        deviceIdFieldDirty.value = true
                        runDeviceIdValidation()
                    },
                    enabled = connectionStatus == ConnectionStatus.Disconnected,
                    isError = deviceIdErrMsg.value.isNotEmpty(),
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp).fillMaxWidth())
                if (deviceIdErrMsg.value.isNotEmpty()) {
                    Text(
                        deviceIdErrMsg.value,
                        color = Color.Red,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp)
                    )
                }
                val deviceIP = viewModel.deviceIP.collectAsState()
                // Device IP Display Field
                StyledTextField(
                    "Your Device IP",
                    value = deviceIP.value,
                    onValueChange = {

                    },
                    enabled = false,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, end = 16.dp).fillMaxWidth())
                SetupInstructionsLink(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp, bottom = 30.dp))
            }

            val buttonColor = when (connectionStatus) {
                ConnectionStatus.Connecting -> colorFromHex("#49de7d")
                ConnectionStatus.Connected -> colorFromHex("#f5524c")
                ConnectionStatus.Disconnected -> colorFromHex("#49de7d")
            }

            Button(
                onClick = {
                    // The user may click the connect button without first closing the
                    // keyboard. Go ahead and close it for them if that's the case.
                    keyboardController?.hide()

                    usernameFieldDirty.value = true
                    deviceIdFieldDirty.value = true
                    val formValid = runUsernameValidation() && runDeviceIdValidation()
                    if (!formValid) {
                        return@Button
                    }

                    coroutineScope.launch(Dispatchers.IO) {
                        viewModel.saveUsername(username.value)
                        viewModel.saveDeviceID(deviceID.value)
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

            val logMessages by viewModel.logMessages.collectAsState()
            LogsColumn(
                title = "Logs",
                logMessages = logMessages,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {


    }

}

@Composable
fun SettingsIconButton() {
    IconButton(
        onClick = { /* Handle the click event */ },
        modifier = Modifier
            .background(color = Color(0xff232D42), shape = RoundedCornerShape(8.dp))
            .size(42.dp) // Adjust the size as needed
    ) {
        Icon(
            imageVector = Icons.Default.Settings,
            contentDescription = "Settings",
            tint = Color.White,
            modifier = Modifier.size(28.dp)
        )
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
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {}, // should be a function that saves the value to store
    enabled: Boolean = true,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    var purple = Color(0xff4A28C6)
    var red = Color(0xffE8132C)
    // Set colors so that even if a text field is disabled, it will
    // have the same colors as an enabled text field.
    var colors = OutlinedTextFieldDefaults.colors(
        unfocusedBorderColor = Color(0x33232D42),
        focusedBorderColor = purple,
        focusedLabelColor = purple,
        unfocusedLabelColor = Color(0x99232D42),
        errorLabelColor = red,
        errorBorderColor = red
    )
    if (!enabled) {
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = colors.unfocusedTextColor,
            disabledLabelColor = colors.unfocusedLabelColor,
        )
    }

    val keyboardOptions = keyboardOptions.copy(imeAction = ImeAction.Done)

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        modifier = modifier.focusRequester(focusRequester),
        shape = RoundedCornerShape(14.dp),
        onValueChange = onValueChange,
        value = value,
        label = { Text(label) },
        colors = colors,
        enabled = enabled,
        singleLine = true,
        isError = isError,
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

data class ValidationResult(val isValid: Boolean, val errMsg: String)

val usernameRegex = "^[a-zA-Z0-9]{4,15}$".toRegex()

fun usernameValidator(text: String): ValidationResult {
    val valid = usernameRegex.matches(text)

    var errMsg = ""
    if (!valid) {
        errMsg = "Username must be 4-15 characters long with no special characters"
    }

    return ValidationResult(valid, errMsg)
}

val deviceIdRegex = "^[a-zA-Z0-9-]{4,40}$".toRegex()

fun deviceIdValidator(text: String): ValidationResult {
    val valid = deviceIdRegex.matches(text)

    var errMsg = ""
    if (!valid) {
        errMsg = "Device ID must be 4-40 characters long with no special characters other than dashes"
    }

    return ValidationResult(valid, errMsg)
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