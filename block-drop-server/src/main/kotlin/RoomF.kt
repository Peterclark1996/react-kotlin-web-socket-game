import events.outbound.OutboundGameStarted
import events.sendToRoom
import java.util.*

suspend fun Room.startGame(connections: Set<Connection>) {
    connections.sendToRoom(roomCode, OutboundGameStarted.serializer(), OutboundGameStarted())
}

fun Set<Room>.getUnusedRoomCode(): String {
    var randomString = UUID.randomUUID().toString().substring(0, 5)
    while (this.find { it.roomCode == randomString } != null) {
        randomString = UUID.randomUUID().toString().substring(0, 5)
    }
    return randomString
}