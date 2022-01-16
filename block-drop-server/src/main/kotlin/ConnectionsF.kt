fun Iterable<Connection>.getAllUsersInRoom(room: String) =
    this.getAllConnectionsInRoom(room)
        .map { c -> c.username }
        .mapNotNull { it }

fun Iterable<Connection>.getAllConnectionsInRoom(room: String) =
    this.filter { c -> c.room == room }