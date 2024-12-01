package com.example.demo.models

import androidx.room.Entity

@Entity(tableName = "users")
class User(
    var id: String? = null,
    var image: String="",
    var email: String="",
    var fullName: String="",
    var password: String="",
)