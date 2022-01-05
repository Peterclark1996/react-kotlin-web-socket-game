import { useEffect, useState } from 'react'
import { useWebSocket } from '../Contexts/WebSocketContext'
import { generateHtmlId } from '../helpers'

const roomUsersUpdatedEventId = generateHtmlId()

const Room = ({ username }) => {
    const { connectionState, on, send } = useWebSocket()

    const [players, setPlayers] = useState([])

    const joinRoom = () => {
        if(username === ""){
            const desiredUsername = window.prompt("Name")
            send("InboundUserJoinedRoom", {
                room: "testRoom",
                username: desiredUsername
            })
        }
    }

    const getStatus = () => {
        if(connectionState === 0) return <span className="text-warning">Connecting</span>
        if(connectionState === 1) return <span className="text-success">Connected</span>
        return <span className="text-danger">Disconnected</span>
    }

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
            {username === "" && <button onClick={joinRoom}>Join room</button>}
        </div>
    )
}

export default Room