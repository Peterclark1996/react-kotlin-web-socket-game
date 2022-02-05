import { useEffect, useState } from 'react'
import { useWebSocket } from '../Contexts/WebSocketContext'
import { generateHtmlId } from '../helpers'

const roomUsersUpdatedEventId = generateHtmlId()

const PlayerList = () => {
    const { on, send } = useWebSocket()

    const [players, setPlayers] = useState([])

    useEffect(() => {
        send("InboundRequestRoomUsers", {})
    }, [send])

    useEffect(() => {
        on(
            "OutboundRoomUsersUpdated", 
            event => setPlayers(event.usernames), 
            roomUsersUpdatedEventId
        )
    }, [on])

    return(
        <div className="d-flex flex-column align-items-center">
            <span className="border-bottom">Players</span>
                {
                    players.map(username => <span key={username}>{username}</span>)
                }
        </div>
    )
}

export default PlayerList