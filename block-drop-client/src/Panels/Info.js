import { useEffect, useState } from 'react'
import { useWebSocket } from '../Contexts/WebSocketContext'
import { generateHtmlId } from '../helpers'

const roomUsersUpdatedEventId = generateHtmlId()

const Info = ({ hasGameStarted, startGame }) => {
    const { connectionState, on, send } = useWebSocket()

    const [players, setPlayers] = useState([])

    const getStatus = () => {
        if(connectionState === 0) return <span className="text-warning">Connecting</span>
        if(connectionState === 1) return <span className="text-success">Connected</span>
        return <span className="text-danger">Disconnected</span>
    }

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
            <div className="d-flex">
                <span className="me-2">Status:</span>
                {getStatus()}
            </div>
            <div className="d-flex flex-column align-items-center border-top border-bottom w-100">
                <span>Players</span>
                {
                    players.map(username => <span key={username}>{username}</span>)
                }
            </div>
            {
                !hasGameStarted &&
                <div>
                    <div className="btn btn-success m-2 text-white px-3 py-2" onClick={startGame}>
                        <h4 className="mb-0">Start Game</h4>
                    </div>
                </div>
            }
        </div>
    )
}

export default Info