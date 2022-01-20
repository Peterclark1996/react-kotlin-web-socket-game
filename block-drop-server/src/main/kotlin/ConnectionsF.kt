fun Set<Connection>.getAllUsersInRoom(room: String) =
    this.getAllConnectionsInRoom(room)
        .map { c -> c.username }
        .mapNotNull { it }

fun Set<Connection>.getAllConnectionsInRoom(room: String) =
    this.filter { c -> c.roomCode == room }