package com.example.demo.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "videos")
class Video(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = 0,
    var fileName: String,
    var encryptedFilePath: String,
    val originalPath: String,
    val originalSize: Long,
    val duration: Long,
    val iv: String,
    val secretKey: String,
) : Serializable