import { useCallback, useEffect, useState } from "react"
import { useWebSocket } from '../Contexts/WebSocketContext'

const KEY_A = 65
const KEY_S = 83
const KEY_D = 68
const KEY_LEFT = 37
const KEY_DOWN = 40
const KEY_RIGHT = 39
const KEY_Q = 81
const KEY_E = 69

const Controls = () => {
    const { send } = useWebSocket()

    const keyClassName = "d-flex border m-2 Key align-items-center justify-content-center"

    const [pressingLeft, setPressingLeft] = useState(false)
    const [pressingRight, setPressingRight] = useState(false)
    const [pressingDown, setPressingDown] = useState(false)
    const [pressingRotateLeft, setPressingRotateLeft] = useState(false)
    const [pressingRotateRight, setPressingRotateRight] = useState(false)

    const updateKeyPressState = (keyCode, state) => {
        switch(keyCode){
            case KEY_A:
            case KEY_LEFT:
                setPressingLeft(state)
                break
            case KEY_D:
            case KEY_RIGHT:
                setPressingRight(state)
                break
            case KEY_S:
            case KEY_DOWN:
                setPressingDown(state)
                break
            case KEY_Q:
                setPressingRotateLeft(state)
                break
            case KEY_E:
                setPressingRotateRight(state)
                break
            default:
                break
        }
    }

    const onKeyDown = useCallback(event => updateKeyPressState(event.keyCode, true), [])

    const onKeyUp = useCallback(event => updateKeyPressState(event.keyCode, false), [])

    useEffect(() => {
        document.addEventListener("keydown", onKeyDown)
        document.addEventListener("keyup", onKeyUp)
        return () => {
            document.removeEventListener("keydown", onKeyDown)
            document.removeEventListener("keyup", onKeyUp)
        }
    }, [onKeyDown, onKeyUp])

    useEffect(() => send("InboundUpdatePressedKey", {
        pressingLeft,
        pressingRight,
        pressingDown,
        pressingRotateRight,
        pressingRotateLeft
    }), [pressingDown, pressingLeft, pressingRight, pressingRotateLeft, pressingRotateRight, send])

    return(
        <div className="d-flex flex-column justify-content-center user-select-none">
            <div className="d-flex justify-content-center">
                <div className={`${keyClassName} ${pressingRotateLeft ? "Pressed" : ""}`}>
                    Q/⟲
                </div>
                <div className={`${keyClassName} ${pressingRotateRight ? "Pressed" : ""}`}>
                    E/⟳
                </div>
            </div>
            <div className="d-flex justify-content-center">
                <div className={`${keyClassName} ${pressingLeft ? "Pressed" : ""}`}>
                    A/🡨
                </div>
                <div className={`${keyClassName} ${pressingDown ? "Pressed" : ""}`}>
                    S/🡫
                </div>
                <div className={`${keyClassName} ${pressingRight ? "Pressed" : ""}`}>
                    D/🡪
                </div>
            </div>
        </div>
    )
}

export default Controls