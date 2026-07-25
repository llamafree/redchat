package com.llama.redchat.network

import com.llama.redchat.domain.model.TorNodeStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TorManager(private val scope: CoroutineScope) {

    private val _torStatus = MutableStateFlow(
        TorNodeStatus(
            isConnected = true,
            currentNode = "185.220.101.5 [Exit Relay Frankfurt]",
            latencyMs = 120,
            activeCircuits = 3
        )
    )
    val torStatus: StateFlow<TorNodeStatus> = _torStatus

    fun setTorEnabled(enabled: Boolean) {
        _torStatus.value = _torStatus.value.copy(
            isConnected = enabled,
            currentNode = if (enabled) "185.220.101.5 [Exit Relay Frankfurt]" else "Desconectado",
            latencyMs = if (enabled) 120 else 0,
            activeCircuits = if (enabled) 3 else 0
        )
    }
}
