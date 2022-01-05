package events

import kotlinx.serialization.Serializable

@Serializable
data class Event(val type: String, val jsonData: String)