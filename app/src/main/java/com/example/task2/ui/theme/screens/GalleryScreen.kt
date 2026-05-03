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
import androidx.compose.material.icons.filled.MoreVert
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
    onTakePhotoClick: () -> Unit,
    onImportPhotoClick: () -> Unit
) {
    val photos by viewModel.photos.collectAsState()
    val exportMessage by viewModel.exportMessage.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedPhoto by remember { mutableStateOf<Photo?>(null) }
    var expandedMenuId by remember { mutableStateOf<String?>(null) }

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
            Column(
                modifier = Modifier.padding(end = 16.dp, bottom = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = onImportPhotoClick,
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Text("📱", fontSize = MaterialTheme.typography.headlineSmall.fontSize)
                }
                FloatingActionButton(
                    onClick = onTakePhotoClick,
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Сделать фото")
                }
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
                    ) {
                        AsyncImage(
                            model = photo.file,
                            contentDescription = "Photo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )

                        // Menu button
                        IconButton(
                            onClick = {
                                expandedMenuId = if (expandedMenuId == photo.id) null else photo.id
                            },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.7f))
                        ) {
                            Icon(
                                Icons.Filled.MoreVert,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }

                        // Dropdown Menu
                        DropdownMenu(
                            expanded = expandedMenuId == photo.id,
                            onDismissRequest = { expandedMenuId = null },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            DropdownMenuItem(
                                text = { Text("📤 Экспорт в галерею") },
                                onClick = {
                                    viewModel.exportPhoto(photo)
                                    expandedMenuId = null
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("🗑️ Удалить") },
                                onClick = {
                                    selectedPhoto = photo
                                    showDeleteDialog = true
                                    expandedMenuId = null
                                }
                            )
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
            text = { Text("Это действие невозможно отменить") },
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