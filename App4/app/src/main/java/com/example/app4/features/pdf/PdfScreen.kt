package com.example.app4.features.pdf

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PdfScreen() {
    val context = LocalContext.current
    val pdfHelper = remember { PdfHelper(context) }

    var pdfTitle by remember { mutableStateOf("Mon Document PDF") }
    var pdfContent by remember { mutableStateOf("Ceci est un exemple de contenu pour le PDF.\n\nVous pouvez modifier ce texte et générer votre propre document PDF.\n\nLes PDFs sont sauvegardés dans le dossier Downloads.") }
    var createStatus by remember { mutableStateOf<String?>(null) }
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var currentPage by remember { mutableIntStateOf(0) }
    var totalPages by remember { mutableIntStateOf(0) }
    var scale by remember { mutableFloatStateOf(1f) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            selectedPdfUri = it
            totalPages = pdfHelper.getPdfPageCount(it)
            currentPage = 0
            pdfBitmap = pdfHelper.renderPdfPage(it, 0)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        if (selectedPdfUri == null) {
            // Mode création/sélection
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "📄",
                    fontSize = 64.sp,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Gestion PDF",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Créer un PDF
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📝 Créer un PDF",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = pdfTitle,
                            onValueChange = { pdfTitle = it },
                            label = { Text("Titre") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = pdfContent,
                            onValueChange = { pdfContent = it },
                            label = { Text("Contenu") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 5
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                val result = pdfHelper.createSamplePdf(pdfTitle, pdfContent)
                                createStatus = result.fold(
                                    onSuccess = { "✅ PDF créé et sauvegardé dans Downloads" },
                                    onFailure = { "❌ Erreur: ${it.message}" }
                                )
                            },
                            enabled = pdfTitle.isNotBlank() && pdfContent.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Générer le PDF")
                        }

                        createStatus?.let {
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

                // Ouvrir un PDF
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "📂 Ouvrir un PDF",
                            fontWeight = FontWeight.Medium,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                pdfPickerLauncher.launch(arrayOf("application/pdf"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Parcourir les PDFs")
                        }
                    }
                }
            }
        } else {
            // Mode visualisation PDF
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        selectedPdfUri = null
                        pdfBitmap = null
                    }) {
                        Text("← Retour")
                    }

                    Text(
                        text = "Page ${currentPage + 1} / $totalPages",
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // PDF Viewer
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    pdfBitmap?.let { bitmap ->
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "PDF Page",
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale
                                )
                                .pointerInput(Unit) {
                                    detectTransformGestures { _, _, zoom, _ ->
                                        scale = (scale * zoom).coerceIn(0.5f, 3f)
                                    }
                                }
                        )
                    } ?: CircularProgressIndicator()
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Navigation
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = {
                            if (currentPage > 0) {
                                currentPage--
                                selectedPdfUri?.let {
                                    pdfBitmap = pdfHelper.renderPdfPage(it, currentPage)
                                }
                            }
                        },
                        enabled = currentPage > 0
                    ) {
                        Text("◀ Précédent")
                    }

                    OutlinedButton(
                        onClick = { scale = 1f }
                    ) {
                        Text("Reset Zoom")
                    }

                    Button(
                        onClick = {
                            if (currentPage < totalPages - 1) {
                                currentPage++
                                selectedPdfUri?.let {
                                    pdfBitmap = pdfHelper.renderPdfPage(it, currentPage)
                                }
                            }
                        },
                        enabled = currentPage < totalPages - 1
                    ) {
                        Text("Suivant ▶")
                    }
                }
            }
        }
    }
}
