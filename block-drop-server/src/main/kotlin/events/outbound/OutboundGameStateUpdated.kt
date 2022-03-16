package events.outbound

import kotlinx.serialization.Serializable
import state.Tiles

@Serializable
data class Player(val id: Int, val name: String, val score: Int, val isDead: Boolean)

@Serializable
class OutboundGameStateUpdated(val tick: Int, val tiles: Tiles, val players: Set<Player>)