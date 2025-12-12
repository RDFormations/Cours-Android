package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapplication.ui.screens.MainMenuScreen
import com.example.myapplication.ui.screens.todo.TodoScreen
import com.example.myapplication.ui.screens.game.GameMenuScreen
import com.example.myapplication.ui.screens.game.TeamSelectionScreen
import com.example.myapplication.ui.screens.game.CombatScreen
import com.example.myapplication.ui.screens.game.GameOverScreen
import com.example.myapplication.ui.screens.game.LeaderboardScreen
import com.example.myapplication.viewmodel.TodoViewModel
import com.example.myapplication.viewmodel.GameViewModel

@Composable
fun AppNavigation(navController: NavHostController) {
    val todoViewModel: TodoViewModel = viewModel()
    val gameViewModel: GameViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = NavRoutes.MainMenu.route
    ) {
        composable(NavRoutes.MainMenu.route) {
            MainMenuScreen(
                onTodoClick = { navController.navigate(NavRoutes.TodoApp.route) },
                onGameClick = { navController.navigate(NavRoutes.GameMenu.route) }
            )
        }

        composable(NavRoutes.TodoApp.route) {
            TodoScreen(
                viewModel = todoViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.GameMenu.route) {
            GameMenuScreen(
                onStartGame = {
                    gameViewModel.resetGame()
                    navController.navigate(NavRoutes.TeamSelection.route)
                },
                onLeaderboard = { navController.navigate(NavRoutes.Leaderboard.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.TeamSelection.route) {
            TeamSelectionScreen(
                viewModel = gameViewModel,
                onStartCombat = { navController.navigate(NavRoutes.Combat.route) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.Combat.route) {
            CombatScreen(
                viewModel = gameViewModel,
                onGameOver = {
                    navController.navigate(NavRoutes.GameOver.route) {
                        popUpTo(NavRoutes.GameMenu.route)
                    }
                }
            )
        }

        composable(NavRoutes.GameOver.route) {
            GameOverScreen(
                viewModel = gameViewModel,
                onRestart = {
                    gameViewModel.resetGame()
                    navController.navigate(NavRoutes.TeamSelection.route) {
                        popUpTo(NavRoutes.GameMenu.route)
                    }
                },
                onMainMenu = {
                    navController.navigate(NavRoutes.GameMenu.route) {
                        popUpTo(NavRoutes.GameMenu.route) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.Leaderboard.route) {
            LeaderboardScreen(
                viewModel = gameViewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
