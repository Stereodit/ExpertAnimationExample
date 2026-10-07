package com.expertanimationexample

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.*
import org.junit.Rule
import org.junit.Test

class MotionLabTest {
    @get:Rule val compose = createComposeRule()

    @Test fun allExperimentsCanBeOpenedAndResetWhilePaused() {
        // Continuous simulations deliberately never become idle. Verify gallery
        // navigation with simulations paused; device motion needs manual review.
        compose.setContent { MaterialTheme { MotionLab(initialPlaying = false) } }
        val names = listOf("Elastic stack", "Spatial carousel", "Shape laboratory", "Orbital field", "Radial bloom")
        names.forEachIndexed { index, name ->
            compose.onNodeWithText("0${index + 1}  $name").performScrollTo().performClick()
            compose.onNodeWithText(name).assertExists()
            compose.onNodeWithText("Продолжить").performScrollTo().assertExists()
            compose.onNodeWithText("Сброс").performClick()
            compose.onNodeWithText(name).assertExists()
        }
    }
}
