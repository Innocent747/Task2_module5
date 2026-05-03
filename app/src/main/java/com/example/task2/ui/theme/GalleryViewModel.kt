package com.example.task2.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.task2.data.Photo
import com.example.task2.data.PhotoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class GalleryViewModel(private val repository: PhotoRepository) : ViewModel() {

    private val _photos = MutableStateFlow<List<Photo>>(emptyList())
    val photos: StateFlow<List<Photo>> = _photos

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _exportMessage = MutableStateFlow<String?>(null)
    val exportMessage: StateFlow<String?> = _exportMessage

    init {
        loadPhotos()
    }

    fun loadPhotos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val loadedPhotos = repository.loadAllPhotos()
                _photos.value = loadedPhotos
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPhoto() {
        viewModelScope.launch {
            loadPhotos()
        }
    }

    fun exportPhoto(photo: Photo) {
        viewModelScope.launch {
            try {
                val success = repository.exportPhotoToGallery(photo.file)
                if (success) {
                    _exportMessage.value = "✓ Фото добавлено в галерею"
                } else {
                    _exportMessage.value = "✗ Ошибка при экспорте"
                }
            } catch (e: Exception) {
                _exportMessage.value = "✗ Ошибка: ${e.message}"
            }
        }
    }

    fun importPhoto(uri: Uri) {
        viewModelScope.launch {
            try {
                val success = repository.importPhotoFromUri(uri)
                if (success) {
                    _exportMessage.value = "✓ Фото импортировано"
                    loadPhotos()
                } else {
                    _exportMessage.value = "✗ Ошибка при импорте"
                }
            } catch (e: Exception) {
                _exportMessage.value = "✗ Ошибка: ${e.message}"
            }
        }
    }

    fun deletePhoto(photoId: String) {
        viewModelScope.launch {
            try {
                if (repository.deletePhoto(photoId)) {
                    _photos.value = _photos.value.filter { it.id != photoId }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun clearExportMessage() {
        _exportMessage.value = null
    }
}

class GalleryViewModelFactory(private val repository: PhotoRepository) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return GalleryViewModel(repository) as T
    }
}