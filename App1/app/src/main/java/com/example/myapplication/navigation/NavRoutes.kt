package com.example.myapplication.navigation

sealed class NavRoutes(val route: String) {
    object MainMenu : NavRoutes("main_menu")
    object TodoApp : NavRoutes("todo_app")
    object GameMenu : NavRoutes("game_menu")
    object TeamSelection : NavRoutes("team_selection")
    object Combat : NavRoutes("combat")
    object GameOver : NavRoutes("game_over")
    object Leaderboard : NavRoutes("leaderboard")
}
