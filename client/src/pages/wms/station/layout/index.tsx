import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
import type { ReactChild, ReactChildren } from "react"
import React from "react"
import Body from "./body"
import Toolbar from "./toolbar"
import Header from "./header"
import style from "./styles.module.scss"

type WorkStationLayoutProps = Omit<
    WorkStationConfig<string>,
    "operationMap" | "layout" | "type"
> & {
    children: ReactChild | ReactChildren
}

const WorkStationLayout = (props: WorkStationLayoutProps) => {
    const { title, actions, stepsDescribe, children, extraTitleInfo } = props
    return (
        <div
            className="d-flex flex-col w-full h-full overflow-x-hidden overflow-y-auto px-4 pt-3"
            // className={style["container"]}
        >
            <Header
                extraTitleInfo={extraTitleInfo}
                title={title}
                stepsDescribe={stepsDescribe}
            />
            <Toolbar actions={actions} />
            <Body>{children}</Body>
        </div>
    )
}

export default WorkStationLayout
