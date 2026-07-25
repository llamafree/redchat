package com.llama.redchat.network

import com.llama.redchat.domain.model.RelayState
import com.llama.redchat.mesh.BleMeshEngine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RelayManager(
    private val scope: CoroutineScope,
    private val bleMeshEngine: BleMeshEngine
) {
    private val _relayState = MutableStateFlow(RelayState())
    val relayState: StateFlow<RelayState> = _relayState

    init {
        startRelayLoop()
    }

    fun updateSettings(
        enabled: Boolean,
        onlyCharging: Boolean,
        onlyWifi: Boolean,
        minBattery: Int,
        screenOff: Boolean,
        limitBattery: Boolean,
        limitData: Boolean
    ) {
        val current = _relayState.value
        _relayState.value = current.copy(
            isEnabled = enabled,
            onlyWhenCharging = onlyCharging,
            onlyWifi = onlyWifi,
            minBatteryPercent = minBattery,
            screenOffOnly = screenOff,
            limitBattery = limitBattery,
            limitData = limitData,
            lastActivityTimestamp = if (enabled) System.currentTimeMillis() else current.lastActivityTimestamp
        )
    }

    private fun startRelayLoop() {
        scope.launch(Dispatchers.Default) {
            var secondsActive = 0L
            while (true) {
                delay(1000)
                val current = _relayState.value
                if (current.isEnabled) {
                    secondsActive++
                    // Simulate real retransmission statistics when mesh traffic flows
                    val incomingPeersCount = bleMeshEngine.meshPeers.value.size
                    val newPackets = if (incomingPeersCount > 0 && (secondsActive % 5 == 0L)) (1..3).random() else 0
                    
                    _relayState.value = current.copy(
                        activeTimeSeconds = secondsActive,
                        packetsRetransmittedCount = current.packetsRetransmittedCount + newPackets,
                        devicesHelpedCount = incomingPeersCount,
                        dataUsageBytes = current.dataUsageBytes + (newPackets * 256L),
                        lastActivityTimestamp = if (newPackets > 0) System.currentTimeMillis() else current.lastActivityTimestamp
                    )
                }
            }
        }
    }
}
