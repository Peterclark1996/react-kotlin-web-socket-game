const Block = ({ colour }) => {
    if(typeof colour !== "string"){
        throw new Error("Block must have colour prop passed in")
    }

    return(
        <div className="p-1" style={{"backgroundColor": colour}}>
            <div className="bg-light p-1">
                <div className="p-2" style={{"backgroundColor": colour}}></div>
            </div>
        </div>
    )
}

export default Block