import Game from './Panels/Game'
import Controls from './Panels/Controls'
import Info from './Panels/Info'
import { useState } from 'react'
import { useWebSocket } from './Contexts/WebSocketContext'
import { useUsername } from './Contexts/UsernameContext'
import { useEffect } from 'react'
import { generateHtmlId } from './helpers'
import Overlay from './Components/Overlay'

const joinSuccessEventId = generateHtmlId()
const joinFailEventId = generateHtmlId()
const gameStartedEventId = generateHtmlId()

const Room = ({ match }) => {
    const { on, send } = useWebSocket()
    const { username, setUsername } = useUsername()

    const [hasGameStarted, setHasGameStarted] = useState(false)

    const [usernameInput, setUsernameInput] = useState("")

    const onJoinGameClicked = () => {
        send("InboundUserTriedToJoinRoom", {
            room: match.params.roomCode,
            username: usernameInput
        })
    }

    const onStartGameClicked = () => {
        send("InboundUserStartedGame", {})
    }

    useEffect(() => {
        on(
            "OutboundUserTriedToJoinRoomSuccess", 
            eventData => setUsername(eventData.username),
            joinSuccessEventId
        )
        on(
            "OutboundUserTriedToJoinRoomFailure", 
            eventData => alert(eventData.message),
            joinFailEventId
        )
        on(
            "OutboundGameStarted",
            _ => setHasGameStarted(true),
            gameStartedEventId
        )
    }, [on, setUsername])

    return (
        <div className="App d-flex justify-content-center align-items-center bg-dark">
            {
                username === "" && 
                    <Overlay>
                        <div className="d-flex flex-column p-3 align-items-center">
                            <span className="mb-2">Joining game '{match.params.roomCode}'</span>
                            <div className="border my-2 w-100" />
                            <div className="d-flex justify-content-end ms-auto mb-2">
                                <span className="me-2">Your name:</span>
                                <input value={usernameInput} onChange={event => setUsernameInput(event.target.value)}/>
                            </div>
                            <div className="btn border" onClick={onJoinGameClicked}>Join Game</div>
                        </div>
                    </Overlay>
            }
            <div className="SidePanel">
                <Info hasGameStarted={hasGameStarted} startGame={onStartGameClicked}/>
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

export default Room