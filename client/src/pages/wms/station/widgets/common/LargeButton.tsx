import React from "react"

const LargeButton = (props: any) => {
    const { text, fontSize = "1.5rem", backgroundColor, onClick } = props
    return (
        <div
            className="h-full py-2 px-1 d-flex justify-center items-center text-center font-semibold"
            style={{
                backgroundColor,
                cursor: "pointer",
                fontSize
            }}
            onClick={onClick}
        >
            {text}
        </div>
    )
}

export default LargeButton
