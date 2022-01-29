import ActionTypes from "./ActionTypes"

const Reducer = (state, action) => {
    switch (action.type) {
        case ActionTypes.USERNAME_UPDATED:
            return {
                ...state,
                username: action.updatedUsername
            }
        default:
            throw new Error(`Reducer action invalid: ${action.type}`)
    }
}

export default Reducer