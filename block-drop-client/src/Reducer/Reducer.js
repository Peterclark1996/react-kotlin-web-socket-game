import ActionTypes from "./ActionTypes"

const Reducer = (state, action) => {
    switch (action.type) {
        case ActionTypes.USERNAME_UPDATED:
            return {
                ...state,
                username: action.updatedUsername
            }
        case ActionTypes.PLAYER_SCORES_UPDATED:
            return {
                ...state,
                playerScores: action.updatedPlayerScores
            }
        default:
            throw new Error(`Reducer action invalid: ${action.type}`)
    }
}

export default Reducer