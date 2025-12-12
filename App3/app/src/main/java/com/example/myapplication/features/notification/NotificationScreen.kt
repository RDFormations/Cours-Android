package com.example.myapplication.features.notification

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun NotificationScreen() {
    val context = LocalContext.current
    val notificationHelper = remember { NotificationHelper(context) }
    val scope = rememberCoroutineScope()

    var hasPermission by remember { mutableStateOf(notificationHelper.hasNotificationPermission()) }
    var progressNotificationId by remember { mutableStateOf<Int?>(null) }
    var currentProgress by remember { mutableIntStateOf(0) }
    var isProgressRunning by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "🔔",
            fontSize = 64.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Notifications Push",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Permission status
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (hasPermission) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (hasPermission) "✅ Notifications activées" else "⚠️ Permission requise",
                    color = if (hasPermission) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                )
                if (!hasPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    ) {
                        Text("Autoriser")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Notifications simples
        Text(
            text = "Types de notifications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        NotificationTypeCard(
            emoji = "📬",
            title = "Notification simple",
            description = "Notification standard",
            buttonText = "Envoyer",
            onClick = {
                notificationHelper.sendNotification(
                    title = "Nouvelle notification",
                    message = "Ceci est une notification de test"
                )
            },
            enabled = hasPermission
        )

        Spacer(modifier = Modifier.height(8.dp))

        NotificationTypeCard(
            emoji = "🚨",
            title = "Notification importante",
            description = "Haute priorité avec son",
            buttonText = "Envoyer",
            onClick = {
                notificationHelper.sendNotification(
                    title = "⚠️ Alerte importante",
                    message = "Action requise immédiatement !",
                    channelId = NotificationHelper.CHANNEL_ID_IMPORTANT
                )
            },
            enabled = hasPermission
        )

        Spacer(modifier = Modifier.height(8.dp))

        NotificationTypeCard(
            emoji = "🎁",
            title = "Promotion",
            description = "Basse priorité, silencieuse",
            buttonText = "Envoyer",
            onClick = {
                notificationHelper.sendNotification(
                    title = "Offre spéciale !",
                    message = "-50% sur tout le site",
                    channelId = NotificationHelper.CHANNEL_ID_PROMO
                )
            },
            enabled = hasPermission
        )

        Spacer(modifier = Modifier.height(8.dp))

        NotificationTypeCard(
            emoji = "📝",
            title = "Notification longue",
            description = "Texte expansible",
            buttonText = "Envoyer",
            onClick = {
                notificationHelper.sendBigTextNotification(
                    title = "Article de blog",
                    shortMessage = "Nouveau post disponible...",
                    longMessage = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris."
                )
            },
            enabled = hasPermission
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Notification avec progression
        Text(
            text = "Notification avec progression",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📊 Téléchargement simulé",
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$currentProgress%",
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { currentProgress / 100f },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        if (!isProgressRunning) {
                            isProgressRunning = true
                            currentProgress = 0
                            scope.launch {
                                val notifId = notificationHelper.sendProgressNotification(
                                    title = "Téléchargement en cours",
                                    progress = 0
                                )
                                progressNotificationId = notifId

                                while (currentProgress < 100) {
                                    delay(100)
                                    currentProgress += 2
                                    notificationHelper.updateProgressNotification(
                                        notificationId = notifId,
                                        title = "Téléchargement en cours",
                                        progress = currentProgress
                                    )
                                }
                                isProgressRunning = false
                            }
                        }
                    },
                    enabled = hasPermission && !isProgressRunning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isProgressRunning) "En cours..." else "Démarrer")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Actions
        OutlinedButton(
            onClick = { notificationHelper.cancelAllNotifications() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("🗑️ Effacer toutes les notifications")
        }
    }
}

@Composable
private fun NotificationTypeCard(
    emoji: String,
    title: String,
    description: String,
    buttonText: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, fontWeight = FontWeight.Medium)
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Button(
                onClick = onClick,
                enabled = enabled
            ) {
                Text(buttonText)
            }
        }
    }
}
