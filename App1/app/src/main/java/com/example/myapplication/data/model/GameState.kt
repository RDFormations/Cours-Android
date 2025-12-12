package com.example.myapplication.data.model

data class GameState(
    val team: List<Character> = emptyList(),
    val currentMonster: Monster? = null,
    val score: Int = 0,
    val monstersDefeated: Int = 0,
    val combatLog: List<String> = emptyList(),
    val gamePhase: GamePhase = GamePhase.MENU
)

enum class GamePhase {
    MENU,
    TEAM_SELECTION,
    COMBAT,
    GAME_OVER
}

data class ScoreEntry(
    val playerName: String,
    val score: Int,
    val monstersDefeated: Int
)
