package com.example.demo.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null,
    var image: String = "",
    var email: String = "",
    @ColumnInfo(name = "full_name")
    var fullName: String = "",
    var password: String = "",
)