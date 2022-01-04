import './App.css'
import MainWindow from './MainWindow'
import * as WebSocket from './Contexts/WebSocketContext'

const App = () => {
    return (
        <WebSocket.WebSocketProvider>
            <MainWindow />
        </WebSocket.WebSocketProvider>
    )
}

export default App