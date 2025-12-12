package com.example.myapplication.ui.screens.game

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.model.Character
import com.example.myapplication.data.model.GamePhase
import com.example.myapplication.data.model.Monster
import com.example.myapplication.viewmodel.GameViewModel

@Composable
fun CombatScreen(
    viewModel: GameViewModel,
    onGameOver: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(gameState.gamePhase) {
        if (gameState.gamePhase == GamePhase.GAME_OVER) {
            onGameOver()
        }
    }

    LaunchedEffect(gameState.combatLog.size) {
        if (gameState.combatLog.isNotEmpty()) {
            listState.animateScrollToItem(gameState.combatLog.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Score display
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("🏆 Score: ${gameState.score}", fontWeight = FontWeight.Bold)
                Text("💀 Monstres: ${gameState.monstersDefeated}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Monster display
        gameState.currentMonster?.let { monster ->
            MonsterCard(monster)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Team display
        Text("👥 Votre équipe :", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            gameState.team.forEach { character ->
                TeamMemberCard(
                    character = character,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Combat log
        Text("📜 Journal de combat :", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(gameState.combatLog) { log ->
                    Text(text = log, fontSize = 14.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Attack button
        Button(
            onClick = { viewModel.executeRound() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = gameState.gamePhase == GamePhase.COMBAT
        ) {
            Text("⚔️ ATTAQUER !", fontSize = 20.sp)
        }
    }
}

@Composable
fun MonsterCard(monster: Monster) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🐉 ${monster.name}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { monster.currentHp.toFloat() / monster.maxHp },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp),
                color = Color.Red,
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text("❤️ ${monster.currentHp}/${monster.maxHp} PV")

            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("⚔️ ${monster.attack} ATK")
                Text("🛡️ ${monster.defense} DEF")
            }
        }
    }
}

@Composable
fun TeamMemberCard(character: Character, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = if (character.isAlive)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (character.isAlive) character.name else "☠️",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )

            if (character.isAlive) {
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { character.currentHp.toFloat() / character.maxHp },
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Green,
                )
                Text(
                    text = "${character.currentHp}/${character.maxHp}",
                    fontSize = 10.sp
                )
            }
        }
    }
}
