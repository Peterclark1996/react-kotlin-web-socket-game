import Game from './Panels/Game'
import Controls from './Panels/Controls'
import Room from './Panels/Room'
import { useState } from 'react/cjs/react.development'
import { useWebSocket } from './Contexts/WebSocketContext'
import { useEffect } from 'react'

const MainWindow = () => {
    const { on } = useWebSocket()

    const [username, setUsername] = useState("")

    useEffect(() => {
        on("OUTBOUND_USER_JOINED_ROOM_RESULT", eventData => {
            if(eventData.success){
                setUsername(eventData.username)
            }
        })
    }, [on])

    return (
        <div className="App d-flex justify-content-center align-items-center bg-dark">
            <div className="SidePanel">
                <Room username={username}/>
            </div>
            <div className="MiddlePanel">
                <Game />
            </div>
            <div className="SidePanel">
                <Controls />
            </div>
        </div>
    )
}

export default MainWindow