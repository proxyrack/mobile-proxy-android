package com.proxyrack.control.ui.screens

import androidx.activity.compose.setContent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.proxyrack.control.MainActivity
import com.proxyrack.control.core.util.TestTags
import com.proxyrack.control.ui.navigation.Screen
import com.proxyrack.control.ui.theme.ProxyControlTheme
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class HomeScreenTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun setUp() {
        hiltRule.inject()
        composeRule.activity.setContent {
            val navController = rememberNavController()
            ProxyControlTheme {
                NavHost(
                    navController = navController,
                    startDestination = Screen.Home.route,
                ) {
                    composable(route = Screen.Home.route) {
                        HomeScreen(navController = navController)
                    }
                }
            }
        }
    }

    @Test
    fun deviceID_isVisible() {
        // val context = ApplicationProvider.getApplicationContext<Context>()
        composeRule.onNodeWithTag(TestTags.DEVICE_ID).assertExists()
        composeRule.onNodeWithTag(TestTags.DEVICE_ID).assertIsDisplayed()
    }
}