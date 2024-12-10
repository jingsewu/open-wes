import React from "react"
// import styles from "./styles.module.scss"

const WorkStationLayoutBody = (props: any) => {
    const { children } = props

    return (
        <div
            className="flex-1 h-full mb-6"
            // className={styles.body}
        >
            {children}
        </div>
    )
}

export default WorkStationLayoutBody
