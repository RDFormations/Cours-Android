package com.example.app4.features.files

import android.net.Uri
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
import kotlinx.coroutines.launch

@Composable
fun FileScreen() {
    val context = LocalContext.current
    val fileHelper = remember { FileHelper(context) }
    val scope = rememberCoroutineScope()

    var downloadProgress by remember { mutableIntStateOf(0) }
    var isDownloading by remember { mutableStateOf(false) }
    var downloadStatus by remember { mutableStateOf<String?>(null) }
    var selectedFileInfo by remember { mutableStateOf<FileInfo?>(null) }
    var fileContent by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedFileInfo = fileHelper.getFileInfo(it)
            if (selectedFileInfo?.mimeType == "text/plain") {
                fileHelper.readTextFile(it).onSuccess { content ->
                    fileContent = content
                }
            } else {
                fileContent = null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text(
            text = "📁",
            fontSize = 64.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Gestion des Fichiers",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Section Téléchargement
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📥 Téléchargement",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (isDownloading) {
                    LinearProgressIndicator(
                        progress = { downloadProgress / 100f },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Téléchargement: $downloadProgress%",
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isDownloading = true
                            downloadProgress = 0
                            downloadStatus = null

                            // URL de test (fichier texte)
                            val result = fileHelper.downloadFile(
                                url = "https://www.w3.org/TR/PNG/iso_8859-1.txt",
                                fileName = "test_download.txt"
                            ) { progress ->
                                downloadProgress = progress
                            }

                            isDownloading = false
                            downloadStatus = result.fold(
                                onSuccess = { "✅ Fichier téléchargé dans Downloads" },
                                onFailure = { "❌ Erreur: ${it.message}" }
                            )
                        }
                    },
                    enabled = !isDownloading,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isDownloading) "Téléchargement..." else "Télécharger fichier test")
                }

                downloadStatus?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = if (it.contains("✅")) Color(0xFF2E7D32) else Color(0xFFC62828),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Création fichier
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📝 Créer un fichier",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                var textToSave by remember { mutableStateOf("") }
                var saveStatus by remember { mutableStateOf<String?>(null) }

                OutlinedTextField(
                    value = textToSave,
                    onValueChange = { textToSave = it },
                    label = { Text("Contenu du fichier") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val result = fileHelper.saveTextFile(
                            fileName = "mon_fichier_${System.currentTimeMillis()}.txt",
                            content = textToSave
                        )
                        saveStatus = result.fold(
                            onSuccess = { "✅ Fichier sauvegardé dans Downloads" },
                            onFailure = { "❌ Erreur: ${it.message}" }
                        )
                    },
                    enabled = textToSave.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Sauvegarder")
                }

                saveStatus?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = it,
                        color = if (it.contains("✅")) Color(0xFF2E7D32) else Color(0xFFC62828),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Section Ouvrir fichier
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📂 Ouvrir un fichier",
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        filePickerLauncher.launch(arrayOf("*/*"))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Parcourir les fichiers")
                }

                selectedFileInfo?.let { info ->
                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD)
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📄 ${info.name}",
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Taille: ${info.formattedSize}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Type: ${info.mimeType}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    fileContent?.let { content ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Contenu:",
                            fontWeight = FontWeight.Medium
                        )
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFF5F5F5)
                            )
                        ) {
                            Text(
                                text = content.take(500) + if (content.length > 500) "..." else "",
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
