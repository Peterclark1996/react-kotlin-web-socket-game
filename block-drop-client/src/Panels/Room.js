import { useEffect, useState } from 'react'
import { useWebSocket } from '../Contexts/WebSocketContext'
import { generateHtmlId } from '../helpers'

const roomUsersUpdatedEventId = generateHtmlId()

const Room = ({ username }) => {
    const { connection, on } = useWebSocket()

    const [players, setPlayers] = useState([])

    const joinRoom = () => {
        if(username === ""){
            const desiredUsername = window.prompt("Name")
            console.log("NAME", desiredUsername)
            connection.send(
                JSON.stringify(
                    {
                        type: "INBOUND_USER_JOINED_ROOM",
                        jsonData: JSON.stringify({
                            roomId: "testRoom",
                            username: desiredUsername
                        })
                    }
                )
            )
        }
    }

    const getStatus = () => {
        if(connection.readyState === 0) return <span className="text-warning">Connecting</span>
        if(connection.readyState === 1) return <span className="text-success">Connected</span>
        return <span className="text-danger">Disconnected</span>
    }

    useEffect(() => {
        on(
            "OUTBOUND_ROOM_USERS_UPDATED", 
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