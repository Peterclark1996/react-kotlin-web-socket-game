import Game from './Panels/Game'
import Controls from './Panels/Controls'
import Room from './Panels/Room'
import { useState } from 'react/cjs/react.development'
import { useWebSocket } from './Contexts/WebSocketContext'
import { useEffect } from 'react'
import { generateHtmlId } from './helpers'
import Overlay from './Components/Overlay'

const joinSuccessEventId = generateHtmlId()

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
            {
                username === "" && 
                    <Overlay>
                        <div className="d-flex flex-column p-3">
                            <span className="mb-2">Block Drop or wtv game</span>
                            <div className="d-flex justify-content-end mb-2">
                                <span className="me-2">Code:</span>
                                <input value={roomCodeInput} onChange={event => setRoomCodeInput(event.target.value)}/>
                            </div>
                            <div className="d-flex justify-content-end mb-2">
                                <span className="me-2">Name:</span>
                                <input value={usernameInput} onChange={event => setUsernameInput(event.target.value)}/>
                            </div>
                            <button onClick={onJoinGameClicked}>Join Game</button>
                            <hr />
                            <button disabled>Create Game</button>
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