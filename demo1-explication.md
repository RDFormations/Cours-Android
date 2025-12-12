# Demo 1 - WebView & Communication Angular ↔ Android

## Objectif

Cette démonstration montre comment :

1. **Embarquer un site web (Angular)** dans une application Android via WebView
2. **Protéger certaines routes** avec un système de garde
3. **Communiquer de manière bidirectionnelle** entre Angular et Android

---

## Architecture

```
App2/
├── webview/
│   ├── WebViewBridge.kt      ← Interface JavaScript exposée à Angular
│   ├── RouteGuard.kt         ← Logique de filtrage des URLs
│   └── WebViewScreen.kt      ← Composable principal avec WebView
├── viewmodel/
│   └── WebViewViewModel.kt   ← Gestion d'état et événements
└── MainActivity.kt

AngularApp/
├── services/
│   └── android-bridge.service.ts  ← Service de communication
├── guards/
│   └── route.guard.ts             ← Guard Angular des routes
└── components/
    └── home/                      ← Interface de démo
```

---

## Concepts Clés

### 1. WebView avec JavaScript

```kotlin
WebView(context).apply {
    settings.javaScriptEnabled = true      // Active JavaScript
    settings.domStorageEnabled = true       // Active localStorage
    settings.allowFileAccess = true         // Accès fichiers locaux
}
```

> ⚠️ `javaScriptEnabled = true` est nécessaire pour les apps Angular mais présente des risques de sécurité. Ne l'activez que pour des sources de confiance.

---

### 2. Bridge JavaScript (Android → Web)

Le **JavascriptInterface** permet d'exposer des méthodes Kotlin au JavaScript :

```kotlin
class WebViewBridge(
    private val onMessageReceived: (String) -> Unit
) {
    @JavascriptInterface  // Annotation obligatoire
    fun postMessage(message: String) {
        onMessageReceived(message)
    }
}

// Création de l'instance
val bridge = WebViewBridge(
    onMessageReceived = { message ->
        viewModel.onMessageFromAngular(message)
    }
)

// Enregistrement dans la WebView
webView.addJavascriptInterface(bridge, "AndroidBridge")
//                             ↑              ↑
//                        l'objet       le nom exposé au JS
```

**Côté Angular** (appel du bridge) :

```typescript
// Vérifie si le bridge existe
if (window.AndroidBridge) {
	window.AndroidBridge.postMessage("button_click:data");
}
```

---

### 3. Communication Web → Android

**Angular envoie un message :**

```typescript
sendToAndroid(action: string, value: string): void {
    const message = `${action}:${value}`;
    window.AndroidBridge.postMessage(message);
}
```

**Android reçoit via le callback :**

```kotlin
WebViewBridge(
    onMessageReceived = { message ->
        // message = "button_click:data"
        viewModel.onMessageFromAngular(message)
    }
)
```

---

### 4. Communication Android → Web

**Android envoie via `evaluateJavascript` :**

```kotlin
webView.evaluateJavascript(
    "window.receiveFromAndroid('update_theme:dark')",
    null  // Callback optionnel pour le résultat
)
```

**Angular reçoit via une fonction globale :**

```typescript
// Déclaration dans le service
window.receiveFromAndroid = (data: string) => {
	this.ngZone.run(() => {
		// Important: retour dans la zone Angular
		this.processMessage(data);
	});
};
```

> 💡 `ngZone.run()` est crucial car le callback vient de l'extérieur d'Angular et ne déclencherait pas la détection de changements automatiquement.

---

### 5. Route Guard (Filtrage des URLs)

Le `RouteGuard` intercepte les navigations et décide de leur sort :

```kotlin
sealed class NavigationResult {
    data object Allowed : NavigationResult()           // Autorisé
    data class Blocked(val reason: String)             // Bloqué
    data class ExternalBrowser(val url: String)        // Navigateur externe
}

fun checkNavigation(url: String): NavigationResult {
    val uri = Uri.parse(url)
    val host = uri.host ?: return NavigationResult.Allowed
    val path = uri.path ?: ""

    return when {
        blockedPaths.any { path.startsWith(it) } ->
            NavigationResult.Blocked("Route interdite")
        !allowedDomains.any { host.contains(it) } ->
            NavigationResult.ExternalBrowser(url)
        else ->
            NavigationResult.Allowed
    }
}
```

**Utilisation dans WebViewClient :**

```kotlin
webViewClient = object : WebViewClient() {
    override fun shouldOverrideUrlLoading(
        view: WebView?,
        request: WebResourceRequest?
    ): Boolean {
        val url = request?.url?.toString() ?: return false

        return when (val result = RouteGuard.checkNavigation(url)) {
            is NavigationResult.Allowed -> false  // Laisser WebView gérer
            is NavigationResult.Blocked -> {
                showError(result.reason)
                true  // Bloquer la navigation
            }
            is NavigationResult.ExternalBrowser -> {
                openInBrowser(result.url)
                true  // Bloquer dans WebView
            }
        }
    }
}
```

---

### 6. Pattern Jump Table pour les événements

Utilisation d'une **map de handlers** pour un code propre et extensible :

```kotlin
// Android - ViewModel
private val eventHandlers: Map<String, (String) -> Unit> = mapOf(
    "button_click" to ::handleButtonClick,
    "form_submit" to ::handleFormSubmit,
    "user_action" to ::handleUserAction
)

fun onMessageFromAngular(message: String) {
    val eventType = message.substringBefore(":")
    eventHandlers[eventType]?.invoke(message)
}
```

```typescript
// Angular - Service
private readonly messageHandlers: Record<string, (value: string) => void> = {
    'update_theme': (value) => this.handleThemeUpdate(value),
    'show_alert': (value) => this.handleShowAlert(value),
    'update_data': (value) => this.handleDataUpdate(value),
};

private processMessage(data: string): void {
    const [action, value] = data.split(':');
    this.messageHandlers[action]?.(value);
}
```

---

## Configuration Réseau

### Émulateur Android

L'adresse `10.0.2.2` est l'alias de `localhost` de la machine hôte :

```kotlin
val currentUrl: String = "http://10.0.2.2:4200"
```

### Appareil Physique

Utilisez l'IP de votre machine sur le réseau local :

```kotlin
val currentUrl: String = "http://192.168.1.XXX:4200"
```

### Angular - Écoute sur toutes les interfaces

```json
{
	"scripts": {
		"start": "ng serve --host 0.0.0.0"
	}
}
```

---

## Permissions Android

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:usesCleartextTraffic="true"  <!-- HTTP non sécurisé (dev) -->
    ...>
```

> ⚠️ `usesCleartextTraffic="true"` ne doit être utilisé qu'en développement. En production, utilisez HTTPS.

---

## Sécurité

### Bonnes pratiques

1. **Validez les messages** reçus du JavaScript
2. **Limitez les méthodes exposées** via `@JavascriptInterface`
3. **Utilisez HTTPS** en production
4. **Filtrez les domaines** autorisés dans la WebView
5. **Ne passez jamais de données sensibles** via le bridge sans chiffrement

### Exemple de validation

```kotlin
@JavascriptInterface
fun postMessage(message: String) {
    // Validation basique
    if (message.length > 1000) return
    if (!message.matches(Regex("^[a-zA-Z_]+:[^<>]*$"))) return

    onMessageReceived(message)
}
```

---

## Résumé des flux

```
┌─────────────────────────────────────────────────────────────┐
│                        ANGULAR                               │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  window.AndroidBridge.postMessage("event:data")     │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                  │
│                           ▼                                  │
└───────────────────────────┼──────────────────────────────────┘
                            │
                            │ @JavascriptInterface
                            │
┌───────────────────────────┼──────────────────────────────────┐
│                           ▼                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  WebViewBridge.postMessage(message)                 │    │
│  │       → onMessageReceived callback                  │    │
│  │       → ViewModel.onMessageFromAngular()            │    │
│  └─────────────────────────────────────────────────────┘    │
│                                                              │
│                        ANDROID                               │
│                                                              │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  webView.evaluateJavascript(                        │    │
│  │      "window.receiveFromAndroid('action:value')"    │    │
│  │  )                                                  │    │
│  └─────────────────────────────────────────────────────┘    │
│                           │                                  │
└───────────────────────────┼──────────────────────────────────┘
                            │
                            │ JavaScript execution
                            │
┌───────────────────────────┼──────────────────────────────────┐
│                           ▼                                  │
│  ┌─────────────────────────────────────────────────────┐    │
│  │  window.receiveFromAndroid = (data) => {            │    │
│  │      ngZone.run(() => processMessage(data))         │    │
│  │  }                                                  │    │
│  └─────────────────────────────────────────────────────┘    │
│                        ANGULAR                               │
└─────────────────────────────────────────────────────────────┘
```

---

## Pour aller plus loin

- **WebSocket** : Pour une communication temps réel bidirectionnelle
- **PostMessage API** : Alternative standard pour les iframes
- **Trusted Web Activity** : Pour publier une PWA comme app native
