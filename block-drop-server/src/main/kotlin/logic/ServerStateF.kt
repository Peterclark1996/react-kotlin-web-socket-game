package logic

import arrow.core.flatMap
import events.outbound.OutboundGameStarted
import mapToUnit
import state.*
import toEither
import java.util.*

fun ServerState.getAllUsersInRoom(room: String) =
    this.getAllConnectionsInRoom(room)
        .map { c -> c.username }
        .mapNotNull { it }

fun ServerState.getAllConnectionsInRoom(room: String) =
    this.getConnections().filter { c -> c.roomCode == room }

fun ServerState.getUnusedRoomCode(): String {
    var randomString = UUID.randomUUID().toString().substring(0, 5)
    while (this.getRooms().find { it.roomCode == randomString } != null) {
        randomString = UUID.randomUUID().toString().substring(0, 5)
    }
    return randomString
}

suspend fun ServerState.startGame(roomCode: String) =
    this.getRooms().find { it.roomCode == roomCode }.toEither().flatMap {
        it.start(this)
        this.sendToRoom(roomCode, OutboundGameStarted.serializer(), OutboundGameStarted())
    }.mapToUnit()