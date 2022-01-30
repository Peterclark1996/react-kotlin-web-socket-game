import { useState, useEffect, useCallback, createContext, useContext } from 'react'

const socketUrl = "ws://localhost:8080/room"
const limitedRetries = false
const maxRetries = 5

const WebSocketContext = createContext()

export const WebSocketProvider = props => {
    const [connection, setConnection] = useState()
    const [eventListeners, setEventListeners] = useState([])

    // 0	CONNECTING
    // 1	OPEN
    // 2	CLOSING
    // 3	CLOSED
    const [connectionState, setConnectionState] = useState(3)
    const [hasFailedToConnect, setHasFailedToConnect] = useState(false)

    const onEventReceived = useCallback(event => {
        const eventData = JSON.parse(event.data)
        console.log(eventData)
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

    const sendToSocket = (eventType, eventData) => {
        if(connectionState === 1){
            connection.send(
                JSON.stringify(
                    {
                        type: eventType,
                        jsonData: JSON.stringify(eventData)
                    }
                )
            )
        }
    }

    const disconnectFromSocket = () => {
        
    }

    const connectToSocket = useCallback((socketUrl, retriesRemaining = maxRetries) => {
        if(hasFailedToConnect) return

        if(limitedRetries && retriesRemaining === 0){
            setConnection()
            setConnectionState(3)
            setHasFailedToConnect(true)
            return
        }
        
        if(connection !== undefined && (connection.readyState === 0 || connection.readyState === 1)){
            connection.onmessage = onEventReceived
            return
        }

        if(connectionState === 0 || connectionState === 1) return

        console.log("Socket connecting to:", socketUrl)
        setConnectionState(0)

        const reconnectToSocketOnClose = (reason, retries = maxRetries) => {

            setConnectionState(0)
 
            if(limitedRetries && retries === 1){
                setConnection()
                setConnectionState(3)
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
            setConnectionState(1)
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

    useEffect(() => connectToSocket(socketUrl), [connectToSocket])

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