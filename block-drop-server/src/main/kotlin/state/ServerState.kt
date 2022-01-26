package state

class ServerState {
    private val connections: MutableSet<Connection> = mutableSetOf()
    private val rooms: MutableSet<Room> = mutableSetOf()

    fun getConnections() = connections
    fun addConnection(connection: Connection){
        connections += connection
    }
    fun removeConnection(connection: Connection){
        connections -= connection
    }

    fun getRooms() = rooms
    fun addRoom(room: Room){
        rooms += room
    }
    fun removeRoom(room: Room){
        room.stop()
        rooms -= room
    }
}