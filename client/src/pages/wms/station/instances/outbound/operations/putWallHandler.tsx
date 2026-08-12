import React, { useImperativeHandle } from "react"

import type { OperationProps } from "@/pages/wms/station/instances/types"
import PutWall from "@/pages/wms/station/widgets/PutWall"
import type { PutWallProps } from "../../../widgets/PutWall/types"
import { useWorkStation } from "../../../state"

type PutWallHandlerProps = Pick<
    PutWallProps,
    "putWallDisplayStyle" | "putWallViews" | "putWallTagConfigDTO"
>

const PutWallHandler = (props: OperationProps<PutWallHandlerProps, any>) => {
    const { refs } = props
    const { workStationEvent } = useWorkStation()

    const value = {
        putWallViews: workStationEvent?.putWallArea?.putWallViews || [],
        putWallDisplayStyle: workStationEvent?.putWallArea?.putWallDisplayStyle,
        putWallTagConfigDTO: workStationEvent?.putWallArea?.putWallTagConfigDTO || {},
        chooseType: workStationEvent?.chooseArea
    }

    useImperativeHandle(refs, () => value)

    return <PutWall />
}

export default PutWallHandler
