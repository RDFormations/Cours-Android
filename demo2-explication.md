# Demo 2 - Biométrie, Appels & Notifications

## Objectif

Cette démonstration montre comment utiliser les fonctionnalités natives Android :

1. **Authentification biométrique** (empreinte digitale, reconnaissance faciale)
2. **Appels téléphoniques** (appel direct et dialer)
3. **Notifications push locales** (canaux, styles, progression)

---

## Architecture

```
App3/
├── features/
│   ├── biometric/
│   │   ├── BiometricHelper.kt    ← Gestion authentification
│   │   └── BiometricScreen.kt    ← Interface utilisateur
│   ├── phone/
│   │   ├── PhoneCallHelper.kt    ← Gestion des appels
│   │   └── PhoneScreen.kt        ← Interface utilisateur
│   └── notification/
│       ├── NotificationHelper.kt ← Gestion des notifications
│       └── NotificationScreen.kt ← Interface utilisateur
├── ui/screens/
│   └── MainScreen.kt             ← Navigation par onglets
└── MainActivity.kt               ← AppCompatActivity (requis pour biométrie)
```

---

## 1. Authentification Biométrique

### Dépendance requise

```kotlin
// build.gradle.kts
implementation("androidx.biometric:biometric:1.1.0")
```

### Vérifier la disponibilité

```kotlin
val biometricManager = BiometricManager.from(context)

when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
    BiometricManager.BIOMETRIC_SUCCESS ->
        // Biométrie disponible et configurée
    BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
        // Pas de capteur biométrique
    BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
        // Capteur temporairement indisponible
    BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
        // Aucune empreinte/visage enregistré
}
```

### Lancer l'authentification

```kotlin
fun authenticate(
    activity: FragmentActivity,  // ⚠️ Doit être FragmentActivity
    onResult: (BiometricResult) -> Unit
) {
    val executor = ContextCompat.getMainExecutor(context)

    val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: AuthenticationResult) {
            onResult(BiometricResult.Success)
        }

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            when (errorCode) {
                BiometricPrompt.ERROR_USER_CANCELED,
                BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                    onResult(BiometricResult.Cancelled)
                else ->
                    onResult(BiometricResult.Error(errString.toString()))
            }
        }

        override fun onAuthenticationFailed() {
            // L'utilisateur peut réessayer, ne rien faire
        }
    }

    val biometricPrompt = BiometricPrompt(activity, executor, callback)

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle("Authentification requise")
        .setSubtitle("Utilisez votre empreinte digitale")
        .setNegativeButtonText("Annuler")  // Obligatoire si pas de device credential
        .build()

    biometricPrompt.authenticate(promptInfo)
}
```

### Pourquoi AppCompatActivity ?

`BiometricPrompt` nécessite une `FragmentActivity` pour gérer le cycle de vie du dialogue :

```kotlin
// ❌ Ne fonctionne pas
class MainActivity : ComponentActivity()

// ✅ Fonctionne
class MainActivity : AppCompatActivity()
```

Dans Compose, récupérer l'activity :

```kotlin
val context = LocalContext.current
val activity = context as? FragmentActivity

activity?.let {
    biometricHelper.authenticate(it) { result ->
        // Traiter le résultat
    }
}
```

### Sealed Class pour les résultats

```kotlin
sealed class BiometricResult {
    data object Success : BiometricResult()
    data object Cancelled : BiometricResult()
    data class Error(val message: String) : BiometricResult()
    data object NotAvailable : BiometricResult()
    data object NotEnrolled : BiometricResult()
}
```

> 💡 Les `sealed class` permettent un `when` exhaustif sans `else`.

---

## 2. Appels Téléphoniques

### Permissions requises

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.CALL_PHONE" />
```

### Deux méthodes d'appel

#### 1. Ouvrir le Dialer (sans permission)

```kotlin
fun openDialer(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_DIAL).apply {
        data = Uri.parse("tel:$phoneNumber")
    }
    context.startActivity(intent)
}
```

- ✅ Ne nécessite **aucune permission**
- ✅ L'utilisateur voit le numéro et décide d'appeler
- 📱 Ouvre l'app Téléphone avec le numéro pré-rempli

#### 2. Appel Direct (avec permission)

```kotlin
fun makeCall(phoneNumber: String) {
    val intent = Intent(Intent.ACTION_CALL).apply {  // ACTION_CALL, pas ACTION_DIAL
        data = Uri.parse("tel:$phoneNumber")
    }
    context.startActivity(intent)
}
```

- ⚠️ Nécessite `CALL_PHONE` permission
- ⚠️ Lance l'appel **immédiatement**
- 🔒 Permission dangereuse = demande runtime

### Demande de permission Runtime

```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        phoneHelper.makeCall(phoneNumber)
    }
}

// Utilisation
Button(onClick = {
    if (phoneHelper.hasCallPermission()) {
        phoneHelper.makeCall(number)
    } else {
        permissionLauncher.launch(Manifest.permission.CALL_PHONE)
    }
})
```

### Vérifier la permission

```kotlin
fun hasCallPermission(): Boolean {
    return ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.CALL_PHONE
    ) == PackageManager.PERMISSION_GRANTED
}
```

---

## 3. Notifications Push Locales

### Permission (Android 13+)

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

```kotlin
// Vérification
fun hasNotificationPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        true  // Pas besoin de permission avant Android 13
    }
}
```

### Créer les canaux de notification

Les **canaux** (Android 8+) permettent à l'utilisateur de gérer les notifications par catégorie :

```kotlin
private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val notificationManager = context.getSystemService(NotificationManager::class.java)

        val channels = listOf(
            NotificationChannel(
                "general_channel",
                "Notifications générales",
                NotificationManager.IMPORTANCE_DEFAULT
            ),
            NotificationChannel(
                "important_channel",
                "Notifications importantes",
                NotificationManager.IMPORTANCE_HIGH  // Son + vibration + heads-up
            ),
            NotificationChannel(
                "promo_channel",
                "Promotions",
                NotificationManager.IMPORTANCE_LOW   // Silencieux
            )
        )

        channels.forEach { notificationManager.createNotificationChannel(it) }
    }
}
```

### Niveaux d'importance

| Importance           | Comportement                                |
| -------------------- | ------------------------------------------- |
| `IMPORTANCE_HIGH`    | Son, vibration, heads-up display            |
| `IMPORTANCE_DEFAULT` | Son, vibration                              |
| `IMPORTANCE_LOW`     | Pas de son, pas de vibration                |
| `IMPORTANCE_MIN`     | Pas de son, pas de vibration, barre réduite |

### Envoyer une notification simple

```kotlin
fun sendNotification(title: String, message: String, channelId: String) {
    // Intent pour ouvrir l'app au clic
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_IMMUTABLE  // Requis Android 12+
    )

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_dialog_info)
        .setContentTitle(title)
        .setContentText(message)
        .setContentIntent(pendingIntent)
        .setAutoCancel(true)  // Disparaît au clic
        .build()

    NotificationManagerCompat.from(context).notify(
        getNextNotificationId(),  // ID unique
        notification
    )
}
```

### Notification avec texte long (BigTextStyle)

```kotlin
val notification = NotificationCompat.Builder(context, channelId)
    .setSmallIcon(android.R.drawable.ic_dialog_info)
    .setContentTitle(title)
    .setContentText(shortMessage)  // Aperçu
    .setStyle(
        NotificationCompat.BigTextStyle()
            .bigText(longMessage)  // Texte complet quand expandé
    )
    .build()
```

### Notification avec progression

```kotlin
fun sendProgressNotification(title: String, progress: Int): Int {
    val notificationId = getNextNotificationId()

    val notification = NotificationCompat.Builder(context, channelId)
        .setSmallIcon(android.R.drawable.ic_popup_sync)
        .setContentTitle(title)
        .setContentText("$progress%")
        .setProgress(100, progress, false)  // max, progress, indeterminate
        .setOngoing(progress < 100)         // Non-dismissable si en cours
        .build()

    NotificationManagerCompat.from(context).notify(notificationId, notification)
    return notificationId
}

// Mise à jour
fun updateProgress(notificationId: Int, progress: Int) {
    // Recréer la notification avec le même ID
    val notification = NotificationCompat.Builder(context, channelId)
        .setProgress(100, progress, false)
        .setOngoing(progress < 100)
        .build()

    NotificationManagerCompat.from(context).notify(notificationId, notification)
}
```

### Annuler des notifications

```kotlin
// Une notification spécifique
NotificationManagerCompat.from(context).cancel(notificationId)

// Toutes les notifications de l'app
NotificationManagerCompat.from(context).cancelAll()
```

---

## Permissions - Résumé

```xml
<!-- AndroidManifest.xml -->

<!-- Biométrie -->
<uses-permission android:name="android.permission.USE_BIOMETRIC" />

<!-- Appels téléphoniques -->
<uses-permission android:name="android.permission.CALL_PHONE" />

<!-- Notifications (Android 13+) -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
```

### Types de permissions

| Permission           | Type                 | Demande     |
| -------------------- | -------------------- | ----------- |
| `USE_BIOMETRIC`      | Normale              | Automatique |
| `CALL_PHONE`         | Dangereuse           | Runtime     |
| `POST_NOTIFICATIONS` | Dangereuse (API 33+) | Runtime     |

---

## Pattern de demande de permission

```kotlin
@Composable
fun FeatureScreen() {
    var hasPermission by remember { mutableStateOf(checkPermission()) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
    }

    Button(onClick = {
        if (hasPermission) {
            doAction()
        } else {
            launcher.launch(Manifest.permission.THE_PERMISSION)
        }
    }) {
        Text(if (hasPermission) "Action" else "Autoriser")
    }
}
```

---

## Configuration Theme (AppCompat)

Pour utiliser `AppCompatActivity`, le thème doit hériter d'AppCompat :

```xml
<!-- res/values/themes.xml -->
<style name="Theme.MyApplication" parent="Theme.AppCompat.Light.NoActionBar" />
```

---

## Bonnes pratiques

### Biométrie

- ✅ Toujours proposer une alternative (PIN, mot de passe)
- ✅ Expliquer pourquoi l'authentification est nécessaire
- ✅ Gérer le cas "aucune empreinte enregistrée"

### Appels

- ✅ Préférer `ACTION_DIAL` à `ACTION_CALL` quand possible
- ✅ Valider le format du numéro avant l'appel
- ✅ Confirmer avant un appel direct

### Notifications

- ✅ Utiliser des canaux appropriés
- ✅ Ne pas abuser des notifications `IMPORTANCE_HIGH`
- ✅ Permettre à l'utilisateur de désactiver certaines catégories
- ✅ Fournir un contenu utile et actionnable

---

## Test sur émulateur

### Biométrie

1. Paramètres émulateur → **Extended Controls** (...)
2. Onglet **Fingerprint**
3. Cliquer **Touch Sensor** quand le dialogue apparaît

### Notifications

- Fonctionnent normalement sur l'émulateur
- Les sons peuvent ne pas être audibles

### Appels

- Le dialer s'ouvre mais l'appel ne peut pas aboutir
- Utilisez un appareil physique pour tester réellement

---

## Résumé des flux

```
┌─────────────────────────────────────────────────────────────┐
│                    BIOMÉTRIE                                │
│                                                             │
│  BiometricManager.canAuthenticate()                         │
│           │                                                 │
│           ▼                                                 │
│  ┌──────────────────┐                                       │
│  │ BIOMETRIC_SUCCESS│ ───▶ BiometricPrompt.authenticate()   │
│  └──────────────────┘              │                        │
│                                    ▼                        │
│                    AuthenticationCallback                   │
│                    ├─ onAuthenticationSucceeded()           │
│                    ├─ onAuthenticationError()               │
│                    └─ onAuthenticationFailed()              │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    APPELS                                   │
│                                                             │
│  ┌──────────────┐        ┌──────────────┐                   │
│  │ ACTION_DIAL  │        │ ACTION_CALL  │                   │
│  │ (pas de perm)│        │ (CALL_PHONE) │                   │
│  └──────┬───────┘        └──────┬───────┘                   │
│         │                       │                           │
│         ▼                       ▼                           │
│    Ouvre Dialer           Appel direct                      │
│    (user décide)          (immédiat)                        │
└─────────────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────────────┐
│                    NOTIFICATIONS                            │
│                                                             │
│  NotificationChannel (API 26+)                              │
│         │                                                   │
│         ▼                                                   │
│  NotificationCompat.Builder                                 │
│         │                                                   │
│         ▼                                                   │
│  NotificationManagerCompat.notify(id, notification)         │
└─────────────────────────────────────────────────────────────┘
```

---

## Pour aller plus loin

- **Credential Manager** : Nouvelle API unifiée pour biométrie + passkeys
- **Firebase Cloud Messaging** : Notifications push depuis un serveur
- **WorkManager** : Notifications programmées même app fermée
- **CallScreeningService** : Filtrer les appels entrants
