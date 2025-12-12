package com.example.myapplication.features.phone

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PhoneScreen() {
    val context = LocalContext.current
    val phoneHelper = remember { PhoneCallHelper(context) }

    var phoneNumber by remember { mutableStateOf("") }
    var hasPermission by remember { mutableStateOf(phoneHelper.hasCallPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "📞",
            fontSize = 64.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Appels Téléphoniques",
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
                    text = if (hasPermission) "✅ Permission accordée" else "⚠️ Permission requise",
                    color = if (hasPermission) Color(0xFF2E7D32) else Color(0xFFEF6C00)
                )
                if (!hasPermission) {
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CALL_PHONE) }
                    ) {
                        Text("Autoriser")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Numéro manuel
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Numéro de téléphone") },
            placeholder = { Text("+33 6 12 34 56 78") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(
                onClick = { phoneHelper.openDialer(phoneNumber) },
                modifier = Modifier.weight(1f),
                enabled = phoneNumber.isNotBlank()
            ) {
                Text("Ouvrir Dialer")
            }

            Button(
                onClick = {
                    if (hasPermission) {
                        phoneHelper.makeCall(phoneNumber)
                    } else {
                        permissionLauncher.launch(Manifest.permission.CALL_PHONE)
                    }
                },
                modifier = Modifier.weight(1f),
                enabled = phoneNumber.isNotBlank()
            ) {
                Text("Appeler")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Contacts rapides",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PhoneCallHelper.PREDEFINED_CONTACTS) { contact ->
                ContactCard(
                    contact = contact,
                    hasPermission = hasPermission,
                    onCallClick = {
                        if (hasPermission) {
                            phoneHelper.makeCall(contact.number)
                        } else {
                            phoneHelper.openDialer(contact.number)
                        }
                    },
                    onDialerClick = { phoneHelper.openDialer(contact.number) }
                )
            }
        }
    }
}

@Composable
private fun ContactCard(
    contact: Contact,
    hasPermission: Boolean,
    onCallClick: () -> Unit,
    onDialerClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = contact.emoji,
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = contact.name,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = contact.number,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onDialerClick) {
                    Text("📱", fontSize = 20.sp)
                }
                if (hasPermission) {
                    IconButton(onClick = onCallClick) {
                        Text("📞", fontSize = 20.sp)
                    }
                }
            }
        }
    }
}
