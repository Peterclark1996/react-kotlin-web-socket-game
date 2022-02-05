import { useHistory } from 'react-router-dom'
import { useWebSocket } from '../Contexts/WebSocketContext'
import { ROUTE_HOME } from '../helpers'

const GameButtons = ({ hasGameStarted }) => {
    const { send } = useWebSocket()
    const history = useHistory()

    const onStartGameClicked = () => {
        send("InboundUserStartedGame", {})
    }

    const onLeaveGameClicked = () => {
        history.push(`${ROUTE_HOME}`)
    }

    return(
        <div className="d-flex flex-column align-items-center">
            {
                !hasGameStarted &&
                <div className="btn btn-success m-2 text-white px-3 py-2" onClick={onStartGameClicked}>
                    <h4 className="mb-0">Start Game</h4>
                </div>
            }
            <div className="btn btn-success m-2 text-white px-3 py-2" onClick={onLeaveGameClicked}>
                <h4 className="mb-0">Leave Game</h4>
            </div>
        </div>
    )
}

export default GameButtons