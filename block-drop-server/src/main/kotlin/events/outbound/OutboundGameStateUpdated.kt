package events.outbound

import kotlinx.serialization.Serializable

@Serializable
class OutboundGameStateUpdated(val tick: Int, val tiles: Array<IntArray>)