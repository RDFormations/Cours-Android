package com.example.app4.features.pdf

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import java.io.File
import java.io.FileOutputStream

class PdfHelper(private val context: Context) {

    fun createSamplePdf(title: String, content: String): Result<Uri> {
        return try {
            val pdfDocument = PdfDocument()

            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.parseColor("#673AB7")
                textSize = 24f
                isFakeBoldText = true
            }

            val contentPaint = Paint().apply {
                color = Color.BLACK
                textSize = 14f
            }

            val datePaint = Paint().apply {
                color = Color.GRAY
                textSize = 12f
            }

            // Header
            canvas.drawText(title, 50f, 80f, titlePaint)

            // Date
            val dateText = "Créé le: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}"
            canvas.drawText(dateText, 50f, 110f, datePaint)

            // Ligne séparatrice
            val linePaint = Paint().apply {
                color = Color.parseColor("#E0E0E0")
                strokeWidth = 2f
            }
            canvas.drawLine(50f, 130f, 545f, 130f, linePaint)

            // Contenu avec retour à la ligne
            val lines = content.split("\n")
            var yPosition = 170f
            for (line in lines) {
                // Wrap long lines
                val words = line.split(" ")
                var currentLine = ""
                for (word in words) {
                    val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
                    val textWidth = contentPaint.measureText(testLine)
                    if (textWidth > 495f) {
                        canvas.drawText(currentLine, 50f, yPosition, contentPaint)
                        yPosition += 20f
                        currentLine = word
                    } else {
                        currentLine = testLine
                    }
                }
                if (currentLine.isNotEmpty()) {
                    canvas.drawText(currentLine, 50f, yPosition, contentPaint)
                    yPosition += 25f
                }
            }

            // Footer
            val footerPaint = Paint().apply {
                color = Color.GRAY
                textSize = 10f
            }
            canvas.drawText("Généré par Demo App 4", 50f, 800f, footerPaint)
            canvas.drawText("Page 1", 500f, 800f, footerPaint)

            pdfDocument.finishPage(page)

            // Sauvegarder le PDF
            val fileName = "document_${System.currentTimeMillis()}.pdf"
            val uri = savePdfToDownloads(pdfDocument, fileName)

            pdfDocument.close()

            uri?.let { Result.success(it) }
                ?: Result.failure(Exception("Impossible de sauvegarder le PDF"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun savePdfToDownloads(pdfDocument: PdfDocument, fileName: String): Uri? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            }
            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                contentValues
            )
            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    pdfDocument.writeTo(outputStream)
                }
            }
            uri
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS
            )
            val file = File(downloadsDir, fileName)
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            Uri.fromFile(file)
        }
    }

    fun renderPdfPage(uri: Uri, pageIndex: Int = 0): Bitmap? {
        return try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            fileDescriptor?.let { fd ->
                val renderer = PdfRenderer(fd)
                if (pageIndex < renderer.pageCount) {
                    val page = renderer.openPage(pageIndex)
                    val bitmap = Bitmap.createBitmap(
                        page.width * 2,
                        page.height * 2,
                        Bitmap.Config.ARGB_8888
                    )
                    bitmap.eraseColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    page.close()
                    renderer.close()
                    bitmap
                } else {
                    renderer.close()
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }

    fun getPdfPageCount(uri: Uri): Int {
        return try {
            val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            fileDescriptor?.let { fd ->
                val renderer = PdfRenderer(fd)
                val count = renderer.pageCount
                renderer.close()
                count
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }
}
