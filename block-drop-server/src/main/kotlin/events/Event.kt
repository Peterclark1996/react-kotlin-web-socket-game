package events

import kotlinx.serialization.Serializable

@Serializable
data class Event(val type: EventType, val jsonData: String)