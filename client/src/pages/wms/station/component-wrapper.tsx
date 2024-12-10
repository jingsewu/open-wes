/**
 * @Description: 用于包装组件，注入组件需要的通用方法，并对event中的数据进行filter处理
 */
import classNames from "classnames/bind"
import { debounce } from "lodash"
import type { FunctionComponent, ReactNode } from "react"
import React, { useContext, useEffect, useRef } from "react"

import { DEBOUNCE_TIME } from "@/pages/wms/station/constant"
import {
    APIContext,
    OperationsContext,
    WorkStationContext
} from "@/pages/wms/station/event-loop/provider"
import {
    WorkStationContextProps,
    WorkStationEvent,
    ChooseArea
} from "@/pages/wms/station/event-loop/types"
import type { OperationProps } from "@/pages/wms/station/instances/types"

import styles from "./layout/styles.module.scss"

const cx = classNames.bind(styles)

/**
 *
 * @param props
 */
function Wrapper(props: {
    type: string
    Component: FunctionComponent<OperationProps<any, any>>
    valueFilter: (data: WorkStationEvent<any> | undefined) => any
    changeAreaHandler?: () => Promise<void>
    withWrapper?: boolean
    style?: React.CSSProperties
}) {
    const {
        type,
        Component,
        valueFilter,
        changeAreaHandler,
        withWrapper = true,
        style
    } = props

    const ref = useRef<ReactNode>(null)

    const { workStationEvent, workStationInfo } =
        useContext<WorkStationContextProps>(WorkStationContext)

    const { operationsMap, setOperationsMap } = useContext(OperationsContext)

    const { onCustomActionDispatch, message } = useContext(APIContext)

    useEffect(() => {
        /**
         * 将当前组件实例注册到OperationContext中
         */
        operationsMap.set(type, ref.current)
        setOperationsMap(operationsMap)
    }, [workStationEvent])

    const evenChangeHandler = debounce(
        async () => {
            // 当前后台要求选中的组件和用户选中的组件一致时，不进行任何操作
            if (workStationEvent?.chooseArea === type) return
            changeAreaHandler && (await changeAreaHandler())
        },
        DEBOUNCE_TIME,
        { leading: true }
    )

    const comp = (
        <Component
            refs={ref}
            onCustomActionDispatch={onCustomActionDispatch}
            value={valueFilter(workStationEvent)}
            message={message}
            workStationInfo={workStationInfo}
            isActive={workStationEvent && type === workStationEvent?.chooseArea}
        />
    )

    return withWrapper ? (
        <div
            className={cx({
                "operation-area": withWrapper,
                highlight: workStationEvent?.chooseArea === type
            })}
            onClick={evenChangeHandler}
            style={{ ...style, backgroundColor: "#fff" }}
        >
            {comp}
        </div>
    ) : (
        comp
    )
}

export default Wrapper
