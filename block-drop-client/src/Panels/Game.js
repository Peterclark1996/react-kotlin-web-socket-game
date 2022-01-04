import Block from "../Components/Block"

const Game = () => {
    const width = 10
    const height = 18
    return(
        <div className="d-flex flex-column justify-content-center align-items-center">
            {
                [...Array(height)].map((_, rowIndex) => 
                    <div key={`row-${rowIndex}`} className="d-flex">
                        {
                            [...Array(width)].map((_, cellIndex) => <Block key={`cell--${rowIndex}-${cellIndex}`} colour={"#ae0000"} />)
                        }
                    </div>
                )
            }
        </div>
    )
}

export default Game