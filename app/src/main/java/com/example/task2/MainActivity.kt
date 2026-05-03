package com.example.task2

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.task2.data.PhotoRepository
import com.example.task2.ui.GalleryViewModel
import com.example.task2.ui.GalleryViewModelFactory
import com.example.task2.ui.screens.GalleryScreen
import com.example.task2.ui.theme.Task2Theme
import com.example.task2.utils.CameraHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Task2Theme {
                val repository = remember { PhotoRepository(this@MainActivity) }
                val viewModel: GalleryViewModel = viewModel(
                    factory = GalleryViewModelFactory(repository)
                )

                val cameraLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.TakePicture()
                ) { success ->
                    if (success) {
                        viewModel.addPhoto()
                    }
                }

                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    if (isGranted) {
                        launchCamera(repository, cameraLauncher)
                    }
                }

                GalleryScreen(
                    viewModel = viewModel,
                    onTakePhotoClick = {
                        if (ContextCompat.checkSelfPermission(
                                this@MainActivity,
                                Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            launchCamera(repository, cameraLauncher)
                        } else {
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    }
                )
            }
        }
    }

    private fun launchCamera(
        repository: PhotoRepository,
        cameraLauncher: androidx.activity.result.ActivityResultLauncher<android.net.Uri>
    ) {
        val photoFile = repository.getNewPhotoFile()
        if (photoFile != null) {
            val photoUri = androidx.core.content.FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoUri)
        }
    }
}