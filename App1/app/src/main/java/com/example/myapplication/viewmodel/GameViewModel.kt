package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import com.example.myapplication.data.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _selectedCharacters = MutableStateFlow<Set<Int>>(emptySet())
    val selectedCharacters: StateFlow<Set<Int>> = _selectedCharacters.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<ScoreEntry>>(emptyList())
    val leaderboard: StateFlow<List<ScoreEntry>> = _leaderboard.asStateFlow()

    fun toggleCharacterSelection(characterId: Int) {
        _selectedCharacters.update { current ->
            if (characterId in current) {
                current - characterId
            } else if (current.size < 3) {
                current + characterId
            } else {
                current
            }
        }
    }

    fun startCombat() {
        val team = availableCharacters
            .filter { it.id in _selectedCharacters.value }
            .map { it.copy(currentHp = it.maxHp) }

        _gameState.update {
            it.copy(
                team = team,
                score = 0,
                monstersDefeated = 0,
                combatLog = listOf("⚔️ Le combat commence !"),
                gamePhase = GamePhase.COMBAT
            )
        }
        spawnNewMonster()
    }

    private fun spawnNewMonster() {
        val monster = monsterPool.random().copy()
        _gameState.update {
            it.copy(
                currentMonster = monster.copy(currentHp = monster.maxHp),
                combatLog = it.combatLog + "🐉 ${monster.name} apparaît !"
            )
        }
    }

    fun executeRound() {
        val state = _gameState.value
        val monster = state.currentMonster ?: return
        var updatedMonster = monster
        var updatedTeam = state.team
        val logs = mutableListOf<String>()

        // Team attacks monster
        updatedTeam.filter { it.isAlive }.forEach { character ->
            if (updatedMonster.isAlive) {
                val damage = character.attack
                updatedMonster = updatedMonster.takeDamage(damage)
                logs.add("⚔️ ${character.name} inflige $damage dégâts à ${monster.name}")
            }
        }

        // Check if monster is dead
        if (!updatedMonster.isAlive) {
            val newScore = state.score + monster.scoreValue
            logs.add("💀 ${monster.name} est vaincu ! +${monster.scoreValue} points")

            _gameState.update {
                it.copy(
                    currentMonster = null,
                    score = newScore,
                    monstersDefeated = it.monstersDefeated + 1,
                    combatLog = it.combatLog + logs
                )
            }
            spawnNewMonster()
            return
        }

        // Monster attacks random alive character
        val aliveCharacters = updatedTeam.filter { it.isAlive }
        if (aliveCharacters.isNotEmpty()) {
            val target = aliveCharacters.random()
            val damage = monster.attack
            updatedTeam = updatedTeam.map {
                if (it.id == target.id) it.takeDamage(damage) else it
            }
            logs.add("🔥 ${monster.name} attaque ${target.name} pour $damage dégâts")

            val newTarget = updatedTeam.find { it.id == target.id }
            if (newTarget != null && !newTarget.isAlive) {
                logs.add("☠️ ${target.name} est mort !")
            }
        }

        // Check game over
        val isGameOver = updatedTeam.none { it.isAlive }

        _gameState.update {
            it.copy(
                team = updatedTeam,
                currentMonster = updatedMonster,
                combatLog = it.combatLog + logs,
                gamePhase = if (isGameOver) GamePhase.GAME_OVER else GamePhase.COMBAT
            )
        }
    }

    fun saveScore(playerName: String) {
        val state = _gameState.value
        val entry = ScoreEntry(
            playerName = playerName.ifBlank { "Anonyme" },
            score = state.score,
            monstersDefeated = state.monstersDefeated
        )

        _leaderboard.update { current ->
            (current + entry)
                .sortedByDescending { it.score }
                .take(10)
        }
    }

    fun resetGame() {
        _selectedCharacters.value = emptySet()
        _gameState.update {
            GameState(gamePhase = GamePhase.TEAM_SELECTION)
        }
    }
}
