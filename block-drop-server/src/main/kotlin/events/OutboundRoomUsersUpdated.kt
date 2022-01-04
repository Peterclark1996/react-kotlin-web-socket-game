package events

import kotlinx.serialization.Serializable

@Serializable
data class OutboundRoomUsersUpdated(val usernames: List<String>)