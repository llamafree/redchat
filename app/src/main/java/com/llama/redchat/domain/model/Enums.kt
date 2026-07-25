package com.llama.redchat.domain.model

enum class TransportType {
    BLE_MESH,
    INTERNET,
    TOR
}

enum class MessageStatus {
    SENDING,
    SENT,
    DELIVERED,
    READ
}

enum class MessageType {
    TEXT,
    EMOJI,
    IMAGE,
    FILE,
    AUDIO,
    LOCATION,
    CONTACT,
    SYSTEM
}

enum class RssiLevel(val dbmRange: String, val labelEs: String, val labelEn: String) {
    EXCELLENT("-30 to -55 dBm", "Excelente", "Excellent"),
    GOOD("-56 to -70 dBm", "Buena", "Good"),
    FAIR("-71 to -85 dBm", "Regular", "Fair"),
    WEAK("-86 to -105 dBm", "Débil", "Weak")
}
