package events.inbound

import KeyTypes
import kotlinx.serialization.Serializable

@Serializable
data class InboundUpdatePressedKey(val pressedKey: KeyTypes)