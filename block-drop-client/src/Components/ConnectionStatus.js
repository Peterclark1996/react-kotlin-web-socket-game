import { useWebSocket } from "../Contexts/WebSocketContext"

const ConnectionStatus = () => {
    const { connectionState } = useWebSocket()

    const getStatus = () => {
        if(connectionState === 0) return <span className="text-warning">Connecting</span>
        if(connectionState === 1) return <span className="text-success">Connected</span>
        return <span className="text-danger">Disconnected</span>
    }

    return(
        <div className="d-flex">
            <span className="me-2">Status:</span>
            {getStatus()}
        </div>
    )
}

export default ConnectionStatus