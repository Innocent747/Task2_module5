package com.example.task2.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.task2.data.Photo
import com.example.task2.ui.GalleryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    viewModel: GalleryViewModel,
    onTakePhotoClick: () -> Unit
) {
    val photos by viewModel.photos.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    var showOptions by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(exportMessage) {
        if (exportMessage != null) {
            snackbarHostState.showSnackbar(exportMessage!!)
            viewModel.clearExportMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Галерея фото") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onTakePhotoClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Сделать фото")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        if (photos.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "У вас пока нет фото",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Button(onClick = onTakePhotoClick) {
                    Text("Сделать первое фото")
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(photos, key = { it.id }) { photo ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showOptions = photo.id }
                    ) {
                        AsyncImage(
                            model = photo.file,
                            contentDescription = "Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        if (showOptions == photo.id) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    FloatingActionButton(
                                        onClick = {
                                            viewModel.exportPhoto(photo)
                                            showOptions = null
                                        },
                                        modifier = Modifier.size(48.dp),
                                        containerColor = MaterialTheme.colorScheme.secondary
                                    ) {
                                        Text("📤", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                                    }

                                    FloatingActionButton(
                                        onClick = {
                                            selectedPhoto = photo
                                            showDeleteDialog = true
                                            showOptions = null
                                        },
                                        modifier = Modifier.size(48.dp),
                                        containerColor = MaterialTheme.colorScheme.error
                                    ) {
                                        Icon(
                                            Icons.Filled.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.onError
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && selectedPhoto != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить фото?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePhoto(selectedPhoto!!.id)
                        showDeleteDialog = false
                        selectedPhoto = null
                    }
                ) {
                    Text("Удалить")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Отмена")
                }
            }
        )
    }
}