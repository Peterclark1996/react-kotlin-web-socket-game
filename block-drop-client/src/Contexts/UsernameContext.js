import { createContext, useContext } from 'react'
import { useState } from 'react'

const UsernameContext = createContext()

export const UsernameProvider = props => {
    const [username, setUsername] = useState("")

    const value = {
        username,
        setUsername
    }

    return <UsernameContext.Provider value={value} {...props} />
}

export const useUsername = () => useContext(UsernameContext)