import ConnectionStatus from "../Components/ConnectionStatus"
import LineBreak from "../Components/LineBreak"
import Controls from "./Controls"
import GameButtons from "./GameButtons"
import PlayerList from "./PlayerList"

const LeftPanel = ({ state, dispatch, hasGameStarted }) => {
    return (
        <div className="d-flex flex-column align-items-center p-3">
            <ConnectionStatus />
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