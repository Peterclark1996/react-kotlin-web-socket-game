import { useState, useEffect, useCallback, createContext, useContext } from 'react'

const socketUrl = "ws://localhost:8080/room"

const WebSocketContect = createContext()

export const WebSocketProvider = props => {
    const [connection, setConnection] = useState({readyState: 3})
    const [connected, setConnected] = useState(false)
    const [eventListeners, setEventListeners] = useState([])

    const onEventReceived = useCallback(event => {
        const eventData = JSON.parse(event.data)
        eventListeners
            .filter(el => el.eventType === eventData.type)
            .forEach(el => el.func(JSON.parse(eventData.jsonData)))
    }, [eventListeners])

    const connectToSocket = useCallback((socketUrl, retriesRemaining = 4) => {
        if(retriesRemaining === 0) return
        if(connection.readyState === 0 || connection.readyState === 1){
            connection.onmessage = onEventReceived
            return
        }

        console.log("Socket connecting to:", socketUrl)

        const reconnectToSocketOnClose = (reason, retries = 4) => {
            if(retries === 1){
                setConnection({readyState: 3})
                return
            }
            const nextAmountOfRetries = retries - 1
            console.log(`Socket closed. Reconnect will be attempted in 5 seconds. ${nextAmountOfRetries} retr${nextAmountOfRetries === 1 ? "y" : "ies"} remaining.`, reason)
            setTimeout(() => {
                connectToSocket(socketUrl, nextAmountOfRetries)
            }, 5000)
        }

        const newConnection = new WebSocket(socketUrl)
        setConnection(newConnection)

        newConnection.onopen = () => {
            console.log("Socket connected")
            setConnected(true)
            newConnection.onclose = event => reconnectToSocketOnClose(event.reason)
        }

        connection.onmessage = onEventReceived

        newConnection.onclose = event => reconnectToSocketOnClose(event.reason, retriesRemaining)

        newConnection.onerror = error => {
            console.error('Socket error: ', error.message)
            newConnection.close()
        }
    }, [connection, onEventReceived])

    const disconnectFromSocket = () => {
        
    }

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

    useEffect(() => connectToSocket(socketUrl), [connectToSocket])

    const value = {
        connection, 
        connected,
        connect: connectToSocket,
        disconnect: disconnectFromSocket,
        on: addListenerToSocket
    }

    return <WebSocketContect.Provider value={value} {...props} />
}

export const useWebSocket = () => useContext(WebSocketContect)