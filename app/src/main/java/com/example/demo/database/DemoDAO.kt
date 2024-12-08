package com.example.demo.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.demo.models.User
import com.example.demo.models.Video
import com.example.demo.models.Video_Shared

@Dao
interface DemoDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideo(video: Video): Long


    @Query("UPDATE videos SET fileName= :name,encryptedFilePath= :encryptedFilePath WHERE id= :id")
    suspend fun updateVideo(id: Long, name: String, encryptedFilePath: String)

    @Query("DELETE FROM videos WHERE id= :id")
    suspend fun deleteVideo(id: Long)

    @Query("SELECT * FROM videos WHERE userId=:userId")
    suspend fun getListVideo(userId: Long): List<Video>

    @Query("SELECT * FROM users WHERE email = :email AND password = :password")
    suspend fun getUserByEmailAndPassword(email: String, password: String): User

    @Query("UPDATE users SET full_name= :fullName , image= :image WHERE email= :email")
    suspend fun updateUser(fullName: String, image: String, email: String)

    @Query("SELECT * FROM users WHERE email= :email")
    suspend fun checkEmailIsExist(email: String): User

    @Query("UPDATE users SET password= :password")
    suspend fun changePassword(password: String)

    @Query("SELECT v.* FROM videos v JOIN video_shared vs ON vs.videoId = v.id JOIN users u ON u.id = vs.userId WHERE u.id= :userId")
    suspend fun getListVideoShared(userId: Long): List<Video>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertVideoShared(videoShared: Video_Shared): Long

}