import './App.css'
import Room from './Room'
import Home from './Home'
import * as WebSocket from './Contexts/WebSocketContext'
import { BrowserRouter, Switch, Route, Redirect } from "react-router-dom"
import { ROUTE_HOME, ROUTE_ROOM } from './helpers'

const App = () => {
    return (
        <WebSocket.WebSocketProvider>
            <BrowserRouter>
                <Switch>
                    <Route exact strict path={ROUTE_HOME} component={Home} />
                    <Route exact strict path={`${ROUTE_ROOM}/:roomCode`} component={Room} />
                    <Route>
                        <Redirect to={ROUTE_HOME} />
                    </Route>
                </Switch>
            </BrowserRouter>
        </WebSocket.WebSocketProvider>
    )
}

export default App