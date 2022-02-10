package events.outbound

import kotlinx.serialization.Serializable
import state.Tiles

@Serializable
class OutboundGameStateUpdated(val tick: Int, val tiles: Tiles)