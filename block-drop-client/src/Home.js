import { useHistory } from 'react-router-dom'
import { useState, useEffect } from 'react'
import Overlay from './Components/Overlay'
import { useWebSocket } from './Contexts/WebSocketContext'
import { useUsername } from './Contexts/UsernameContext'
import { generateHtmlId, ROUTE_ROOM } from './helpers'

const joinSuccessEventId = generateHtmlId()
const joinFailEventId = generateHtmlId()
const createSuccessEventId = generateHtmlId()
const createFailEventId = generateHtmlId()

const Home = () => {
    const { on, send } = useWebSocket()
    const { setUsername } = useUsername()
    const history = useHistory()

    const [roomCodeInput, setRoomCodeInput] = useState("")
    const [usernameInput, setUsernameInput] = useState("")

    const onJoinGameClicked = () => {
        send("InboundUserTriedToJoinRoom", {
            room: roomCodeInput,
            username: usernameInput
        })
    }

    const onCreateGameClicked = () => {
        send("InboundUserTriedToCreateRoom", {
            username: usernameInput
        })
    }

    useEffect(() => {
        on(
            "OutboundUserJoinedRoomSuccess", 
            eventData => {
                setUsername(eventData.username)
                history.push(`${ROUTE_ROOM}/${eventData.room}`)
            },
            joinSuccessEventId
        )
        on(
            "OutboundUserTriedToCreateRoomFailure", 
            eventData => alert(eventData.message),
            joinFailEventId
        )
        on(
            "OutboundUserTriedToCreateRoomSuccess", 
            eventData => {
                setUsername(eventData.username)
                history.push(`${ROUTE_ROOM}/${eventData.room}`)
            },
            createSuccessEventId
        )
        on(
            "OutboundUserJoinedRoomFailure", 
            eventData => alert(eventData.message),
            createFailEventId
        )
    }, [history, on, setUsername])

    return(
        <div className="App d-flex justify-content-center align-items-center bg-dark">
            <Overlay>
                <div className="d-flex flex-column p-3 align-items-center">
                    <span className="mb-2">Block Drop</span>
                    <div className="d-flex mb-2">
                        <span className="me-2">Your name:</span>
                        <input value={usernameInput} onChange={event => setUsernameInput(event.target.value)}/>
                    </div>
                    <div className="border my-2 w-100" />
                    <div className="d-flex mb-2">
                        <span className="me-2">Code:</span>
                        <input value={roomCodeInput} onChange={event => setRoomCodeInput(event.target.value)}/>
                        <div className="ms-2 btn border" onClick={onJoinGameClicked}>Join Game</div>
                    </div>
                    <div className="border my-2 w-100" />
                    <div className="btn border" onClick={onCreateGameClicked}>Create New Game</div>
                </div>
            </Overlay>
        </div>
    )
}

export default Home