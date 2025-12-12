package com.example.app4.features.security

import android.app.Activity
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

@Composable
fun SecurityScreen() {
    val context = LocalContext.current
    val activity = context as? Activity

    var isProtected by remember {
        mutableStateOf(activity?.let { SecurityHelper.isScreenCaptureProtected(it) } ?: false)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🛡️",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sécurité Écran",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (isProtected) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isProtected) "🔒" else "🔓",
                    fontSize = 48.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isProtected)
                        "Protection activée"
                    else
                        "Protection désactivée",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isProtected) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (isProtected)
                        "Les captures d'écran et l'enregistrement sont bloqués"
                    else
                        "Les captures d'écran sont autorisées",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Toggle Button
        Button(
            onClick = {
                activity?.let {
                    if (isProtected) {
                        SecurityHelper.disableScreenCaptureProtection(it)
                    } else {
                        SecurityHelper.enableScreenCaptureProtection(it)
                    }
                    isProtected = !isProtected
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isProtected)
                    Color(0xFFEF6C00)
                else
                    Color(0xFF2E7D32)
            )
        ) {
            Text(
                text = if (isProtected)
                    "🔓 Désactiver la protection"
                else
                    "🔒 Activer la protection",
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3F2FD)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "ℹ️ Comment tester ?",
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1565C0)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "1. Activez la protection ci-dessus",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "2. Essayez de faire une capture d'écran",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "3. La capture affichera un écran noir",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "📱 Sur émulateur : Power + Volume Down",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Sensitive data demo
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔐 Données sensibles (démo)",
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                SensitiveDataRow("Numéro de carte", "4532 •••• •••• 7891")
                SensitiveDataRow("CVV", "•••")
                SensitiveDataRow("Solde", "12 450,00 €")
                SensitiveDataRow("Code PIN", "••••")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = if (isProtected)
                "✅ Ces données sont protégées contre la capture"
            else
                "⚠️ Ces données peuvent être capturées !",
            style = MaterialTheme.typography.bodySmall,
            color = if (isProtected) Color(0xFF2E7D32) else Color(0xFFC62828),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SensitiveDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontWeight = FontWeight.Medium
        )
    }
}
