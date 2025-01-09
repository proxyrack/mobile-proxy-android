package com.proxyrack.control.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.proxyrack.control.BuildConfig
import com.proxyrack.control.R


@Composable
fun SettingsScreen(navController: NavController, viewModel: SettingsViewModel = hiltViewModel()) {
    val windowInsetPadding = WindowInsets.statusBars.asPaddingValues()
    Column(modifier = Modifier.fillMaxSize().padding(start = 20.dp, top = windowInsetPadding.calculateTopPadding(), end = 20.dp, bottom = windowInsetPadding.calculateBottomPadding())) {
        Column(
            // 'weight' allows Row with version number to actually show on the screen
            // https://stackoverflow.com/a/71668596/6716264
            // The children without weight are measured first. After that, the remaining space in the column is spread among the children with weights, proportional to their weight.
            modifier = Modifier.fillMaxSize().weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box() {
                BackIconButton(navController)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Settings",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                    )
                }
            }


            //var checked by remember { mutableStateOf(true) }
            val checked by viewModel.analyticsEnabled.collectAsState()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(top = 30.dp)
            ) {
                Column {
                    Text(
                        "Allow anonymous analytics"
                    )
                    Text(
                        "No identifying data is collected.",
                        color = Color(0x6600091F),
                        fontSize = 14.sp,
                    )
                }
                Switch(
                    checked = checked,
                    onCheckedChange = {
                        viewModel.setAnalyticsEnabled(it)
                    },
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Color(0xff4A28C6)
                    )
                )
            }
        }
        Row(

        ) {
            Text("v" + BuildConfig.VERSION_NAME)
        }
    }

}

@Composable
fun BackIconButton(navController: NavController) {
    IconButton(
        onClick = {
            navController.navigateUp()
        },
        modifier = Modifier
            .background(color = Color(0x232D421A), shape = RoundedCornerShape(8.dp))
            .size(36.dp)
    ) {
        Icon(
            painter = rememberVectorPainter(
                image = ImageVector.vectorResource(id = R.drawable.arrow_left)
            ),
            contentDescription = "Settings",
            modifier = Modifier.size(22.dp)
        )
    }
}