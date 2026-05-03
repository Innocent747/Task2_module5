package com.example.task2.data

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PhotoRepository(private val context: Context) {

    private val picturesDir: File? = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US)

    suspend fun loadAllPhotos(): List<Photo> = withContext(Dispatchers.IO) {
        if (picturesDir == null || !picturesDir.exists()) {
            return@withContext emptyList()
        }

        picturesDir.listFiles()?.mapNotNull { file ->
            if (file.isFile && file.extension.lowercase() in listOf("jpg", "jpeg", "png")) {
                try {
                    val timestamp = file.lastModified()
                    Photo(
                        id = file.name,
                        filename = file.name,
                        file = file,
                        timestamp = timestamp
                    )
                } catch (e: Exception) {
                    Log.e("PhotoRepository", "Error loading photo ${file.name}", e)
                    null
                }
            } else {
                null
            }
        }?.sortedByDescending { it.timestamp } ?: emptyList()
    }

    fun getNewPhotoFile(): File? {
        if (picturesDir == null) {
            picturesDir?.mkdirs()
            return null
        }

        if (!picturesDir.exists()) {
            picturesDir.mkdirs()
        }

        val timeStamp = dateFormat.format(Date())
        val imageFileName = "IMG_$timeStamp.jpg"
        return File(picturesDir, imageFileName)
    }

    suspend fun exportPhotoToGallery(photoFile: File): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, photoFile.name)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                resolver.openOutputStream(uri)?.use { outputStream ->
                    photoFile.inputStream().use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error exporting photo to gallery", e)
            false
        }
    }

    suspend fun importPhotoFromUri(sourceUri: android.net.Uri): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val photoFile = getNewPhotoFile()
            if (photoFile != null) {
                val resolver = context.contentResolver
                resolver.openInputStream(sourceUri)?.use { inputStream ->
                    photoFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error importing photo", e)
            false
        }
    }

    suspend fun deletePhoto(photoId: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val file = File(picturesDir, photoId)
            file.delete()
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error deleting photo", e)
            false
        }
    }
}