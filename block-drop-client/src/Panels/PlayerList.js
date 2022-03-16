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
                    playerState.length === 0 ? 
                    connectedPlayers.map(username => {
                        return (
                            <div key={username} className="m-1 px-1">
                                <span className="mb-1">{username}</span>
                            </div>
                        )
                    }) :
                    playerState.map(player => {
                        return (
                            <div key={player.name} className="m-1 px-1 Rounded" style={{"backgroundColor": BlockColours[player.id]}}>
                                <span className={`mb-1 ${ player.isDead ? "StrikeThrough" : ""}`}>{player.name}</span>
                                <span className="ms-3 mb-1">{player.score}</span>
                            </div>
                        )
                    })
                }
        </div>
    )
}

export default PlayerList