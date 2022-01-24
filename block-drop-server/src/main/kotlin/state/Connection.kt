package state

import io.ktor.http.cio.websocket.*

class Connection(val session: DefaultWebSocketSession) {
    var roomCode: String? = null
    var username: String? = null
    var pressedKey: KeyTypes = KeyTypes.NONE
}

enum class KeyTypes{
    NONE,
    DOWN,
    LEFT,
    RIGHT
}