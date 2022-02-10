package state

import io.ktor.http.cio.websocket.*

class Connection(val session: DefaultWebSocketSession) {
    var roomCode: String? = null
    var username: String? = null
    var pressingLeft: Boolean = false
    var pressingRight: Boolean = false
    var pressingDown: Boolean = false
    var pressingRotateLeft: Boolean = false
    var pressingRotateRight: Boolean = false
}