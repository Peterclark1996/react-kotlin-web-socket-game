package events.outbound

import kotlinx.serialization.Serializable
import state.Tiles

@Serializable
data class Player(val name: String, val score: Int)

@Serializable
class OutboundGameStateUpdated(val tick: Int, val tiles: Tiles, val players: Set<Player>)