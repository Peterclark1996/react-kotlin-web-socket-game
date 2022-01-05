import Game from './Panels/Game'
import Controls from './Panels/Controls'
import Room from './Panels/Room'
import { useState } from 'react/cjs/react.development'
import { useWebSocket } from './Contexts/WebSocketContext'
import { useEffect } from 'react'
import { generateHtmlId } from './helpers'

const joinSuccessEventId = generateHtmlId()

const MainWindow = () => {
    const { on } = useWebSocket()

    const [username, setUsername] = useState("")

    useEffect(() => {
        on(
            "OutboundUserJoinedRoomResult", 
            eventData => {
                if(eventData.success){
                    setUsername(eventData.username)
                }
            },
            joinSuccessEventId
        )
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