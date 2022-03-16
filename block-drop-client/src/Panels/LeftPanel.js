import ConnectionStatus from "../Components/ConnectionStatus"
import LineBreak from "../Components/LineBreak"
import Controls from "./Controls"
import GameButtons from "./GameButtons"
import PlayerList from "./PlayerList"

const LeftPanel = ({ state, dispatch, roomCode, hasGameStarted }) => {
    return (
        <div className="d-flex flex-column align-items-center p-3">
            <ConnectionStatus />
            <div>
                Room Code:
                <span className="ms-2 p-1 user-select-all Rounded bg-secondary">{roomCode}</span>
            </div>
            <LineBreak />
            <PlayerList connectedPlayers={state.connectedPlayers} playerState={state.playerState} />
            <LineBreak />
            <Controls />
            <LineBreak />
            <GameButtons hasGameStarted={hasGameStarted} dispatch={dispatch} />
        </div>
    )
}

export default LeftPanel