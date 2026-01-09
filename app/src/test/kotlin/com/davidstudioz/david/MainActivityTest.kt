package com.davidstudioz.david

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testMainScreenDisplaysTitle() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        composeTestRule
            .onNodeWithText("DAVID AI")
            .assertExists()
    }
    
    @Test
    fun testVoiceButtonExists() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        composeTestRule
            .onNodeWithText("Voice Input")
            .assertExists()
    }
    
    @Test
    fun testVoiceButtonClickable() {
        composeTestRule.setContent {
            MainScreen()
        }
        
        composeTestRule
            .onNodeWithText("Voice Input")
            .performClick()
    }
}
