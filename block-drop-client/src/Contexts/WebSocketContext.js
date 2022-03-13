import { useState, useEffect, useCallback, createContext, useContext } from 'react'

const getSocketUrl = () => {
    if (!process.env.NODE_ENV || process.env.NODE_ENV === 'development') {
        return "ws://localhost:8080/room"
    }
    return `wss://${window.location.host}/room`
}

const limitedRetries = false
const maxRetries = 5

const WebSocketContext = createContext()

export const ConnectionStateType = {
    CONNECTING: "CONNECTING",
    OPEN: "OPEN",
    CLOSING: "CLOSING",
    CLOSED: "CLOSED"
}

export const WebSocketProvider = props => {
    const [connection, setConnection] = useState()
    const [eventListeners, setEventListeners] = useState([])

    const [connectionState, setConnectionState] = useState(ConnectionStateType.CLOSED)
    const [hasFailedToConnect, setHasFailedToConnect] = useState(false)

    const onEventReceived = useCallback(event => {
        const eventData = JSON.parse(event.data)
        eventListeners
            .filter(el => el.eventType === eventData.type)
            .forEach(el => el.func(JSON.parse(eventData.jsonData)))
    }, [eventListeners])

    const addListenerToSocket = useCallback((eventType, func, key) => {
        if(typeof key !== "string") throw new Error(`Key must be provided when adding a socket listener for event type '${eventType}'`)
        if(eventListeners.filter(el => el.key === key).length > 0) return

        setEventListeners([
            ...eventListeners,
            {
                eventType,
                func,
                key
            }
        ])
    }, [eventListeners])

    const sendToSocket = useCallback((eventType, eventData) => {
            if(connectionState !== ConnectionStateType.OPEN) return
            
            connection.send(
                JSON.stringify(
                    {
                        type: eventType,
                        jsonData: JSON.stringify(eventData)
                    }
                )
            )
        }, [connection, connectionState])

    const disconnectFromSocket = () => {
        
    }

    const connectToSocket = useCallback((socketUrl, retriesRemaining = maxRetries) => {
        if(hasFailedToConnect) return

        if(limitedRetries && retriesRemaining === 0){
            setConnection()
            setConnectionState(ConnectionStateType.CLOSED)
            setHasFailedToConnect(true)
            return
        }
        
        if(connection !== undefined && (connection.readyState === 0 || connection.readyState === 1)){
            connection.onmessage = onEventReceived
            return
        }

        if(connectionState === ConnectionStateType.CONNECTING || connectionState === ConnectionStateType.OPEN) return

        console.log("Socket connecting to:", socketUrl)
        setConnectionState(ConnectionStateType.CONNECTING)

        const reconnectToSocketOnClose = (reason, retries = maxRetries) => {

            setConnectionState(ConnectionStateType.CONNECTING)
 
            if(limitedRetries && retries === 1){
                setConnection()
                setConnectionState(ConnectionStateType.CLOSED)
                setHasFailedToConnect(true)
                return
            }

            const nextAmountOfRetries = retries - 1
            const retryCooldownText = ` ${nextAmountOfRetries} retr${nextAmountOfRetries === 1 ? "y" : "ies"} remaining.`
            console.log(`Socket closed. Reconnect will be attempted in 3 seconds.${limitedRetries ? retryCooldownText : ""}`, reason)
            setTimeout(() => {
                connectToSocket(socketUrl, nextAmountOfRetries)
            }, 3000)
        }

        const newConnection = new WebSocket(socketUrl)
        
        newConnection.onopen = () => {
            console.log("Socket connected")
            setConnection(newConnection)
            setConnectionState(ConnectionStateType.OPEN)
            setHasFailedToConnect(false)
            newConnection.onclose = event => reconnectToSocketOnClose(event.reason)
        }

        newConnection.onmessage = onEventReceived

        newConnection.onclose = event => reconnectToSocketOnClose(event.reason, retriesRemaining)

        newConnection.onerror = error => {
            console.error('Socket error: ', error.message)
            newConnection.close()
        }
    }, [connection, connectionState, hasFailedToConnect, onEventReceived])

    useEffect(() => connectToSocket(getSocketUrl()), [connectToSocket])

    const value = {
        connectionState,
        connect: connectToSocket,
        disconnect: disconnectFromSocket,
        on: addListenerToSocket,
        send: sendToSocket
    }

    return <WebSocketContext.Provider value={value} {...props} />
}

export const useWebSocket = () => useContext(WebSocketContext)