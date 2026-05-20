package com.numberletterpuzzle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.core.content.edit
import com.numberletterpuzzle.ui.MainMenuScreen
import com.numberletterpuzzle.ui.PuzzleEditorScreen
import com.numberletterpuzzle.ui.PuzzleScreen
import com.numberletterpuzzle.ui.PuzzleSelectionScreen
import com.numberletterpuzzle.ui.Screen
import com.numberletterpuzzle.ui.StatisticsScreen
import com.numberletterpuzzle.ui.theme.NumberLetterPuzzleGameTheme

class MainActivity : ComponentActivity() {

    private val themePrefs by lazy { getSharedPreferences("app_preferences", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isDarkMode by remember { mutableStateOf(themePrefs.getBoolean("dark_mode", false)) }

            NumberLetterPuzzleGameTheme(darkTheme = isDarkMode) {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.MainMenu) }

                when (val screen = currentScreen) {
                    Screen.MainMenu -> MainMenuScreen(
                        isDarkMode       = isDarkMode,
                        onDarkModeToggle = { enabled ->
                            isDarkMode = enabled
                            themePrefs.edit { putBoolean("dark_mode", enabled) }
                        },
                        onGame          = { currentScreen = Screen.PuzzleSelection },
                        onPuzzleEditor  = { currentScreen = Screen.PuzzleEditor },
                        onStatistics    = { currentScreen = Screen.Statistics },
                        onExit          = { finish() }
                    )
                    Screen.PuzzleSelection -> PuzzleSelectionScreen(
                        onPuzzleSelected = { puzzleDef -> currentScreen = Screen.Game(puzzleDef) },
                        onBack           = { currentScreen = Screen.MainMenu }
                    )
                    is Screen.Game -> PuzzleScreen(
                        puzzleDefinition = screen.puzzleDefinition,
                        onBack           = { currentScreen = Screen.PuzzleSelection }
                    )
                    Screen.PuzzleEditor -> PuzzleEditorScreen(
                        onBack = { currentScreen = Screen.MainMenu }
                    )
                    Screen.Statistics -> StatisticsScreen(
                        onBack = { currentScreen = Screen.MainMenu }
                    )
                }
            }
        }
    }
}
