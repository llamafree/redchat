package com.llama.redchat.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.llama.redchat.command.CommandResult
import com.llama.redchat.command.SlashCommandParser
import com.llama.redchat.domain.model.*
import com.llama.redchat.repository.ChatRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    val repository = ChatRepository(application, viewModelScope)

    val messagesState: StateFlow<Map<String, List<Message>>> = repository.messagesState
    val channelsState: StateFlow<List<Channel>> = repository.channelsState
    val activePeers: StateFlow<List<Peer>> = repository.activePeers
    val settingsState: StateFlow<AppSettings> = repository.settingsState
    val torStatus: StateFlow<TorNodeStatus> = repository.torManager.torStatus
    val logsState: StateFlow<List<LogEntry>> = repository.logsState
    val decoysCount: StateFlow<Int> = repository.coverTrafficGenerator.sentDecoysCount
    val relayState: StateFlow<RelayState> = repository.relayManager.relayState

    // Currently open conversation
    private val _activeChatId = MutableStateFlow<String?>("peer_sombra")
    val activeChatId: StateFlow<String?> = _activeChatId

    private val _commandFeedback = MutableSharedFlow<String>()
    val commandFeedback: SharedFlow<String> = _commandFeedback

    fun openChat(id: String) {
        _activeChatId.value = id
    }

    fun sendMessage(targetId: String, isChannel: Boolean, text: String, type: MessageType = MessageType.TEXT, attachmentUrl: String? = null, attachmentName: String? = null) {
        if (text.trim().startsWith("/")) {
            handleSlashCommand(targetId, text)
            return
        }
        repository.sendMessage(targetId, isChannel, text, type, attachmentUrl, attachmentName)
    }

    private fun handleSlashCommand(targetId: String, input: String) {
        viewModelScope.launch {
            when (val result = SlashCommandParser.parse(input)) {
                is CommandResult.JoinChannel -> {
                    val chan = channelsState.value.find { it.name.lowercase().contains(result.channelName.lowercase()) }
                    if (chan != null) {
                        openChat(chan.channelId)
                        _commandFeedback.emit("Unido al canal ${chan.name}")
                    } else {
                        _commandFeedback.emit("Canal #${result.channelName} no encontrado.")
                    }
                }
                is CommandResult.SendPrivateMessage -> {
                    val peer = activePeers.value.find { it.nickname.equals(result.targetUser, ignoreCase = true) }
                    if (peer != null) {
                        repository.sendMessage(peer.peerId, false, result.messageText)
                        openChat(peer.peerId)
                    } else {
                        _commandFeedback.emit("Usuario @${result.targetUser} no encontrado en la red.")
                    }
                }
                is CommandResult.ListWho -> {
                    val list = activePeers.value.joinToString(", ") { "@${it.nickname}" }
                    _commandFeedback.emit("Pares en línea: $list")
                }
                is CommandResult.ListChannels -> {
                    val list = channelsState.value.joinToString(", ") { it.name }
                    _commandFeedback.emit("Canales activos: $list")
                }
                is CommandResult.BlockUser -> {
                    _commandFeedback.emit("Usuario @${result.targetUser} bloqueado.")
                }
                is CommandResult.UnblockUser -> {
                    _commandFeedback.emit("Usuario @${result.targetUser} desbloqueado.")
                }
                is CommandResult.ClearChat -> {
                    repository.clearChatInMemory(targetId)
                    _commandFeedback.emit("Historial en memoria limpiado para este chat.")
                }
                is CommandResult.SetChannelPassword -> {
                    _commandFeedback.emit("Contraseña establecida con derivación Argon2id.")
                }
                is CommandResult.TransferOwnership -> {
                    _commandFeedback.emit("Propiedad transferida a @${result.targetUser}.")
                }
                is CommandResult.SaveRetention -> {
                    _commandFeedback.emit("Retención habilitada para el propietario.")
                }
                is CommandResult.Error -> {
                    _commandFeedback.emit(result.message)
                }
                CommandResult.NotACommand -> {}
            }
        }
    }

    fun createChannel(name: String, description: String, isProtected: Boolean, password: String?) {
        repository.createChannel(name, description, isProtected, password)
    }

    fun updateNickname(nickname: String) = repository.updateNickname(nickname)
    fun updateTheme(theme: String) = repository.updateTheme(theme)
    fun updateLanguage(lang: String) = repository.updateLanguage(lang)
    fun toggleBleMesh(enabled: Boolean) = repository.toggleBleMesh(enabled)
    fun toggleTor(enabled: Boolean) = repository.toggleTor(enabled)
    fun toggleCoverTraffic(enabled: Boolean) = repository.toggleCoverTraffic(enabled)
    fun emergencyWipe() = repository.emergencyWipe()
    fun clearLogs() = repository.clearLogs()
    fun updateRelaySettings(
        enabled: Boolean,
        onlyCharging: Boolean,
        onlyWifi: Boolean,
        minBattery: Int,
        screenOff: Boolean,
        limitBattery: Boolean,
        limitData: Boolean
    ) = repository.updateRelaySettings(enabled, onlyCharging, onlyWifi, minBattery, screenOff, limitBattery, limitData)
}
