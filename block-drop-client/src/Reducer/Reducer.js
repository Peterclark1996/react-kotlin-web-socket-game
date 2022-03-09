import ActionTypes from "./ActionTypes"

const Reducer = (state, action) => {
    switch (action.type) {
        case ActionTypes.USERNAME_UPDATED:
            return {
                ...state,
                username: action.updatedUsername
            }
        case ActionTypes.PLAYER_STATE_UPDATED:
            return {
                ...state,
                playerState: action.updatedPlayerState
            }
        default:
            throw new Error(`Reducer action invalid: ${action.type}`)
    }
}

export default Reducer