/**
 * @Description: 用于包装组件，注入组件需要的通用方法，并对event中的数据进行filter处理
 */
import classNames from "classnames/bind"
import { debounce } from "lodash"
import type { FunctionComponent, ReactNode } from "react"
import React, { useEffect, useRef, memo, useMemo, useCallback } from "react"

import { DEBOUNCE_TIME } from "@/pages/wms/station/constants/constant"
import { useWorkStation } from "@/pages/wms/station/state"
import { WorkStationView } from "@/pages/wms/station/event-loop/types"
import type { OperationProps } from "@/pages/wms/station/instances/types"

import styles from "./layout/styles.module.scss"

const cx = classNames.bind(styles)

/**
 * 组件包装器
 * @param props
 */
const Wrapper = memo(function Wrapper(props: {
    type: string
    Component: FunctionComponent<OperationProps<any, any>>
    valueFilter: (data: WorkStationView<any> | undefined) => any
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

    const { store, onActionDispatch, message } = useWorkStation()
    const { workStationEvent } = store

    const filteredValue = useMemo(() => {
        return valueFilter(workStationEvent)
    }, [workStationEvent, valueFilter])

    const isActive = workStationEvent?.chooseArea === type

    const evenChangeHandler = useCallback(
        debounce(
            async () => {
                if (workStationEvent?.chooseArea === type) return
                changeAreaHandler && (await changeAreaHandler())
            },
            DEBOUNCE_TIME,
            { leading: true }
        ),
        [type, changeAreaHandler, workStationEvent?.chooseArea]
    )

    // 注册组件实例
    useEffect(() => {
        store.setOperationMap(type, ref.current)
    }, [type, store])

    const wrapperClassName = cx({
        "operation-area": withWrapper,
        highlight: isActive
    })

    return withWrapper ? (
        <div
            className={wrapperClassName}
            onClick={evenChangeHandler}
            style={{ ...style, backgroundColor: "#fff" }}
        >
            <Component
                refs={ref}
                onActionDispatch={onActionDispatch}
                value={filteredValue}
                message={message}
                isActive={isActive}
            />
        </div>
    ) : (
        <Component
            refs={ref}
            onActionDispatch={onActionDispatch}
            value={filteredValue}
            message={message}
            isActive={isActive}
        />
    )
})

export default Wrapper
