package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class WebViewState(
    // Pour l'émulateur Android: 10.0.2.2 pointe vers localhost de la machine hôte
    // Pour un appareil physique: utiliser l'IP de votre machine (ex: 192.168.1.x:4200)
    val currentUrl: String = "http://10.0.2.2:4200",
    val fallbackUrl: String = "file:///android_asset/demo.html",
    val messages: List<String> = emptyList(),
    val lastAngularEvent: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val useLocalFallback: Boolean = false
)

sealed class WebViewEvent {
    data class MessageFromAngular(val message: String) : WebViewEvent()
    data class NavigationBlocked(val route: String, val reason: String) : WebViewEvent()
    data class OpenInBrowser(val url: String) : WebViewEvent()
    data object ClearError : WebViewEvent()
}

class WebViewViewModel : ViewModel() {

    private val _state = MutableStateFlow(WebViewState())
    val state: StateFlow<WebViewState> = _state.asStateFlow()

    private val eventHandlers: Map<String, (String) -> Unit> = mapOf(
        "button_click" to ::handleButtonClick,
        "form_submit" to ::handleFormSubmit,
        "user_action" to ::handleUserAction
    )

    fun onMessageFromAngular(message: String) {
        _state.update { current ->
            current.copy(
                messages = current.messages + "Angular → Android: $message",
                lastAngularEvent = message
            )
        }

        val eventType = message.substringBefore(":")
        eventHandlers[eventType]?.invoke(message)
    }

    fun onNavigationBlocked(route: String, reason: String) {
        _state.update { current ->
            current.copy(
                errorMessage = "Navigation bloquée: $reason",
                messages = current.messages + "🚫 Route bloquée: $route"
            )
        }
    }

    fun onLoadingChanged(isLoading: Boolean) {
        _state.update { it.copy(isLoading = isLoading) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun addMessage(message: String) {
        _state.update { current ->
            current.copy(messages = current.messages + message)
        }
    }

    fun toggleLocalFallback() {
        _state.update { current ->
            current.copy(useLocalFallback = !current.useLocalFallback)
        }
    }

    fun getActiveUrl(): String {
        val current = _state.value
        return if (current.useLocalFallback) current.fallbackUrl else current.currentUrl
    }

    private fun handleButtonClick(message: String) {
        addMessage("🔘 Bouton cliqué dans Angular")
    }

    private fun handleFormSubmit(message: String) {
        addMessage("📝 Formulaire soumis depuis Angular")
    }

    private fun handleUserAction(message: String) {
        addMessage("👤 Action utilisateur: ${message.substringAfter(":")}")
    }
}
