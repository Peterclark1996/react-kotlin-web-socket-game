import { useEffect, useState } from 'react'
import ConnectionStatus from '../Components/ConnectionStatus'
import { useWebSocket } from '../Contexts/WebSocketContext'
import { generateHtmlId } from '../helpers'

const roomUsersUpdatedEventId = generateHtmlId()

const Info = ({ hasGameStarted, startGame }) => {
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
            <ConnectionStatus />
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