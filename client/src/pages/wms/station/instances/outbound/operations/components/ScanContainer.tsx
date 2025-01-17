import { ScanOutlined } from "@ant-design/icons"
import { useDebounceFn } from "ahooks"
import type { InputRef } from "antd"
import { Input } from "antd"
import classNames from "classnames/bind"
import React, { useEffect, useRef, useState, useMemo } from "react"
import { DEBOUNCE_TIME } from "@/pages/wms/station/constant"
import { returnButton } from "@/pages/wms/station/event-loop/utils"

import style from "./index.module.scss"
import {useTranslation} from "react-i18next";

const cx = classNames.bind(style)

const SCAN_PERMISSIONS = [10701] // 扫描容器权限ID
interface Iprops {
    /** 料箱号 */
    containerCode?: string
    /** 是否出库默认页面 */
    isDefaultPage?: boolean
    /** 是否开启多拣选位 */
    conveyorMultiplePickingPositions?: boolean
    /** 扫码完成事件 */
    onChange?: (param: string | undefined) => void
    /** 点击扫码框展示input事件 */
    handleShowInput?: () => void
    isActive?: boolean
}

const ScanIcon = ({ showInput, handleClick }: any) => {
    return (
        <div
            className={cx("scan-icon", { active: showInput })}
            onClick={handleClick}
        >
            <ScanOutlined className={cx("scan-svg")} />
        </div>
    )
}

const ScanContainer = ({
    containerCode,
    isDefaultPage = false,
    conveyorMultiplePickingPositions,
    handleShowInput,
    onChange,
    isActive
}: Iprops) => {
    const [showInput, setShowInput] = useState(false)
    const [scanCode, setScanCode] = useState<string>()

    const { t } = useTranslation();

    const inputRef = useRef<InputRef>(null)

    useEffect(() => {
        if (!containerCode) return
        setShowInput(false)
    }, [containerCode])

    useEffect(() => {
        if (!showInput || !isActive) return
        inputRef.current?.focus()
    }, [showInput, isActive])

    const handleChange = (e: any) => {
        setScanCode(e.target.value)
    }

    const handleFinishScan = useDebounceFn(
        () => {
            onChange && onChange(scanCode)
        },
        { wait: DEBOUNCE_TIME }
    ).run

    const handleClick = useDebounceFn(
        () => {
            if (conveyorMultiplePickingPositions) {
                return handleShowInput && handleShowInput()
            }
            setShowInput(!showInput)
            scanCode && setScanCode("")
        },
        { wait: DEBOUNCE_TIME }
    ).run

    const scanPermissions = useMemo(() => {
        return returnButton(SCAN_PERMISSIONS)
    }, [])

    return isDefaultPage && !showInput ? (
        <ScanIcon showInput={showInput} handleClick={handleClick} />
    ) : (
        <div className={cx("container-code-box")}>
            <div className={cx("container-code", { codecolor: containerCode })}>
                {showInput ? (
                    <Input
                        placeholder= {t("table.containerCode")}
                        size="large"
                        value={scanCode}
                        onChange={handleChange}
                        onPressEnter={handleFinishScan}
                        style={{ borderRadius: 4 }}
                        ref={inputRef}
                    />
                ) : (
                    containerCode || t("table.containerCode")  // Simplified translation usage
                )}
            </div>
            {scanPermissions && (
                <ScanIcon showInput={showInput} handleClick={handleClick} />
            )}
        </div>
    )
}

export default ScanContainer
