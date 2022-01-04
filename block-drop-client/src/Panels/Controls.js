import { useCallback, useEffect, useState } from "react"

const Controls = () => {
    const keyClassName = "d-flex border m-2 Key align-items-center justify-content-center"

    const KEY_A = 65
    const KEY_S = 83
    const KEY_D = 68
    const KEY_LEFT = 37
    const KEY_DOWN = 40
    const KEY_RIGHT = 39

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