import Game from '../Panels/Game'
import { useState } from 'react'
import { useWebSocket } from '../Contexts/WebSocketContext'
import { useEffect } from 'react'
import { generateHtmlId, ROUTE_HOME } from '../helpers'
import Overlay from '../Components/Overlay'
import ActionTypes from '../Reducer/ActionTypes'
import { useHistory, useParams } from 'react-router-dom'
import ConnectionStatus from '../Components/ConnectionStatus'
import LineBreak from '../Components/LineBreak'
import LeftPanel from '../Panels/LeftPanel'

const roomUsersUpdatedEventId = generateHtmlId()
const joinSuccessEventId = generateHtmlId()
const joinFailEventId = generateHtmlId()
const gameStartedEventId = generateHtmlId()

const Room = ({ state, dispatch }) => {
    const { on, send } = useWebSocket()
    const { roomCode } = useParams()
    const history = useHistory()

    const [hasGameStarted, setHasGameStarted] = useState(false)
    const [usernameInput, setUsernameInput] = useState("")
    const [errorMessage, setErrorMessage] = useState("")

    const onBackButtonClicked = () => {
        history.push(`${ROUTE_HOME}`)
    }

    const onJoinGameClicked = () => {
        send("InboundUserTriedToJoinRoom", {
            room: roomCode,
            username: usernameInput
        })
    }

    useEffect(() => {
        on(
            "OutboundRoomUsersUpdated", 
            eventData => dispatch({ type: ActionTypes.CONNECTED_PLAYERS_UPDATED, updatedConnectedPlayers: eventData.usernames }), 
            roomUsersUpdatedEventId
        )
        on(
            "OutboundUserTriedToJoinRoomSuccess", 
            eventData => dispatch({ type: ActionTypes.USERNAME_UPDATED, updatedUsername: eventData.username }),
            joinSuccessEventId
        )
        on(
            "OutboundUserTriedToJoinRoomFailure", 
            eventData => setErrorMessage(eventData.message),
            joinFailEventId
        )
        on(
            "OutboundGameStarted",
            _ => setHasGameStarted(true),
            gameStartedEventId
        )
    }, [dispatch, on])

    return (
        <div className="App d-flex justify-content-center align-items-center bg-dark">
            {
                state.username === "" ?
                    <Overlay>
                        <div className="d-flex flex-column p-3 align-items-center">
                            <div className="d-flex w-100 mb-2">
                                <div className="btn border me-5" onClick={onBackButtonClicked}>Back</div>
                                <span>Joining game '{roomCode}'</span>
                            </div>
                            <LineBreak />
                            <div className="d-flex justify-content-end ms-auto mb-2">
                                <span className="me-2">Your name:</span>
                                <input value={usernameInput} onChange={event => setUsernameInput(event.target.value)}/>
                            </div>
                            <div className="btn border" onClick={onJoinGameClicked}>Join Game</div>
                            {errorMessage !== "" && <span className="bg-danger rounded px-2 mt-2">{errorMessage}</span>}
                            <LineBreak />
                            <ConnectionStatus />
                        </div>
                    </Overlay> :
                    <div className="d-flex w-100">
                        <div className="SidePanel">
                            <LeftPanel state={state} dispatch={dispatch} hasGameStarted={hasGameStarted} />
                        </div>
                        <div className="MainPanel">
                            <Game dispatch={dispatch} />
                        </div>
                    </div>
            }
        </div>
    )
}

export default Room