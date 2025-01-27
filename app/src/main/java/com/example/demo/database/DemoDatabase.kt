package com.example.demo.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.demo.models.User
import com.example.demo.models.Video
import com.example.demo.models.VideoShared

@Database(entities = [User::class, Video::class, VideoShared::class], version = 1)
abstract class DemoDatabase : RoomDatabase() {
    companion object {
        private var INSTANCE: DemoDatabase? = null
        private const val DATABASE_NAME = "demo.db"

        @Synchronized
        fun getInstance(context: Context): DemoDatabase {
            if (INSTANCE == null) {
                synchronized(DemoDatabase::class.java) {
                    if (INSTANCE == null) {
                        INSTANCE = Room.databaseBuilder(
                            context.applicationContext,
                            DemoDatabase::class.java,
                            DATABASE_NAME
                        )
                            .allowMainThreadQueries()
                            .build()
                    }
                }
            }
            return INSTANCE!!
        }
    }

    abstract fun demoDAO(): DemoDAO
}