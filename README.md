# Number Letter Puzzle Game

A number-letter puzzle game for Android. Every cell in the grid contains a number that corresponds to a hidden letter — the same number always maps to the same letter throughout the entire puzzle. Your goal is to figure out all the mappings and decode the hidden words or message.

## How to play

1. Tap a numbered cell to select it
2. Press a letter on the on-screen keyboard to assign it to that number
3. The same letter will automatically fill all cells that share the same number
4. If you assign a letter that is already used elsewhere, it moves to the new number
5. Hit **Check** to verify your current solution
6. The puzzle is solved when every cell has the correct letter

Each puzzle starts with **3 pre-filled cells** revealed as a free starting hint.

## Features

### Gameplay
- **Alphabet tracker** — a scrollable row below the grid shows which letters have already been assigned, making it easy to spot gaps
- **Hint system** — up to 3 hints per puzzle; each hint reveals the correct letter for a randomly chosen unsolved number
- **Check button** — validates your current answer and counts mistakes; wrong assignments are tracked so retrying the same wrong guess does not inflate the mistake count
- **Reveal solution** — shows the full correct mapping when you are stuck (recorded separately, does not count as a win)
- **Reset** — restarts the puzzle with a freshly shuffled number-to-letter mapping
- **Timer** — counts elapsed time while you play; pauses automatically when you leave the screen

### Statistics
After completing a puzzle a result dialog shows your time, mistakes, and hints used. A dedicated **Statistics screen** tracks across all sessions:
- Number of puzzles completed
- Best and average solve times
- Total mistakes and hints used
- Number of times the solution was revealed
- Last played puzzle and completion timestamp

Stats can be cleared from the Statistics screen.

### Puzzle editor
A built-in **Puzzle Editor** lets you create and save your own puzzles:
- Build the grid cell by cell using the Hungarian keyboard
- Mark cells as blocked (black squares) to shape the layout
- Paste a plain-text board layout directly (letters for cells, `#` for blocked squares)
- Saved custom puzzles appear alongside the built-in puzzles in the puzzle selection screen

### Other
- **Dark mode** toggle on the main menu
- **Hungarian keyboard** with all accented characters: á, é, í, ó, ö, ő, ú, ü, ű
- Puzzle definitions are bundled as a `puzzles.json` asset, making it easy to add more puzzles without code changes

## Tech stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose, Material 3 |
| State management | ViewModel + StateFlow |
| Local storage | Room (SQLite) |
| Build system | Gradle with version catalog (`libs.versions.toml`) |
| Min SDK | 24 (Android 7.0) |
| Target SDK | 36 |

## Getting started

```bash
git clone https://github.com/<your-username>/NumberLetterPuzzleGame1.git
```

1. Open the project in **Android Studio Hedgehog** or newer
2. Let Gradle sync finish
3. Run on an emulator or a physical device running Android 7.0+

No API keys or external services are required — everything runs locally on device.

## Project structure

```
app/src/main/
├── assets/
│   └── puzzles.json              # Built-in puzzle definitions
└── java/com/numberletterpuzzle/
    ├── data/                     # Room entities, DAOs, repositories, data models
    ├── logic/                    # PuzzleGenerator, PuzzleValidator
    └── ui/
        ├── components/           # GridCell, PuzzleGrid, HungarianKeyboard,
        │                         #   AlphabetTracker, ResultDialog
        ├── theme/                # Color, typography, dark/light theme, PuzzleColors
        ├── MainMenuScreen.kt
        ├── PuzzleScreen.kt
        ├── PuzzleSelectionScreen.kt
        ├── PuzzleEditorScreen.kt
        ├── StatisticsScreen.kt
        └── PuzzleViewModel.kt
```

