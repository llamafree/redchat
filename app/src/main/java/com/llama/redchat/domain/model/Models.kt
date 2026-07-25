package com.llama.redchat.domain.model

data class Message(
    val id: String,
    val senderPeerId: String,
    val senderNickname: String,
    val targetId: String, // peerId or channelId
    val isChannel: Boolean = false,
    val content: String,
    val type: MessageType = MessageType.TEXT,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.SENT,
    val isEncrypted: Boolean = true,
    val transport: TransportType = TransportType.BLE_MESH,
    val rssiDbm: Int = -62,
    val replyToId: String? = null,
    val replyToContent: String? = null,
    val attachmentUrl: String? = null,
    val attachmentName: String? = null
)

data class Peer(
    val peerId: String,
    val nickname: String,
    val rssiDbm: Int = -65,
    val latencyMs: Long = 12,
    val transport: TransportType = TransportType.BLE_MESH,
    val isBlocked: Boolean = false,
    val isOnline: Boolean = true,
    val isTyping: Boolean = false,
    val lastActiveTimestamp: Long = System.currentTimeMillis(),
    val publicKey: String = "X25519-0x8f3a..."
) {
    val rssiLevel: RssiLevel
        get() = when {
            rssiDbm >= -55 -> RssiLevel.EXCELLENT
            rssiDbm >= -70 -> RssiLevel.GOOD
            rssiDbm >= -85 -> RssiLevel.FAIR
            else -> RssiLevel.WEAK
        }
}

data class Channel(
    val channelId: String,
    val name: String,
    val description: String,
    val isProtected: Boolean = false,
    val ownerPeerId: String,
    val memberCount: Int = 1,
    val isHistorySaved: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class TorNodeStatus(
    val isConnected: Boolean = true,
    val currentNode: String = "185.220.101.5 [Exit Relay Germany]",
    val latencyMs: Long = 142,
    val activeCircuits: Int = 3
)

data class AppSettings(
    val nickname: String = "RedPeer-${(1000..9999).random()}",
    val theme: String = "DARK", // DARK, LIGHT, SYSTEM
    val language: String = "ES", // ES, EN
    val isBleMeshEnabled: Boolean = true,
    val isTorEnabled: Boolean = true,
    val isCoverTrafficEnabled: Boolean = false,
    val coverTrafficIntervalSec: Int = 5,
    val isVibrationEnabled: Boolean = true,
    val isSoundsEnabled: Boolean = true,
    val isNotificationsEnabled: Boolean = true
)

data class LogEntry(
    val timestamp: Long = System.currentTimeMillis(),
    val tag: String,
    val message: String,
    val level: String = "INFO"
)

data class RelayState(
    val isEnabled: Boolean = false,
    val onlyWhenCharging: Boolean = false,
    val onlyWifi: Boolean = false,
    val minBatteryPercent: Int = 15,
    val screenOffOnly: Boolean = false,
    val limitBattery: Boolean = true,
    val limitData: Boolean = true,
    val activeTimeSeconds: Long = 0,
    val packetsRetransmittedCount: Int = 0,
    val devicesHelpedCount: Int = 0,
    val averageLatencyMs: Long = 14,
    val batteryUsagePercent: Float = 0.2f,
    val dataUsageBytes: Long = 0,
    val lastActivityTimestamp: Long = 0
)

