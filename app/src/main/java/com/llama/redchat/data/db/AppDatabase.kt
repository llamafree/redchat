package com.llama.redchat.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.llama.redchat.data.db.dao.*
import com.llama.redchat.data.db.entity.*

@Database(
    entities = [
        PeerEntity::class,
        ChannelEntity::class,
        SettingEntity::class,
        ChannelMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun peerDao(): PeerDao
    abstract fun channelDao(): ChannelDao
    abstract fun settingDao(): SettingDao
    abstract fun channelMessageDao(): ChannelMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "redchat_db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
