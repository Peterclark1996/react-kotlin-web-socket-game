import { useHistory } from 'react-router-dom'
import { useState, useEffect } from 'react'
import Overlay from '../Components/Overlay'
import { useWebSocket } from '../Contexts/WebSocketContext'
import { generateHtmlId, ROUTE_ROOM } from '../helpers'
import ActionTypes from '../Reducer/ActionTypes'
import ConnectionStatus from '../Components/ConnectionStatus'
import LineBreak from '../Components/LineBreak'

const joinSuccessEventId = generateHtmlId()
const joinFailEventId = generateHtmlId()
const createSuccessEventId = generateHtmlId()
const createFailEventId = generateHtmlId()

const Home = ({ dispatch }) => {
    const { on, send } = useWebSocket()
    const history = useHistory()

    const [roomCodeInput, setRoomCodeInput] = useState("")
    const [usernameInput, setUsernameInput] = useState("")
    const [errorMessage, setErrorMessage] = useState("")

    const onJoinGameClicked = () => {
        dispatch({ type: ActionTypes.RESET_STATE })
        send("InboundUserTriedToJoinRoom", {
            room: roomCodeInput,
            username: usernameInput
        })
    }

    const onCreateGameClicked = () => {
        dispatch({ type: ActionTypes.RESET_STATE })
        send("InboundUserTriedToCreateRoom", {
            username: usernameInput
        })
    }

    useEffect(() => {
        on(
            "OutboundUserTriedToJoinRoomSuccess", 
            eventData => {
                dispatch({ type: ActionTypes.USERNAME_UPDATED, updatedUsername: eventData.username })
                history.push(`${ROUTE_ROOM}/${eventData.room}`)
            },
            joinSuccessEventId
        )
        on(
            "OutboundUserTriedToCreateRoomFailure", 
            eventData => setErrorMessage(eventData.message),
            joinFailEventId
        )
        on(
            "OutboundUserTriedToCreateRoomSuccess", 
            eventData => {
                dispatch({ type: ActionTypes.USERNAME_UPDATED, updatedUsername: eventData.username })
                history.push(`${ROUTE_ROOM}/${eventData.room}`)
            },
            createSuccessEventId
        )
        on(
            "OutboundUserJoinedRoomFailure", 
            eventData => setErrorMessage(eventData.message),
            createFailEventId
        )
    }, [dispatch, history, on])

    return(
        <div className="App d-flex justify-content-center align-items-center bg-dark">
            <Overlay>
                <div className="d-flex flex-column p-3 align-items-center">
                    <span className="mb-2">Block Drop</span>
                    <div className="d-flex mb-2">
                        <span className="me-2">Your name:</span>
                        <input value={usernameInput} onChange={event => setUsernameInput(event.target.value)}/>
                    </div>
                    <LineBreak />
                    <div className="d-flex mb-2">
                        <span className="me-2">Code:</span>
                        <input value={roomCodeInput} onChange={event => setRoomCodeInput(event.target.value)}/>
                        <div className="ms-2 btn border" onClick={onJoinGameClicked}>Join Game</div>
                    </div>
                    <div className="mb-2">
                        OR
                    </div>
                    <div className="btn border" onClick={onCreateGameClicked}>Create New Game</div>
                    {errorMessage !== "" && <span className="bg-danger rounded px-2 mt-2">{errorMessage}</span>}
                    <LineBreak />
                    <ConnectionStatus />
                </div>
            </Overlay>
        </div>
    )
}

export default Home