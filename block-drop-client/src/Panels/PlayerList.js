import { useEffect, useState } from 'react'
import { useWebSocket } from '../Contexts/WebSocketContext'
import { generateHtmlId } from '../helpers'

const roomUsersUpdatedEventId = generateHtmlId()

const PlayerList = ({ playerState }) => {
    const { on, send } = useWebSocket()

    const [connectedPlayers, setConnectedPlayers] = useState([])

    useEffect(() => {
        send("InboundRequestRoomUsers", {})
    }, [send])

    useEffect(() => {
        on(
            "OutboundRoomUsersUpdated", 
            event => setConnectedPlayers(event.usernames), 
            roomUsersUpdatedEventId
        )
    }, [on])

    return(
        <div className="d-flex flex-column align-items-center">
            <span className="border-bottom">Players</span>
                {
                    connectedPlayers.map(username => {
                        const player = playerState.find(p => p.name === username)
                        return (
                            <div key={username}>
                                    <span className={player && player.isDead ? "StrikeThrough" : ""}>{username}</span>
                                    <span className="ms-2">{player ? player.score : 0}</span>
                            </div>
                        )
                    })
                }
        </div>
    )
}

export default PlayerList