import ConnectionStatus from "../Components/ConnectionStatus"
import LineBreak from "../Components/LineBreak"
import Controls from "./Controls"
import GameButtons from "./GameButtons"
import PlayerList from "./PlayerList"

const LeftPanel = ({ playerState, hasGameStarted }) => {
    return (
        <div className="d-flex flex-column align-items-center p-3">
            <ConnectionStatus />
            <LineBreak />
            <PlayerList playerState={playerState} />
            <LineBreak />
            <Controls />
            <LineBreak />
            <GameButtons hasGameStarted={hasGameStarted} />
        </div>
    )
}

export default LeftPanel