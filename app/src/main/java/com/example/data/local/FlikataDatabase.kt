package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [UserEntity::class, PostEntity::class, ChatEntity::class, ViewHistoryEntity::class, SavedItemEntity::class, PageEntity::class],
    version = 7,
    exportSchema = false
)
abstract class FlikataDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun postDao(): PostDao
    abstract fun chatDao(): ChatDao
    abstract fun historyDao(): HistoryDao
    abstract fun savedDao(): SavedDao
    abstract fun pageDao(): PageDao

    companion object {
        @Volatile
        private var INSTANCE: FlikataDatabase? = null

        fun getDatabase(context: Context): FlikataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FlikataDatabase::class.java,
                    "flikata_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
