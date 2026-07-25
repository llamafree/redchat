package com.llama.redchat.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "peers")
data class PeerEntity(
    @PrimaryKey val peerId: String,
    val nickname: String,
    val rssiDbm: Int,
    val latencyMs: Long,
    val transport: String,
    val isBlocked: Boolean,
    val isOnline: Boolean,
    val lastActiveTimestamp: Long,
    val publicKey: String
)

@Entity(tableName = "channels")
data class ChannelEntity(
    @PrimaryKey val channelId: String,
    val name: String,
    val description: String,
    val isProtected: Boolean,
    val passwordHash: String?,
    val ownerPeerId: String,
    val memberCount: Int,
    val isHistorySaved: Boolean,
    val createdAt: Long
)

@Entity(tableName = "settings")
data class SettingEntity(
    @PrimaryKey val key: String,
    val value: String
)

@Entity(tableName = "channel_messages")
data class ChannelMessageEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val senderPeerId: String,
    val senderNickname: String,
    val content: String,
    val type: String,
    val timestamp: Long,
    val attachmentUrl: String?,
    val attachmentName: String?
)
