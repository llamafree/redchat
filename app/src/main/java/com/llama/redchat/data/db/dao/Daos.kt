package com.llama.redchat.data.db.dao

import androidx.room.*
import com.llama.redchat.data.db.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PeerDao {
    @Query("SELECT * FROM peers ORDER BY lastActiveTimestamp DESC")
    fun getAllPeers(): Flow<List<PeerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePeer(peer: PeerEntity)

    @Query("UPDATE peers SET isBlocked = :isBlocked WHERE peerId = :peerId")
    suspend fun setBlocked(peerId: String, isBlocked: Boolean)

    @Query("DELETE FROM peers WHERE peerId = :peerId")
    suspend fun deletePeer(peerId: String)

    @Query("DELETE FROM peers")
    suspend fun clearAllPeers()
}

@Dao
interface ChannelDao {
    @Query("SELECT * FROM channels ORDER BY createdAt DESC")
    fun getAllChannels(): Flow<List<ChannelEntity>>

    @Query("SELECT * FROM channels WHERE channelId = :channelId LIMIT 1")
    suspend fun getChannelById(channelId: String): ChannelEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateChannel(channel: ChannelEntity)

    @Query("DELETE FROM channels WHERE channelId = :channelId")
    suspend fun deleteChannel(channelId: String)

    @Query("DELETE FROM channels")
    suspend fun clearAllChannels()
}

@Dao
interface SettingDao {
    @Query("SELECT * FROM settings")
    fun getAllSettings(): Flow<List<SettingEntity>>

    @Query("SELECT value FROM settings WHERE key = :key LIMIT 1")
    suspend fun getSetting(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setSetting(setting: SettingEntity)

    @Query("DELETE FROM settings")
    suspend fun clearAllSettings()
}

@Dao
interface ChannelMessageDao {
    @Query("SELECT * FROM channel_messages WHERE channelId = :channelId ORDER BY timestamp ASC")
    fun getMessagesForChannel(channelId: String): Flow<List<ChannelMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChannelMessageEntity)

    @Query("DELETE FROM channel_messages WHERE channelId = :channelId")
    suspend fun clearChannelMessages(channelId: String)

    @Query("DELETE FROM channel_messages")
    suspend fun clearAllChannelMessages()
}
