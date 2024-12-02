package com.example.demo.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.demo.models.User

@Dao
interface DemoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User): Long

    @Query("SELECT * FROM users")
    fun getListUser(): LiveData<List<User>>

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    fun getUserByEmailAndPassword(email: String, password: String): User

    @Query("UPDATE users SET full_name= :fullName AND image= :image WHERE email= :email")
    fun updateUser(fullName: String, image: String, email: String)

    @Query("SELECT * FROM users WHERE email= :email")
    fun checkEmailIsExist(email: String): User

    @Query("UPDATE users SET password= :password")
    fun changePassword(password: String)

}