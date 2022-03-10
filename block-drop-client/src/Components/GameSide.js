import Block from "./Block"

const GameSide = ({ size, isColumn = false }) => {
    if(typeof size !== "number"){
        throw new Error("GameSide must have size prop passed in")
    }

    return(
        <div className={`d-flex ${isColumn ? "flex-column": ""}`}>
        {
            [...Array(size)].map((_, index) => <Block key={`top-${index}`} colour="#B7B7B7" />)
        }
    </div>
    )
}

export default GameSide