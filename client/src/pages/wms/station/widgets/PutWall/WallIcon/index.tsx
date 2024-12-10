import Icon from "@ant-design/icons"

import React from "react"

import WallAllSvg from "@/icon/wall/wall_all.svg"

import SplitIcon from "./SplitIcon"
// import MergeIcon from "./SplitIcon"

const MergeIcon = () => (
    <Icon
        component={() => <WallAllSvg />}
        className="text-2xl	mr-4 text-secondary"
    />
)

const WallIcon = (props: any) => {
    const { putWallDisplayStyle = "merge", putWallViews = [] } = props

    const displayIcon = {
        merge: <MergeIcon />,
        split: <SplitIcon putWallViews={putWallViews} />
    }

    return (
        <div className="leading-none">{displayIcon[putWallDisplayStyle]}</div>
    )
}

export default WallIcon
