const Overlay = ({ children }) => {
    return (
        <div className="OverlayBackground position-fixed w-100 h-100">
            <div className="Overlay bg-white text-dark rounded">
                {children}
            </div>
        </div>
    )
}

export default Overlay