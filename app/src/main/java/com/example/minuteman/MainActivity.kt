package com.example.minuteman

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.minuteman.model.ShellScreen
import com.example.minuteman.ui.screens.MainConsoleScreen
import com.example.minuteman.ui.theme.MinutemanTheme
import com.example.minuteman.viewmodel.ConsoleViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: ConsoleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MinutemanTheme {
                val activeCart by viewModel.activeCartridge.collectAsState()
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Intercept back button to return to Shell Menu or Cart browser
                BackHandler(enabled = activeCart != null || currentScreen != ShellScreen.MENU) {
                    if (activeCart != null) {
                        viewModel.exitCurrentCartridge()
                    } else if (currentScreen != ShellScreen.MENU) {
                        viewModel.soundEngine.menuBack()
                        // Return to menu
                        when (currentScreen) {
                            ShellScreen.CARTS, ShellScreen.HOWTO, ShellScreen.MEMORY,
                            ShellScreen.SETTINGS, ShellScreen.DEBUG, ShellScreen.CREDITS -> {
                                // Handled in ViewModel
                                viewModel.onButtonChange(com.example.minuteman.model.GamepadButtonKey.B, true)
                                window.decorView.postDelayed({
                                    viewModel.onButtonChange(com.example.minuteman.model.GamepadButtonKey.B, false)
                                }, 50)
                            }
                            else -> finish()
                        }
                    }
                }

                MainConsoleScreen(viewModel = viewModel)
            }
        }
    }
}
