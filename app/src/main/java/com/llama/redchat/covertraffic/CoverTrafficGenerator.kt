package com.llama.redchat.covertraffic

import com.llama.redchat.crypto.CryptoEngine
import com.llama.redchat.mesh.BleMeshEngine
import com.llama.redchat.mesh.MeshPacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CoverTrafficGenerator(
    private val scope: CoroutineScope,
    private val meshEngine: BleMeshEngine
) {

    private val _isCoverTrafficEnabled = MutableStateFlow(false)
    val isCoverTrafficEnabled: StateFlow<Boolean> = _isCoverTrafficEnabled

    private val _sentDecoysCount = MutableStateFlow(0)
    val sentDecoysCount: StateFlow<Int> = _sentDecoysCount

    private var intervalSec: Int = 5

    init {
        startCoverTrafficLoop()
    }

    fun setCoverTrafficEnabled(enabled: Boolean, interval: Int = 5) {
        _isCoverTrafficEnabled.value = enabled
        intervalSec = interval
    }

    private fun startCoverTrafficLoop() {
        scope.launch(Dispatchers.Default) {
            while (true) {
                delay((intervalSec * 1000L) + (-1000..1000).random())
                if (_isCoverTrafficEnabled.value) {
                    generateAndSendDecoyPacket()
                }
            }
        }
    }

    private fun generateAndSendDecoyPacket() {
        val decoyPayload = "DECOY_PADDING_" + (1..32).map { ('A'..'Z').random() }.joinToString("")
        val encryptedPayload = CryptoEngine.encryptAes256Gcm(decoyPayload, "CoverTrafficKey")

        val packet = MeshPacket(
            packetId = "DECOY-" + UUID.randomUUID().toString().take(8),
            sourcePeerId = "anon_decoy_node",
            destinationId = "broadcast_null",
            payloadBase64 = encryptedPayload,
            ttl = (3..5).random(),
            signature = CryptoEngine.signEd25519(encryptedPayload)
        )

        meshEngine.sendPacket(packet)
        _sentDecoysCount.value += 1
    }
}
