package com.example.app4.features.files

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class FileHelper(private val context: Context) {

    suspend fun downloadFile(
        url: String,
        fileName: String,
        onProgress: (Int) -> Unit
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection()
            connection.connect()
            val totalSize = connection.contentLength
            val inputStream = connection.getInputStream()

            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, getMimeType(fileName))
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)
                Uri.fromFile(file)
            }

            uri?.let { destinationUri ->
                context.contentResolver.openOutputStream(destinationUri)?.use { outputStream ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    var totalBytesRead = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        outputStream.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (totalSize > 0) {
                            val progress = ((totalBytesRead * 100) / totalSize).toInt()
                            onProgress(progress)
                        }
                    }
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val contentValues = ContentValues().apply {
                        put(MediaStore.Downloads.IS_PENDING, 0)
                    }
                    context.contentResolver.update(destinationUri, contentValues, null, null)
                }

                Result.success(destinationUri)
            } ?: Result.failure(Exception("Impossible de créer le fichier"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun saveTextFile(fileName: String, content: String): Result<Uri> {
        return try {
            val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, "text/plain")
                }
                context.contentResolver.insert(
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                    contentValues
                )
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS
                )
                val file = File(downloadsDir, fileName)
                Uri.fromFile(file)
            }

            uri?.let {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    outputStream.write(content.toByteArray())
                }
                Result.success(it)
            } ?: Result.failure(Exception("Impossible de créer le fichier"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun readTextFile(uri: Uri): Result<String> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().readText()
            }
            content?.let { Result.success(it) }
                ?: Result.failure(Exception("Impossible de lire le fichier"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getFileInfo(uri: Uri): FileInfo? {
        return try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                    val mimeIndex = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)

                    FileInfo(
                        name = if (nameIndex >= 0) cursor.getString(nameIndex) else "Unknown",
                        size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0,
                        mimeType = if (mimeIndex >= 0) cursor.getString(mimeIndex) else "Unknown"
                    )
                } else null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun getMimeType(fileName: String): String {
        return when {
            fileName.endsWith(".pdf") -> "application/pdf"
            fileName.endsWith(".txt") -> "text/plain"
            fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") -> "image/jpeg"
            fileName.endsWith(".png") -> "image/png"
            fileName.endsWith(".zip") -> "application/zip"
            else -> "application/octet-stream"
        }
    }
}

data class FileInfo(
    val name: String,
    val size: Long,
    val mimeType: String
) {
    val formattedSize: String
        get() = when {
            size < 1024 -> "$size B"
            size < 1024 * 1024 -> "${size / 1024} KB"
            else -> "${size / (1024 * 1024)} MB"
        }
}
