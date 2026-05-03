package com.example.task2.data

import java.io.File

data class Photo(
    val id: String,
    val filename: String,
    val file: File,
    val timestamp: Long
)