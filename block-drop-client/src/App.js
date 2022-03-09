import './App.css'
import Room from './Pages/Room'
import Home from './Pages/Home'
import * as WebSocketContext from './Contexts/WebSocketContext'
import { BrowserRouter, Switch, Route, Redirect } from "react-router-dom"
import { ROUTE_HOME, ROUTE_ROOM } from './helpers'
import Reducer from './Reducer/Reducer'
import { useReducer } from 'react'

const App = () => {
    const [state, dispatch] = useReducer(
        Reducer,
        {
            username: "",
            playerState: []
        }
    )

    return (
        <WebSocketContext.WebSocketProvider>
            <BrowserRouter>
                <Switch>
                    <Route 
                        exact 
                        strict 
                        path={ROUTE_HOME} 
                        children={<Home dispatch={dispatch} />} 
                    />
                    <Route 
                        exact 
                        strict 
                        path={`${ROUTE_ROOM}/:roomCode`} 
                        children={<Room state={state} dispatch={dispatch} />} 
                    />
                    <Route>
                        <Redirect to={ROUTE_HOME} />
                    </Route>
                </Switch>
            </BrowserRouter>
        </WebSocketContext.WebSocketProvider>
    )
}

export default App