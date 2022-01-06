import Game from './Panels/Game'
import Controls from './Panels/Controls'
import Room from './Panels/Room'
import { useState } from 'react/cjs/react.development'
import { useWebSocket } from './Contexts/WebSocketContext'
import { useEffect } from 'react'
import { generateHtmlId } from './helpers'
import Overlay from './Components/Overlay'

const joinSuccessEventId = generateHtmlId()
const joinFailEventId = generateHtmlId()

const MainWindow = () => {
    const { on, send } = useWebSocket()

    const [username, setUsername] = useState("")
    const [roomCodeInput, setRoomCodeInput] = useState("")
    const [usernameInput, setUsernameInput] = useState("")

    const onJoinGameClicked = () => {
        send("InboundUserJoinedRoom", {
            room: roomCodeInput,
            username: usernameInput
        })
    }

    useEffect(() => {
        on(
            "OutboundUserJoinedRoomSuccess", 
            eventData => setUsername(eventData.username),
            joinSuccessEventId
        )
        on(
            "OutboundUserJoinedRoomFailure", 
            eventData => alert(eventData.message),
            joinFailEventId
        )
    }, [on])

    return (
        <div className="App d-flex justify-content-center align-items-center bg-dark">
            {
                username === "" && 
                    <Overlay>
                        <div className="d-flex flex-column p-3 align-items-center">
                            <span className="mb-2">Block Drop or wtv game</span>
                            <div className="border my-2 w-100" />
                            <div className="d-flex justify-content-end ms-auto mb-2">
                                <span className="me-2">Code:</span>
                                <input value={roomCodeInput} onChange={event => setRoomCodeInput(event.target.value)}/>
                            </div>
                            <div className="d-flex justify-content-end ms-auto mb-2">
                                <span className="me-2">Name:</span>
                                <input value={usernameInput} onChange={event => setUsernameInput(event.target.value)}/>
                            </div>
                            <div className="btn border" onClick={onJoinGameClicked}>Join Game</div>
                            <div className="border my-2 w-100" />
                            <div className="btn border" disabled>Create New Game</div>
                        </div>
                    </Overlay>
            }
            <div className="SidePanel">
                <Room />
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