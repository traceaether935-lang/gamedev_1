# Project Plan

A complete native Android 2048-style number merging puzzle game.
Features:
- 4x4 and 5x5 board modes (generic board engine working with any supported board size)
- Touch swipe controls (UP, DOWN, LEFT, RIGHT)
- Core game rules: 90% chance of tile 2, 10% chance of tile 4. Merging rules, no double-merge in single move, move score addition, win at 2048 (with option to continue), game over check (no empty cells and no adjacent equal tiles).
- Working 1-step Undo system.
- Restart, Pause system (Resume, Restart, Home), lifecycle survival.
- Home screen (Game title, 4x4, 5x5, Statistics, Settings).
- Game screen (Back/Home, Scores, Board, Undo, Pause, Restart).
- Tile styling system for values 2 to 4096+.
- Animations (tile move, spawn, merge, button feedback, dialogs).
- Settings screen (Sound ON/OFF, Vibration/Haptics ON/OFF, persisted in DataStore).
- Statistics screen (games played, games won, highest score, highest tile, total moves for 4x4 and 5x5).
- DataStore persistence (restore unfinished game state, best scores, settings).
- Unit tests for GameEngine.
- Architecture: Clean separation (game engine, viewmodel, data repository, ui composables). Material 3, Jetpack Compose, Kotlin, Coroutines, DataStore.
- Target device: Portrait phone experience.

## Project Brief

# Project Brief: 2048 Puzzle Game (Android)

## Features

1. **Dynamic Grid Engine & Touch Swipe Mechanics**: Flexible engine supporting 4x4 and 5x5 board modes with touch swipe gestures (UP, DOWN, LEFT, RIGHT), tile generation rules (90% tile 2, 10% tile 4), accurate single-merge logic per move, 2048 win detection (with option to continue), and game-over verification.
2. **Interactive Play Screen & Controls**: Full-featured gameplay UI with score tracking, 1-step Undo mechanism, Pause/Resume/Restart overlay menus, smooth tile animations (spawn, move, merge), and dynamic styling for tile values ranging from 2 to 4096+.
3. **State & Preferences Persistence**: Robust background state management using Jetpack DataStore to persist unfinished board states across lifecycle events, high scores, total moves, and user settings (Sound and Haptic feedback toggles).
4. **Adaptive Screen Flow & Statistics**: Multi-screen navigation (Home, Game, Settings, Statistics) with dedicated statistics tracking for 4x4 and 5x5 modes (games played, win rate, peak score, highest tile, total moves).

## High-Level Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose, Material 3
- **Navigation**: Jetpack Navigation 3 (state-driven)
- **Layout & Adaptive Strategy**: Compose Material Adaptive library (optimized for portrait mobile layouts)
- **Architecture & Concurrency**: MVVM (GameEngine, ViewModel, Repository, Composables), Kotlin Coroutines & StateFlow
- **Persistence**: Jetpack Preferences DataStore
- **Testing**: JUnit 4 (Unit testing for GameEngine core logic)

## Implementation Steps

### Task_1_CoreGameEngine: Implement core 2048 game engine and logic supporting 4x4 and 5x5 grids, move/merge mechanics, random tile spawns (90% tile 2, 10% tile 4), score tracking, 1-step undo state, and win/game-over detection.
- **Status:** COMPLETED
- **Updates:** Core GameEngine, Board, GameState, MoveResult, Direction, GameStatus, Tile classes implemented in com.game.a2048. Fully tested with 17 unit tests in GameEngineTest covering all movement directions, merging cases, score calculation, 1-step undo, win, game over, restart, pause, and 4x4 / 5x5 grid sizes. All unit tests build and pass successfully.
- **Acceptance Criteria:**
  - Grid engine handles 4x4 and 5x5 dimensions correctly
  - Swipe move and single-merge logic computed accurately
  - 1-step undo restores previous board state and score
  - Win (2048 tile) and game-over states detected
  - Core unit tests pass

### Task_2_PersistenceAndRepository: Implement Jetpack DataStore persistence and repository layer for active game state (board state, score, move count), user settings (sound, haptics), and statistics (games played, win rate, peak score, highest tile per board size).
- **Status:** COMPLETED
- **Updates:** Implemented DataStore persistence and repository layer in com.game.a2048.data: UserSettings, GameStatistics, SavedGameState data models, SettingsRepository, StatisticsRepository, GameRepository using androidx.datastore preferences and kotlinx.serialization. JSON encoding used for saving active game states and undo states per board size (4x4 and 5x5). Added repository unit tests and verified all 25 unit tests pass.
- **Acceptance Criteria:**
  - DataStore persists and restores active game state across app restarts
  - High scores and statistics persisted separately for 4x4 and 5x5 modes
  - Settings preferences (sound, haptics) saved and loaded reliably

### Task_3_ComposeUIAndNavigation: Implement Jetpack Compose M3 UI, stateful ViewModels, and navigation for Home, Game (with touch swipe gesture detection, smooth tile rendering/animations, and Pause/Win/Game-Over overlays), Settings, and Statistics screens.
- **Status:** COMPLETED
- **Updates:** Implemented Jetpack Compose M3 UI, ViewModels, Gesture Detection, Tile Styling System, Overlays/Dialogs, and Navigation.
- Centralized tile styling in TileTheme.kt and TileView.kt supporting values 2 through 16384+.
- GameViewModel, SettingsViewModel, and StatisticsViewModel linking GameEngine, Repositories, and DataStore.
- Pointer drag swipe detector with 30dp threshold for UP, DOWN, LEFT, RIGHT gestures.
- GameBoard supporting dynamic 4x4 and 5x5 grid sizes.
- Overlays for Win (2048 reached with Continue option), Game Over, and Pause (Resume, Restart, Home).
- AppNavigation with Home, Game, Settings, and Statistics screens.
- MainActivity updated and built successfully with assembleDebug and 25 passing unit tests.
- **Acceptance Criteria:**
  - Navigation flows smoothly between Home, Game, Settings, and Statistics screens
  - Game screen handles swipe gestures, undo button, restart, and score dynamic UI
  - Tile colors and typography adapt properly to tile values
  - Settings and Statistics screens correctly bound to repository data
- **Duration:** N/A

### Task_4_RunAndVerify: Build, run, and verify the complete 2048 game application. Verify gameplay stability on 4x4 and 5x5 grids, state persistence, UI responsiveness, and absence of crashes. Critic agent verifies application stability, user requirements alignment, and UI issues.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - Verified 2048 gameplay, undo, persistence, settings, and statistics
- **StartTime:** 2026-09-13 17:35:07 IST

