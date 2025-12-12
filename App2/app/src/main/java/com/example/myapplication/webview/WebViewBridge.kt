package com.example.myapplication.webview

import android.webkit.JavascriptInterface

/**
 * Bridge JavaScript pour la communication Angular ↔ Android
 * Expose des méthodes appelables depuis JavaScript
 */
class WebViewBridge(
    private val onMessageReceived: (String) -> Unit,
    private val onNavigationRequest: (String) -> Unit
) {

    @JavascriptInterface
    fun postMessage(message: String) {
        onMessageReceived(message)
    }

    @JavascriptInterface
    fun requestNavigation(route: String) {
        onNavigationRequest(route)
    }

    @JavascriptInterface
    fun log(message: String) {
        println("WebView Log: $message")
    }

    companion object {
        const val BRIDGE_NAME = "AndroidBridge"
    }
}
