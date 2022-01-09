import './App.css'
import Room from './Room'
import Home from './Home'
import * as WebSocketContext from './Contexts/WebSocketContext'
import * as UsernameContext from './Contexts/UsernameContext'
import { BrowserRouter, Switch, Route, Redirect } from "react-router-dom"
import { ROUTE_HOME, ROUTE_ROOM } from './helpers'

const App = () => {
    return (
        <WebSocketContext.WebSocketProvider>
            <UsernameContext.UsernameProvider>
                <BrowserRouter>
                    <Switch>
                        <Route exact strict path={ROUTE_HOME} component={Home} />
                        <Route exact strict path={`${ROUTE_ROOM}/:roomCode`} component={Room} />
                        <Route>
                            <Redirect to={ROUTE_HOME} />
                        </Route>
                    </Switch>
                </BrowserRouter>
            </UsernameContext.UsernameProvider>
        </WebSocketContext.WebSocketProvider>
    )
}

export default App