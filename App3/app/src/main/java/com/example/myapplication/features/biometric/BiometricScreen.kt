package com.example.myapplication.features.biometric

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.*
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
import androidx.fragment.app.FragmentActivity

@Composable
fun BiometricScreen() {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricHelper = remember { BiometricHelper(context) }

    var authStatus by remember { mutableStateOf<String?>(null) }
    var isAuthenticated by remember { mutableStateOf(false) }
    var availability by remember { mutableStateOf<BiometricResult?>(null) }

    // Rafraîchir quand l'écran redevient visible
    LaunchedEffect(Unit) {
        availability = biometricHelper.checkBiometricAvailability()
    }

    val refreshAvailability: () -> Unit = {
        availability = biometricHelper.checkBiometricAvailability()
        if (availability is BiometricResult.Success) {
            authStatus = null
            isAuthenticated = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔐",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Authentification Biométrique",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status de disponibilité
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (availability) {
                    is BiometricResult.Success -> Color(0xFFE8F5E9)
                    is BiometricResult.NotAvailable -> Color(0xFFFFEBEE)
                    is BiometricResult.NotEnrolled -> Color(0xFFFFF3E0)
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Status du capteur",
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = when (availability) {
                        is BiometricResult.Success -> "✅ Biométrie disponible et configurée"
                        is BiometricResult.NotAvailable -> "❌ Capteur biométrique non disponible"
                        is BiometricResult.NotEnrolled -> "⚠️ Aucune empreinte enregistrée"
                        is BiometricResult.Error -> "❌ ${(availability as BiometricResult.Error).message}"
                        else -> "Vérification..."
                    },
                    color = when (availability) {
                        is BiometricResult.Success -> Color(0xFF2E7D32)
                        is BiometricResult.NotAvailable -> Color(0xFFC62828)
                        is BiometricResult.NotEnrolled -> Color(0xFFEF6C00)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )

                // Bouton pour ouvrir les paramètres si pas d'empreinte
                if (availability is BiometricResult.NotEnrolled) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Settings.ACTION_SECURITY_SETTINGS)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("⚙️ Ouvrir Paramètres Sécurité")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = refreshAvailability,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🔄 Rafraîchir le status")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton d'authentification
        Button(
            onClick = {
                activity?.let {
                    biometricHelper.authenticate(it) { result ->
                        when (result) {
                            is BiometricResult.Success -> {
                                isAuthenticated = true
                                authStatus = "✅ Authentification réussie !"
                            }
                            is BiometricResult.Cancelled -> {
                                authStatus = "⚠️ Authentification annulée"
                            }
                            is BiometricResult.Error -> {
                                authStatus = "❌ Erreur: ${result.message}"
                            }
                            else -> {}
                        }
                    }
                }
            },
            enabled = availability is BiometricResult.Success,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text(
                text = if (isAuthenticated) "Se reconnecter" else "S'authentifier",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Résultat de l'authentification
        authStatus?.let { status ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        status.contains("réussie") -> Color(0xFFE8F5E9)
                        status.contains("annulée") -> Color(0xFFFFF3E0)
                        else -> Color(0xFFFFEBEE)
                    }
                )
            ) {
                Text(
                    text = status,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Zone sécurisée (visible après auth)
        if (isAuthenticated) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFE3F2FD)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "🎉 Zone Sécurisée",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1565C0)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Vous avez accès aux données protégées",
                        textAlign = TextAlign.Center,
                        color = Color(0xFF1565C0)
                    )
                }
            }
        }
    }
}
