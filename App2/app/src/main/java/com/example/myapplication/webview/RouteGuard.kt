package com.example.myapplication.webview

/**
 * Configuration des routes autorisées/interdites
 * Les routes interdites s'ouvrent dans le navigateur externe
 */
object RouteGuard {

    private val allowedDomains = setOf(
        "angular.io",
        "localhost",
        "127.0.0.1",
        "10.0.2.2" // Android emulator localhost
    )

    private val blockedPaths = setOf(
        "/admin",
        "/settings",
        "/private",
        "/external"
    )

    sealed class NavigationResult {
        data object Allowed : NavigationResult()
        data class Blocked(val reason: String) : NavigationResult()
        data class ExternalBrowser(val url: String) : NavigationResult()
    }

    fun checkNavigation(url: String): NavigationResult {
        val uri = android.net.Uri.parse(url)
        val host = uri.host ?: return NavigationResult.Allowed
        val path = uri.path ?: ""

        // Vérifier si le domaine est autorisé
        val isDomainAllowed = allowedDomains.any { host.contains(it) }

        // Vérifier si le path est bloqué
        val isPathBlocked = blockedPaths.any { path.startsWith(it) }

        return when {
            isPathBlocked -> NavigationResult.Blocked("Route interdite: $path")
            !isDomainAllowed && !url.startsWith("file://") -> NavigationResult.ExternalBrowser(url)
            else -> NavigationResult.Allowed
        }
    }
}
