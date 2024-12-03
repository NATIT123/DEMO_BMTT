package com.example.demo.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
class User(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    var image: String = "",
    var email: String = "",
    var fullName: String = "",
    var password: String = "",
)