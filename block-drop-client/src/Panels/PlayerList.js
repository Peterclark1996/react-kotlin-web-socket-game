import { useEffect, useRef } from 'react'
import { useWebSocket } from '../Contexts/WebSocketContext'
import BlockColours from '../Enums/BlockColours'

const PlayerList = ({ connectedPlayers, playerState }) => {
    const { on, send } = useWebSocket()

    const isMounted = useRef(true)

    useEffect(() => {
        isMounted.current = true
        return () => isMounted.current = false
    },[])

    useEffect(() => {
        if(!isMounted.current) return
        if(connectedPlayers.length > 0) return

        send("InboundRequestRoomUsers", {})
        
    }, [connectedPlayers.length, on, send])

    return(
        <div className="d-flex flex-column align-items-center">
            <span className="border-bottom">Players</span>
                {
                    connectedPlayers.map(username => {
                        const player = playerState.find(p => p.name === username)
                        return (
                            <div key={username} className="m-1 px-1 Rounded" style={{"backgroundColor": player ? BlockColours[player.id] : ""}}>
                                    <span className={`mb-1 ${player && player.isDead ? "StrikeThrough" : ""}`}>{username}</span>
                                    <span className="ms-3 mb-1">{player ? player.score : 0}</span>
                            </div>
                        )
                    })
                }
        </div>
    )
}

export default PlayerList