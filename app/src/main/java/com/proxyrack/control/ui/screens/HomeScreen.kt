package com.proxyrack.control.ui.screens

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.proxyrack.control.R
import com.proxyrack.control.core.util.TestTags
import com.proxyrack.control.ui.navigation.Screen
import com.proxyrack.control.domain.ConnectionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

var purple = Color(0xff4A28C6)

@Composable
fun HomeScreen(navController: NavController, viewModel: HomeViewModel = hiltViewModel()) {
    val coroutineScope = rememberCoroutineScope()
    var scrollState = rememberScrollState()
    var showBottomSheet = remember { mutableStateOf(false) }
    val logMessages by viewModel.logMessages.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {

        Header(navController, viewModel)

        // Main Content
        Column(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp).fillMaxSize().verticalScroll(scrollState),
        ) {
            Text(
                "Mobile Proxy Control",
                fontWeight = FontWeight.W700,
                fontSize = 20.sp,
                color = purple,
                modifier = Modifier.padding(top = 20.dp)
            )

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

            Text("Settings",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top=20.dp, bottom = 10.dp))

            StyledTextField(
                "Username",
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
                modifier = Modifier.fillMaxWidth(),
            )
            if (usernameErrMsg.value.isNotEmpty()) {
                Text(
                    usernameErrMsg.value,
                    color = Color.Red,
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
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth().testTag(TestTags.DEVICE_ID))
            if (deviceIdErrMsg.value.isNotEmpty()) {
                Text(
                    deviceIdErrMsg.value,
                    color = Color.Red,
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
                modifier = Modifier.padding(top = 20.dp).fillMaxWidth())

            IPRotationRow(viewModel)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                var lineColor = Color(0x33232D42)
                // Left line
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(lineColor)
                )
                SetupInstructionsLink(modifier = Modifier.padding(top = 30.dp, bottom = 30.dp, start = 5.dp, end = 5.dp))
                // Right line
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(1.dp)
                        .background(lineColor)
                )
            }

            val buttonColor = when (connectionStatus) {
                ConnectionStatus.Connecting -> colorFromHex("#4A28C6")
                ConnectionStatus.Connected -> colorFromHex("#232D42")
                ConnectionStatus.Disconnected -> colorFromHex("#4A28C6")
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
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors().copy(containerColor = buttonColor),
                shape = RoundedCornerShape(5.dp)
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

            LogsButton(showBottomSheet)

        }

        LogsBottomSheet(logMessages, showBottomSheet)
    }

}

@Composable
fun IPRotationRow(viewModel: HomeViewModel) {
    val isRooted = viewModel.isRooted()

    if (!isRooted) {
        QuestionMarkCircleButton()
    } else {
        Row(modifier = Modifier.height(20.dp)) {  } // just a spacer
    }

    // Rotation interval dropdown and "Rotate Now" button
    Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            RotationTimeDropdown(viewModel, enabled = isRooted)

            RotateIPButton(viewModel, enabled = isRooted)
    }
}

@Composable
fun QuestionMarkCircleButton() {
    var showDialog = rememberSaveable { mutableStateOf(false) }

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
    ) {
        //Text("Root Required", modifier = Modifier.padding(end = 10.dp))
        IconButton(
            onClick = {
                showDialog.value = true
            },
            modifier = Modifier
                .width(30.dp)
                .height(30.dp)
                .background(color = Color(0xff232D42), shape = CircleShape)
                .clip(CircleShape)
        ) {
            Text("?", color = Color.White)
        }
    }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                showDialog.value = false
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog.value = false
                    }
                ) {
                    Text("OK")
                }
            },
            title = {
                Text(text = "Root Required")
            },
            text = {
                Text("IP rotation works by disconnecting and reconnecting to the cell tower. " +
                        "Airplane mode can only be toggled automatically on a rooted device.")
            }
        )

    }
}

@Composable
fun RotateIPButton(viewModel: HomeViewModel, enabled: Boolean = true) {
    var borderColor = Color.Black
    var textColor = Color.Black

    if (!enabled) {
        borderColor = Color(0x33232D42)
        textColor = Color.Gray
    }

    Button(
        onClick = {
            if (!enabled) return@Button

            CoroutineScope(Dispatchers.IO).launch {
                viewModel.rotateIP()
            }

        },
        colors = ButtonDefaults.buttonColors().copy(containerColor = Color.Transparent),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                border = BorderStroke(1.dp, borderColor),
                shape = RoundedCornerShape(15.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Rotate",
                tint = textColor,
                modifier = Modifier.size(20.dp)
            )
            Text(
                "Rotate Now",
                color = textColor,
                style = TextStyle(fontSize = 17.sp),
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RotationTimeDropdown(viewModel: HomeViewModel, enabled: Boolean = true, modifier: Modifier = Modifier) {
    val options: List<String> = listOf("Disabled", "1 min", "3 min", "5 min", "10 min", "15 min", "30 min")
    var expanded = rememberSaveable() { mutableStateOf(false) }
    val textFieldState = rememberTextFieldState(options[0])

    // load previously saved value
    LaunchedEffect(Unit) {
        val savedText = viewModel.savedIPRotationIntervalText()
        if (savedText.isNotEmpty()) {
            textFieldState.setTextAndPlaceCursorAtEnd(savedText)
        }
    }

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

    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = {
            if (enabled) {
                expanded.value = it
            }
       },
    ) {
        OutlinedTextField(
            // The `menuAnchor` modifier must be passed to the text field to handle
            // expanding/collapsing the menu on click. A read-only text field has
            // the anchor type `PrimaryNotEditable`.
            modifier = modifier
                .width(150.dp)
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            state = textFieldState,
            readOnly = true,
            lineLimits = TextFieldLineLimits.SingleLine,
            label = { Text("IP Rotation Interval") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            colors = colors,
            shape = RoundedCornerShape(14.dp),
            enabled = enabled,
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = MaterialTheme.typography.bodyLarge) },
                    onClick = {
                        textFieldState.setTextAndPlaceCursorAtEnd(option)
                        expanded.value = false
                        coroutineScope.launch {
                            focusManager.clearFocus()
                            viewModel.setIPRotationInterval(option)
                        }
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                )
            }
        }
    }
}


@Composable
fun Header(navController: NavController, viewModel: HomeViewModel) {
    val connectionStatus by viewModel.connectionStatus.collectAsState()
    val painter: Painter = painterResource(id = R.drawable.header_bg)

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
                modifier = Modifier.padding(start = 20.dp, top = 35.dp, end = 20.dp, bottom = 20.dp).fillMaxWidth().align(Alignment.Center),
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
                            .clip(RoundedCornerShape(5.dp))
                            .background(color = Color(0xffF0F5F5))
                            .padding(4.dp)
                    ) {
                        if (connectionStatus == ConnectionStatus.Disconnected || connectionStatus == ConnectionStatus.Connecting) {
                            Image(
                                painter = painterResource(id = R.drawable.wifi_disconnected),
                                contentDescription = "Connection Status Disconnected",
                                modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                            )
                            Text("Disconnected",
                                color = Color(0x66232D42),
                                modifier = Modifier.padding(end = 5.dp)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.wifi_connected),
                                contentDescription = "Connection Status Connected",
                                modifier = Modifier.padding(start = 5.dp, end = 5.dp)
                            )
                            Text("Connected",
                                color = purple,
                                modifier = Modifier.padding(end = 5.dp)
                            )
                        }

                    }
                }

                SettingsIconButton(navController)
            }
        }
    }
}

@Composable
fun SettingsIconButton(navController: NavController) {

    IconButton(
        onClick = {
            navController.navigate(Screen.Settings.route)
        },
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
    val textColor = Color(0x66232D42)

    val annotatedString = buildAnnotatedString {
        pushStringAnnotation(tag = "link", annotation = "https://www.proxyrack.com/mobile-proxy/")
        withStyle(
            style = SpanStyle(
                color = textColor,
                textDecoration = TextDecoration.Underline,
                fontSize = 16.sp,
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
fun LogsButton(
    showBottomSheet: MutableState<Boolean>,
) {

    val grey = Color(0x33232D42)

    Column(
        modifier = Modifier.padding(top = 20.dp)
            .border(
                width = 1.dp,
                color = grey,
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .height(50.dp)
                .fillMaxWidth()
                .clickable {
                    //expanded.value = !expanded.value
                    showBottomSheet.value = true
                }
        ) {
            Text("Logs",
                color = Color(0x66232D42),
                modifier = Modifier.padding(start = 15.dp),
                )

            Icon(
                painter = painterResource(id = R.drawable.open_in_full_24px),
                contentDescription = "Open in full",
                tint = grey,
                modifier = Modifier.padding(end = 15.dp).size(20.dp)
            )

        }

    }

}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsBottomSheet(logMessages: List<String>, showBottomSheet: MutableState<Boolean>) {
    val sheetState = rememberModalBottomSheetState()

    // Create a LazyListState to control the scroll position
    val listState = rememberLazyListState()

    // Launch a coroutine to scroll to the bottom whenever items change
    LaunchedEffect(logMessages.size) {
        // Scroll to the last item
        if (logMessages.isNotEmpty()) {
            listState.animateScrollToItem(logMessages.size - 1)
        }
    }

    Scaffold(

    ) { contentPadding ->
        if (showBottomSheet.value) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet.value = false
                },
                sheetState = sheetState
            ) {

                val grey = Color(0x33232D42)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            val strokeWidth = 1.dp.toPx() // Border thickness
                            val y = size.height - strokeWidth / 2 // Position the line at the bottom
                            drawLine(
                                color = grey,
                                start = androidx.compose.ui.geometry.Offset(0f, y),
                                end = androidx.compose.ui.geometry.Offset(size.width, y),
                                strokeWidth = strokeWidth
                            )
                        }
                ) {
                    Text("Logs", modifier = Modifier.padding(start = 20.dp, bottom = 5.dp))
                }

                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                        modifier = Modifier.fillMaxWidth()

                    ) {
                        itemsIndexed(logMessages) { index, msg ->
                            Text(msg, modifier = Modifier.padding(
                                start = 20.dp,
                                end = 20.dp))
                        }
                    }

            }
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