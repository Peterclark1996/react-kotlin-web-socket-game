import Block from "../Components/Block"
import { useWebSocket } from '../Contexts/WebSocketContext'
import { useEffect, useState } from 'react'
import { generateHtmlId } from '../helpers'
import ActionTypes from "../Reducer/ActionTypes"
import GameSide from "../Components/GameSide"

const GameStateUpdatedEventId = generateHtmlId()

const blockColours = [
    "#FFFFFF",
    "#FFA9A9",
    "#A9BAFF",
    "#A9FFBA",
    "#F1FFA9",
    "#EFA9FF",
    "#6D43A9",
    "#C69437"   
]

const Game = ({ dispatch }) => {
    const { on } = useWebSocket()

    const [tiles, setTiles] = useState([[]])

    useEffect(() => {
        on(
            "OutboundGameStateUpdated", 
            eventData => {
                dispatch({ type: ActionTypes.PLAYER_STATE_UPDATED, updatedPlayerState: eventData.players })
                setTiles(eventData.tiles)
            },
            GameStateUpdatedEventId
        )
    }, [on, dispatch])

    if(tiles.length === 0 || tiles[0].length === 0){
        return <div></div>
    }

    return(
        <div className="d-flex justify-content-center">
            <GameSide size={tiles.length + 2} isColumn={true} />
            <div className="d-flex flex-column">
                <GameSide size={tiles[0].length} />
                {
                    tiles.map((row, rowIndex) => 
                        <div key={`row-${rowIndex}`} className="d-flex">
                            {
                                row.map((cell, cellIndex) => {
                                    if(cell === -1){
                                        return <Block key={`cell-${rowIndex}-${cellIndex}`} colour="#B7B7B7" />
                                    }
                                    return <Block key={`cell-${rowIndex}-${cellIndex}`} colour={blockColours[cell]} />
                                })
                            }
                        </div>
                    )
                }
                <GameSide size={tiles[0].length} />
            </div>
            <GameSide size={tiles.length + 2} isColumn={true} />
        </div>
    )
}

export default Game