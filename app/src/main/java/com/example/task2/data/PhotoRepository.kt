package com.example.task2.data

import android.content.Context
import android.os.Environment
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
            val galleryDir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "GalleryApp"
            )

            if (!galleryDir.exists()) {
                galleryDir.mkdirs()
            }

            val newFile = File(galleryDir, photoFile.name)
            photoFile.copyTo(newFile, overwrite = true)
            true
        } catch (e: Exception) {
            Log.e("PhotoRepository", "Error exporting photo", e)
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