import ActionTypes from "./ActionTypes"

export const defaultState = {
    username: "",
    connectedPlayers: [],
    playerState: []
}

const Reducer = (state, action) => {
    switch (action.type) {
        case ActionTypes.USERNAME_UPDATED:
            return {
                ...state,
                username: action.updatedUsername
            }
        case ActionTypes.CONNECTED_PLAYERS_UPDATED:
            return {
                ...state,
                connectedPlayers: action.updatedConnectedPlayers
            }
        case ActionTypes.PLAYER_STATE_UPDATED:
            return {
                ...state,
                playerState: action.updatedPlayerState
            }
        case ActionTypes.RESET_STATE:
            return defaultState
        default:
            throw new Error(`Reducer action invalid: ${action.type}`)
    }
}

export default Reducer