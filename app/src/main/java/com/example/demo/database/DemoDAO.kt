package com.example.demo.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.demo.models.User
import com.example.demo.models.Video

@Dao
interface DemoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideo(video: Video): Long


    @Query("SELECT * FROM users")
    fun getListUser(): LiveData<List<User>>

    @Query("UPDATE videos SET fileName= :name,encryptedFilePath= :encryptedFilePath WHERE id= :id")
    suspend fun updateVideo(id: Long, name: String, encryptedFilePath: String)

    @Query("DELETE FROM videos WHERE id= :id")
    suspend fun deleteVideo(id: Long)

    @Query("SELECT * FROM videos WHERE userId=:userId")
    fun getListVideo(userId: Long): List<Video>

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    fun getUserByEmailAndPassword(email: String, password: String): User

    @Query("UPDATE users SET full_name= :fullName , image= :image WHERE email= :email")
    suspend fun updateUser(fullName: String, image: String, email: String)

    @Query("SELECT * FROM users WHERE email= :email")
    fun checkEmailIsExist(email: String): User

    @Query("UPDATE users SET password= :password")
    fun changePassword(password: String)

}