package com.llama.redchat.repository

import android.content.Context
import com.llama.redchat.covertraffic.CoverTrafficGenerator
import com.llama.redchat.crypto.CryptoEngine
import com.llama.redchat.data.db.AppDatabase
import com.llama.redchat.data.db.entity.ChannelEntity
import com.llama.redchat.data.db.entity.PeerEntity
import com.llama.redchat.data.db.entity.SettingEntity
import com.llama.redchat.domain.model.*
import com.llama.redchat.mesh.BleMeshEngine
import com.llama.redchat.network.RelayManager
import com.llama.redchat.network.TorManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class ChatRepository(
    private val context: Context,
    val scope: CoroutineScope
) {
    private val db = AppDatabase.getInstance(context)

    val bleMeshEngine = BleMeshEngine(scope)
    val torManager = TorManager(scope)
    val coverTrafficGenerator = CoverTrafficGenerator(scope, bleMeshEngine)
    val relayManager = RelayManager(scope, bleMeshEngine)

    // In-Memory volatile storage for Private DMs & Channel Messages
    // TargetId -> List of Messages
    private val _messagesState = MutableStateFlow<Map<String, List<Message>>>(emptyMap())
    val messagesState: StateFlow<Map<String, List<Message>>> = _messagesState

    // App Settings State
    private val _settingsState = MutableStateFlow(AppSettings())
    val settingsState: StateFlow<AppSettings> = _settingsState

    // Live Logs State
    private val _logsState = MutableStateFlow<List<LogEntry>>(emptyList())
    val logsState: StateFlow<List<LogEntry>> = _logsState

    // Default Channels
    private val _channelsState = MutableStateFlow<List<Channel>>(emptyList())
    val channelsState: StateFlow<List<Channel>> = _channelsState

    // Active Peers
    val activePeers: StateFlow<List<Peer>> = bleMeshEngine.meshPeers

    init {
        loadSettingsFromDb()
        loadChannelsFromDb()
        observeMeshPackets()
        
        // Seed initial discovered nearby peers for REDLink & Mesh
        bleMeshEngine.addDiscoveredPeer(Peer("peer_sombra", "Sombra", -48, 8, TransportType.BLE_MESH, false, true, false, System.currentTimeMillis(), "X25519-0x8f3a"))
        bleMeshEngine.addDiscoveredPeer(Peer("peer_valkyria", "Valkyria", -62, 15, TransportType.BLE_MESH, false, true, false, System.currentTimeMillis(), "X25519-0x9c21"))
        bleMeshEngine.addDiscoveredPeer(Peer("peer_nexus", "NexusNode", -75, 24, TransportType.BLE_MESH, false, true, false, System.currentTimeMillis(), "X25519-0x4e12"))

        addLog("SYSTEM", "REDChat Core Engine inicializado correctamente")
    }

    private fun addLog(tag: String, msg: String, level: String = "INFO") {
        val entry = LogEntry(tag = tag, message = msg, level = level)
        val current = _logsState.value.toMutableList()
        current.add(0, entry)
        if (current.size > 100) current.removeAt(current.lastIndex)
        _logsState.value = current
    }

    private fun loadSettingsFromDb() {
        scope.launch(Dispatchers.IO) {
            db.settingDao().getAllSettings().collect { list ->
                val map = list.associate { it.key to it.value }
                _settingsState.value = AppSettings(
                    nickname = map["nickname"] ?: "RedPeer-${(1000..9999).random()}",
                    theme = map["theme"] ?: "DARK",
                    language = map["language"] ?: "ES",
                    isBleMeshEnabled = map["isBleMeshEnabled"]?.toBooleanStrictOrNull() ?: true,
                    isTorEnabled = map["isTorEnabled"]?.toBooleanStrictOrNull() ?: true,
                    isCoverTrafficEnabled = map["isCoverTrafficEnabled"]?.toBooleanStrictOrNull() ?: false,
                    isVibrationEnabled = map["isVibrationEnabled"]?.toBooleanStrictOrNull() ?: true,
                    isSoundsEnabled = map["isSoundsEnabled"]?.toBooleanStrictOrNull() ?: true,
                    isNotificationsEnabled = map["isNotificationsEnabled"]?.toBooleanStrictOrNull() ?: true
                )
            }
        }
    }

    private fun loadChannelsFromDb() {
        scope.launch(Dispatchers.IO) {
            db.channelDao().getAllChannels().collect { entities ->
                if (entities.isEmpty()) {
                    // Seed initial default channel
                    val defaultChannels = listOf(
                        Channel("chan_general", "#general", "Canal general de la red mesh", false, "my_self", 1)
                    )
                    defaultChannels.forEach { c ->
                        db.channelDao().insertOrUpdateChannel(
                            ChannelEntity(c.channelId, c.name, c.description, c.isProtected, null, c.ownerPeerId, c.memberCount, c.isHistorySaved, c.createdAt)
                        )
                    }
                    _channelsState.value = defaultChannels
                } else {
                    _channelsState.value = entities.map {
                        Channel(it.channelId, it.name, it.description, it.isProtected, it.ownerPeerId, it.memberCount, it.isHistorySaved, it.createdAt)
                    }
                }
            }
        }
    }

    private fun observeMeshPackets() {
        scope.launch {
            bleMeshEngine.incomingPackets.collect { packet ->
                addLog("MESH_PKT", "Paquete recibido ID: ${packet.packetId} TTL: ${packet.ttl}")
            }
        }
    }

    /**
     * Sends a real-time message (private DM or Channel message)
     */
    fun sendMessage(
        targetId: String,
        isChannel: Boolean,
        text: String,
        type: MessageType = MessageType.TEXT,
        attachmentUrl: String? = null,
        attachmentName: String? = null
    ) {
        val myNick = _settingsState.value.nickname
        val msgId = "MSG-" + UUID.randomUUID().toString().take(8)

        // Encrypt message payload with AES-256-GCM
        val encryptedContent = CryptoEngine.encryptAes256Gcm(text)
        addLog("CRYPTO", "Payload cifrado con AES-256-GCM (Longitud: ${encryptedContent.length} bytes)")

        val newMessage = Message(
            id = msgId,
            senderPeerId = "my_self",
            senderNickname = myNick,
            targetId = targetId,
            isChannel = isChannel,
            content = text,
            type = type,
            timestamp = System.currentTimeMillis(),
            status = MessageStatus.SENT,
            isEncrypted = true,
            transport = if (torManager.torStatus.value.isConnected) TransportType.TOR else TransportType.BLE_MESH,
            rssiDbm = -60,
            attachmentUrl = attachmentUrl,
            attachmentName = attachmentName
        )

        // Store in volatile in-memory map
        val map = _messagesState.value.toMutableMap()
        val list = (map[targetId] ?: emptyList()).toMutableList()
        list.add(newMessage)
        map[targetId] = list
        _messagesState.value = map

        // Send over BLE Mesh packet
        val packet = com.llama.redchat.mesh.MeshPacket(
            packetId = msgId,
            sourcePeerId = "my_self",
            destinationId = targetId,
            payloadBase64 = encryptedContent,
            ttl = 7,
            signature = CryptoEngine.signEd25519(encryptedContent)
        )
        bleMeshEngine.sendPacket(packet)
    }

    private fun updateMessageStatus(targetId: String, msgId: String, status: MessageStatus) {
        val map = _messagesState.value.toMutableMap()
        val list = (map[targetId] ?: return).toMutableList()
        val idx = list.indexOfFirst { it.id == msgId }
        if (idx != -1) {
            list[idx] = list[idx].copy(status = status)
            map[targetId] = list
            _messagesState.value = map
        }
    }

    fun clearChatInMemory(targetId: String) {
        val map = _messagesState.value.toMutableMap()
        map[targetId] = emptyList()
        _messagesState.value = map
        addLog("PRIVACY", "Memoria RAM del chat $targetId limpiada exitosamente.")
    }

    fun updateNickname(newNickname: String) {
        _settingsState.value = _settingsState.value.copy(nickname = newNickname)
        saveSettingToDb("nickname", newNickname)
    }

    fun updateTheme(theme: String) {
        _settingsState.value = _settingsState.value.copy(theme = theme)
        saveSettingToDb("theme", theme)
    }

    fun updateLanguage(lang: String) {
        _settingsState.value = _settingsState.value.copy(language = lang)
        saveSettingToDb("language", lang)
    }

    fun toggleBleMesh(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(isBleMeshEnabled = enabled)
        bleMeshEngine.setMeshActive(enabled)
        saveSettingToDb("isBleMeshEnabled", enabled.toString())
    }

    fun toggleTor(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(isTorEnabled = enabled)
        torManager.setTorEnabled(enabled)
        saveSettingToDb("isTorEnabled", enabled.toString())
    }

    fun toggleCoverTraffic(enabled: Boolean) {
        _settingsState.value = _settingsState.value.copy(isCoverTrafficEnabled = enabled)
        coverTrafficGenerator.setCoverTrafficEnabled(enabled)
        saveSettingToDb("isCoverTrafficEnabled", enabled.toString())
    }

    fun createChannel(name: String, description: String, isProtected: Boolean, password: String?) {
        val chanId = "chan_" + name.lowercase().replace(" ", "_").replace("#", "")
        val formattedName = if (name.startsWith("#")) name else "#$name"
        val channel = Channel(
            channelId = chanId,
            name = formattedName,
            description = description,
            isProtected = isProtected,
            ownerPeerId = "my_self",
            memberCount = 1
        )

        scope.launch(Dispatchers.IO) {
            db.channelDao().insertOrUpdateChannel(
                ChannelEntity(
                    channelId = chanId,
                    name = formattedName,
                    description = description,
                    isProtected = isProtected,
                    passwordHash = if (isProtected && password != null) String(CryptoEngine.hashArgon2id(password)) else null,
                    ownerPeerId = "my_self",
                    memberCount = 1,
                    isHistorySaved = false,
                    createdAt = System.currentTimeMillis()
                )
            )
            val current = _channelsState.value.toMutableList()
            current.add(0, channel)
            _channelsState.value = current
            addLog("CHANNEL", "Nuevo canal $formattedName creado")
        }
    }

    private fun saveSettingToDb(key: String, value: String) {
        scope.launch(Dispatchers.IO) {
            db.settingDao().setSetting(SettingEntity(key, value))
        }
    }

    /**
     * Emergency Wipe: Wipes all RAM messages, Room Database, Keys & Settings immediately
     */
    fun emergencyWipe() {
        addLog("EMERGENCY", "INICIANDO BORRADO DE EMERGENCIA CRÍTICO...", "CRITICAL")
        scope.launch(Dispatchers.IO) {
            // 1. Clear RAM
            _messagesState.value = emptyMap()
            bleMeshEngine.clearMeshState()

            // 2. Clear Database
            db.peerDao().clearAllPeers()
            db.channelDao().clearAllChannels()
            db.settingDao().clearAllSettings()
            db.channelMessageDao().clearAllChannelMessages()

            // 3. Reset Crypto session keys
            CryptoEngine.rotateSessionKey()

            // 4. Reset settings state
            _settingsState.value = AppSettings()
            _channelsState.value = emptyList()

            addLog("EMERGENCY", "BORRADO DE EMERGENCIA COMPLETADO. TODOS LOS DATOS FUERON DESTRUIDOS.", "CRITICAL")
        }
    }

    fun clearLogs() {
        _logsState.value = emptyList()
    }

    fun updateRelaySettings(
        enabled: Boolean,
        onlyCharging: Boolean,
        onlyWifi: Boolean,
        minBattery: Int,
        screenOff: Boolean,
        limitBattery: Boolean,
        limitData: Boolean
    ) {
        relayManager.updateSettings(
            enabled = enabled,
            onlyCharging = onlyCharging,
            onlyWifi = onlyWifi,
            minBattery = minBattery,
            screenOff = screenOff,
            limitBattery = limitBattery,
            limitData = limitData
        )
        addLog("RELAY", if (enabled) "Modo Relay Voluntario activado" else "Modo Relay Voluntario desactivado")
    }
}

