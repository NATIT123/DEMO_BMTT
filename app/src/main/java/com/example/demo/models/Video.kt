package com.example.demo.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
class Video(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    var fileName: String,
    var encryptedFilePath: String,
    val originalPath: String,
    val originalSize: Long,
    )