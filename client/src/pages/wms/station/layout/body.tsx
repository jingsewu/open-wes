import React from "react"

const WorkStationLayoutBody = (props: any) => {
    const {children} = props

    return (
        <div
            className="flex-1 h-full mb-6"
        >
            {children}
        </div>
    )
}

export default WorkStationLayoutBody
