/**
 * @Description: 用于包装组件，注入组件需要的通用方法，并对event中的数据进行filter处理
 */
import classNames from "classnames/bind"
import {debounce} from "lodash"
import type {FunctionComponent, ReactNode} from "react"
import React, {useContext, useEffect, useRef, memo, useMemo, useCallback} from "react"

import {DEBOUNCE_TIME} from "@/pages/wms/station/constants/constant"
import {WorkStationContext} from "@/pages/wms/station/event-loop/provider"
import {WorkStationView} from "@/pages/wms/station/event-loop/types"
import type {OperationProps} from "@/pages/wms/station/instances/types"

import styles from "./layout/styles.module.scss"

const cx = classNames.bind(styles)

/**
 *
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

    // 使用统一的WorkStationContext，一次性获取所有需要的值
    const {workStationEvent, operationsMap, setOperationsMap, onActionDispatch, message} = useContext(WorkStationContext)

    // 只对复杂计算使用 useMemo
    const filteredValue = useMemo(() => {
        return valueFilter(workStationEvent);
    }, [workStationEvent, valueFilter]);

    // 简单的布尔值计算不需要 useMemo
    const isActive = workStationEvent && type === workStationEvent?.chooseArea;

    // 使用 useCallback 缓存防抖函数，但减少依赖项
    const evenChangeHandler = useCallback(
        debounce(
            async () => {
                // 当前后台要求选中的组件和用户选中的组件一致时，不进行任何操作
                if (workStationEvent?.chooseArea === type) return
                changeAreaHandler && (await changeAreaHandler())
            },
            DEBOUNCE_TIME,
            {leading: true}
        ),
        [type, changeAreaHandler] // 移除 workStationEvent?.chooseArea，因为防抖函数内部会重新获取
    );

    useEffect(() => {
        /**
         * 将当前组件实例注册到OperationContext中
         */
        operationsMap.set(type, ref.current)
        setOperationsMap(operationsMap)
    }, [workStationEvent, type]) // 移除 operationsMap 和 setOperationsMap 依赖

    // 简单的 JSX 不需要 useMemo
    const comp = (
        <Component
            refs={ref}
            onActionDispatch={onActionDispatch}
            value={filteredValue}
            message={message}
            isActive={isActive}
        />
    );

    // 简单的 className 计算不需要 useMemo
    const wrapperClassName = cx({
        "operation-area": withWrapper,
        highlight: workStationEvent?.chooseArea === type
    });

    return withWrapper ? (
        <div
            className={wrapperClassName}
            onClick={evenChangeHandler}
            style={{...style, backgroundColor: "#fff"}}
        >
            {comp}
        </div>
    ) : (
        comp
    )
})

export default Wrapper
