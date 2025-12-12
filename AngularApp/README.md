# Angular Bridge Demo

Application Angular de démonstration pour la communication bidirectionnelle avec Android WebView.

## Installation

```bash
cd AngularApp
npm install
```

## Démarrage

```bash
npm start
```

L'application sera disponible sur `http://localhost:4200`

## Connexion avec Android

### Émulateur Android

L'URL est automatiquement configurée pour `http://10.0.2.2:4200` (équivalent localhost pour l'émulateur).

### Appareil physique

Modifier l'URL dans `WebViewViewModel.kt` avec l'IP de votre machine :

```kotlin
val currentUrl: String = "http://192.168.1.XXX:4200"
```

## Fonctionnalités

### Communication Angular → Android

```typescript
// Envoyer un message à Android
this.bridge.sendToAndroid("button_click", "data");
```

### Communication Android → Angular

```kotlin
// Depuis Android
webView.evaluateJavascript("window.receiveFromAndroid('update_theme:dark')", null)
```

### Route Guards

Les routes `/admin` et `/settings` sont bloquées par le guard et notifient Android.

## Structure

```
src/app/
├── services/
│   └── android-bridge.service.ts  # Service de communication
├── guards/
│   └── route.guard.ts             # Guard des routes bloquées
├── components/
│   ├── home/                      # Page principale
│   ├── admin/                     # Page bloquée
│   └── settings/                  # Page bloquée
└── app.routes.ts                  # Configuration des routes
```
