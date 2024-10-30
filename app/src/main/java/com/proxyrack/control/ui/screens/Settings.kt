package com.proxyrack.control.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proxyrack.control.SettingsViewModel
import androidx.hilt.navigation.compose.hiltViewModel



@Composable
fun SettingsScreen(viewModel: SettingsViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Settings",
            fontSize = 26.sp,
        )
        //var checked by remember { mutableStateOf(true) }
        val checked by viewModel.analyticsEnabled.collectAsState()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier.fillMaxWidth().padding(start = 20.dp, top = 10.dp)
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = {
                    viewModel.setAnalyticsEnabled(it)
                }
            )
            Text(
                "Allow anonymous analytics"
            )
        }
    }
}