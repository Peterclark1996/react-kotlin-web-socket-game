import Block from "../Components/Block"
import { useWebSocket } from '../Contexts/WebSocketContext'
import { useEffect, useState } from 'react'
import { generateHtmlId } from '../helpers'

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

const Game = () => {
    const { on } = useWebSocket()

    const [tiles, setTiles] = useState([[]])

    useEffect(() => {
        on(
            "OutboundGameStateUpdated", 
            eventData => setTiles(eventData.tiles),
            GameStateUpdatedEventId
        )
    }, [on])

    return(
        <div className="d-flex flex-column justify-content-center align-items-center">
            {
                tiles.map((row, rowIndex) => 
                    <div key={`row-${rowIndex}`} className="d-flex">
                        {
                            row.map((cell, cellIndex) => {
                                if(cell === -1){
                                    return <Block key={`cell--${rowIndex}-${cellIndex}`} colour={"#000000"} />
                                }
                                return <Block key={`cell--${rowIndex}-${cellIndex}`} colour={blockColours[cell]} />
                            })
                        }
                    </div>
                )
            }
        </div>
    )
}

export default Game