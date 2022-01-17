import { useCallback, useEffect, useState } from "react"
import { useWebSocket } from '../Contexts/WebSocketContext'

const KEY_A = 65
const KEY_S = 83
const KEY_D = 68
const KEY_LEFT = 37
const KEY_DOWN = 40
const KEY_RIGHT = 39

const keyIdToEnum = keyId =>{
    switch(keyId){
        case KEY_A:
        case KEY_LEFT:
            return "LEFT"
        case KEY_D:
        case KEY_RIGHT:
            return "RIGHT"
        case KEY_S:
        case KEY_DOWN:
            return "DOWN"
        default:
            return "NONE"
    }
}

const Controls = () => {
    const { send } = useWebSocket()

    const keyClassName = "d-flex border m-2 Key align-items-center justify-content-center"

    const [selectedKey, setSelectedKey] = useState(0)

    const onKeyDown = useCallback(event => setSelectedKey(event.keyCode), [])

    const onKeyUp = useCallback(event => {
        if(selectedKey === event.keyCode){
            setSelectedKey(0)
        }
    }, [selectedKey])

    useEffect(() => {
        document.addEventListener("keydown", onKeyDown)
        document.addEventListener("keyup", onKeyUp)
        return () => {
            document.removeEventListener("keydown", onKeyDown)
            document.removeEventListener("keyup", onKeyUp)
        }
    }, [onKeyDown, onKeyUp])

    useEffect(() => send("InboundUpdatePressedKey", {
        pressedKey: keyIdToEnum(selectedKey)
    }), [selectedKey, send])

    return(
        <div className="d-flex flex-column h-100 justify-content-center">
            <div className="d-flex justify-content-center">
                <div className={`${keyClassName} ${selectedKey === KEY_A ? "Pressed" : ""}`}>
                    A
                </div>
                <div className={`${keyClassName} ${selectedKey === KEY_S ? "Pressed" : ""}`}>
                    S
                </div>
                <div className={`${keyClassName} ${selectedKey === KEY_D ? "Pressed" : ""}`}>
                    D
                </div>
            </div>
            <div className="d-flex justify-content-center">
                <div className={`${keyClassName} ${selectedKey === KEY_LEFT ? "Pressed" : ""}`}>
                    🡨
                </div>
                <div className={`${keyClassName} ${selectedKey === KEY_DOWN ? "Pressed" : ""}`}>
                    🡫
                </div>
                <div className={`${keyClassName} ${selectedKey === KEY_RIGHT ? "Pressed" : ""}`}>
                    🡪
                </div>
            </div>
        </div>
    )
}

export default Controls