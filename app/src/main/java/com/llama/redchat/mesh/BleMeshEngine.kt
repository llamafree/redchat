package com.llama.redchat.mesh

import com.llama.redchat.domain.model.Message
import com.llama.redchat.domain.model.Peer
import com.llama.redchat.domain.model.TransportType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class MeshPacket(
    val packetId: String,
    val sourcePeerId: String,
    val destinationId: String,
    val payloadBase64: String,
    val ttl: Int = 7,
    val signature: String,
    val timestamp: Long = System.currentTimeMillis()
)

class BleMeshEngine(private val scope: CoroutineScope) {

    private val _isMeshActive = MutableStateFlow(true)
    val isMeshActive: StateFlow<Boolean> = _isMeshActive

    private val _meshPeers = MutableStateFlow<List<Peer>>(emptyList())
    val meshPeers: StateFlow<List<Peer>> = _meshPeers

    private val _incomingPackets = MutableSharedFlow<MeshPacket>()
    val incomingPackets: SharedFlow<MeshPacket> = _incomingPackets

    // Deduplication table for packet IDs
    private val seenPacketIds = mutableSetOf<String>()

    init {
        // Active mesh listening engine
    }

    fun setMeshActive(active: Boolean) {
        _isMeshActive.value = active
    }

    /**
     * Broadcasts or forwards a mesh packet with deduplication & TTL decrement
     */
    fun sendPacket(packet: MeshPacket) {
        if (!_isMeshActive.value) return
        if (seenPacketIds.contains(packet.packetId)) return // Deduplicated

        seenPacketIds.add(packet.packetId)

        scope.launch(Dispatchers.Default) {
            // Forwarding multi-hop
            if (packet.ttl > 0) {
                val forwarded = packet.copy(ttl = packet.ttl - 1)
                _incomingPackets.emit(forwarded)
            }
        }
    }

    fun addDiscoveredPeer(peer: Peer) {
        val current = _meshPeers.value.toMutableList()
        current.removeAll { it.peerId == peer.peerId }
        current.add(0, peer)
        _meshPeers.value = current
    }

    fun removePeer(peerId: String) {
        val current = _meshPeers.value.toMutableList()
        current.removeAll { it.peerId == peerId }
        _meshPeers.value = current
    }

    fun clearMeshState() {
        seenPacketIds.clear()
        _meshPeers.value = emptyList()
    }
}
