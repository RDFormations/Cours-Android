package com.example.myapplication.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.viewmodel.WebViewViewModel
import com.example.myapplication.viewmodel.WebViewState

@Composable
fun WebViewScreen(
    modifier: Modifier = Modifier,
    viewModel: WebViewViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    var webView by remember { mutableStateOf<WebView?>(null) }
    val activeUrl = viewModel.getActiveUrl()

    // Recharger quand on change de mode
    LaunchedEffect(state.useLocalFallback) {
        webView?.loadUrl(activeUrl)
    }

    Column(modifier = modifier.fillMaxSize()) {
        // Header avec toggle
        WebViewHeader(
            state = state,
            onToggleSource = {
                viewModel.toggleLocalFallback()
                viewModel.addMessage("🔄 Source: ${if (!state.useLocalFallback) "Local" else "Angular Server"}")
            }
        )

        // WebView
        WebViewContent(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            url = activeUrl,
            onWebViewCreated = { webView = it },
            onMessageReceived = viewModel::onMessageFromAngular,
            onNavigationBlocked = viewModel::onNavigationBlocked,
            onLoadingChanged = viewModel::onLoadingChanged,
            onOpenInBrowser = { url ->
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
            }
        )

        // Boutons d'action Android → Angular
        ActionButtons(
            onSendToAngular = { event ->
                webView?.evaluateJavascript(
                    "window.receiveFromAndroid('$event')",
                    null
                )
                viewModel.addMessage("Android → Angular: $event")
            }
        )

        // Console de messages
        MessageConsole(
            messages = state.messages,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        )
    }

    // Afficher erreur si présente
    state.errorMessage?.let { error ->
        ErrorSnackbar(
            message = error,
            onDismiss = viewModel::clearError
        )
    }
}

@Composable
private fun WebViewHeader(
    state: WebViewState,
    onToggleSource: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Demo WebView Angular",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (state.useLocalFallback) "📄 Local HTML" else "🅰️ Angular Server",
                    style = MaterialTheme.typography.bodySmall
                )
                Switch(
                    checked = !state.useLocalFallback,
                    onCheckedChange = { onToggleSource() }
                )
            }
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun WebViewContent(
    modifier: Modifier = Modifier,
    url: String,
    onWebViewCreated: (WebView) -> Unit,
    onMessageReceived: (String) -> Unit,
    onNavigationBlocked: (String, String) -> Unit,
    onLoadingChanged: (Boolean) -> Unit,
    onOpenInBrowser: (String) -> Unit
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true

                // Ajouter le bridge JavaScript
                addJavascriptInterface(
                    WebViewBridge(
                        onMessageReceived = onMessageReceived,
                        onNavigationRequest = { route ->
                            when (val result = RouteGuard.checkNavigation(route)) {
                                is RouteGuard.NavigationResult.Allowed -> loadUrl(route)
                                is RouteGuard.NavigationResult.Blocked ->
                                    onNavigationBlocked(route, result.reason)
                                is RouteGuard.NavigationResult.ExternalBrowser ->
                                    onOpenInBrowser(result.url)
                            }
                        }
                    ),
                    WebViewBridge.BRIDGE_NAME
                )

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: WebResourceRequest?
                    ): Boolean {
                        val requestUrl = request?.url?.toString() ?: return false

                        return when (val result = RouteGuard.checkNavigation(requestUrl)) {
                            is RouteGuard.NavigationResult.Allowed -> false
                            is RouteGuard.NavigationResult.Blocked -> {
                                onNavigationBlocked(requestUrl, result.reason)
                                true
                            }
                            is RouteGuard.NavigationResult.ExternalBrowser -> {
                                onOpenInBrowser(result.url)
                                true
                            }
                        }
                    }

                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        onLoadingChanged(true)
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        onLoadingChanged(false)
                    }
                }

                loadUrl(url)
                onWebViewCreated(this)
            }
        }
    )
}

@Composable
private fun ActionButtons(
    onSendToAngular: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "Actions Android → Angular",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSendToAngular("update_theme:dark") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Theme Dark", fontSize = 12.sp)
                }
                Button(
                    onClick = { onSendToAngular("show_alert:Hello from Android!") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Alert", fontSize = 12.sp)
                }
                Button(
                    onClick = { onSendToAngular("update_data:${System.currentTimeMillis()}") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Update", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun MessageConsole(
    messages: List<String>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = modifier,
        color = Color(0xFF1E1E1E),
        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
    ) {
        Column {
            Text(
                text = "Console",
                color = Color.Gray,
                fontSize = 12.sp,
                modifier = Modifier.padding(8.dp)
            )
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 8.dp)
            ) {
                items(messages) { message ->
                    Text(
                        text = "> $message",
                        color = when {
                            message.contains("Angular →") -> Color(0xFF4CAF50)
                            message.contains("Android →") -> Color(0xFF2196F3)
                            message.contains("🚫") -> Color(0xFFF44336)
                            else -> Color.White
                        },
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorSnackbar(
    message: String,
    onDismiss: () -> Unit
) {
    Snackbar(
        modifier = Modifier.padding(16.dp),
        action = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    ) {
        Text(message)
    }
}
