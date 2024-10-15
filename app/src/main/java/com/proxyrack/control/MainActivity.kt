package com.proxyrack.control

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import com.proxyrack.control.domain.ConnectionStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
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
                            modifier = Modifier.background(
                                color = Color.White
                            )
                        )
                        TitledColumn("Settings") {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                // Server IP Field
                                StyledTextField(
                                    "Server IP",
                                    value = viewModel.serverIP,
                                    onValueChange = {
                                        viewModel.updateServerIP(it)
                                    },
                                    onDone = {
                                        viewModel.saveServerIP()
                                    },
                                    modifier = Modifier.weight(1f).padding(end = 4.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    )
                                // Device IP Display Field
                                StyledTextField(
                                    "Your Device IP",
                                    value = viewModel.deviceIP,
                                    onValueChange = {
                                        viewModel.updateDeviceIP(it)
                                    },
                                    enabled = false,
                                    modifier = Modifier.weight(1f).padding(start = 4.dp))
                            }
                            StyledTextField(
                                "Your Device ID",
                                value = viewModel.deviceID,
                                onValueChange = {
                                    viewModel.updateDeviceID(it)
                                },
                                onDone = {
                                    viewModel.saveDeviceID()
                                },
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp).fillMaxWidth())
                            SetupInstructionsLink(modifier = Modifier.align(Alignment.CenterHorizontally).padding(top = 30.dp, bottom = 30.dp))
                        }
                        val connectionStatus by viewModel.connectionStatus.collectAsState()

                        val buttonColor = when (connectionStatus) {
                            ConnectionStatus.Connecting -> colorFromHex("#49de7d")
                            ConnectionStatus.Connected -> colorFromHex("#f5524c")
                            ConnectionStatus.Disconnected -> colorFromHex("#49de7d")
                        }

                        Button(
                            onClick = {
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
            }
        }
    }
}

// https://stackoverflow.com/a/69549929/6716264
@Composable
fun SetupInstructionsLink(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val annotatedString = buildAnnotatedString {
        pushStringAnnotation(tag = "link", annotation = "https://google.com")
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
                .background(Color.White)
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
                .background(Color.White)
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
    var colors = OutlinedTextFieldDefaults.colors()
    if (!enabled) {
        colors = OutlinedTextFieldDefaults.colors(
            disabledTextColor = colors.unfocusedTextColor,
            disabledBorderColor = colors.unfocusedIndicatorColor,
            disabledLabelColor = colors.unfocusedLabelColor,
        )
    }

    val keyboardOptions = keyboardOptions.copy(imeAction = ImeAction.Done)

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val textValue by value.collectAsState()

    OutlinedTextField(
        modifier = modifier.focusRequester(focusRequester),
        onValueChange = onValueChange,
        value = textValue,
        label = { Text(label) },
        colors = colors,
        enabled = enabled,
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