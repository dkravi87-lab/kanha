package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.KishuTypeConverters
import com.example.data.model.ProjectEntity
import com.example.data.model.SceneEntity
import androidx.room.TypeConverters

@Database(entities = [ProjectEntity::class, SceneEntity::class], version = 1, exportSchema = false)
@TypeConverters(KishuTypeConverters::class)
abstract class KishuDatabase : RoomDatabase() {

    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: KishuDatabase? = null

        fun getInstance(context: Context): KishuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    KishuDatabase::class.java,
                    "kishu_studio_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
