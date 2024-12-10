import type { InputProps } from "antd"
import { Input } from "antd"
import classNames from "classnames/bind"
import React, { useRef } from "react"

import style from "./index.module.scss"

const cx = classNames.bind(style)

interface PureScanInputProps extends InputProps {
    shortcutCodeMap?: { moveInText: string; fullText: string }
}

// 受控扫码输入框
const PureScanInput: React.ForwardRefRenderFunction<any, PureScanInputProps> = (
    props,
    ref
) => {
    const { onPressEnter, onChange, shortcutCodeMap, ...rest } = props
    const isShortcut = useRef<boolean>(false)

    const handleChange = (e: any) => {
        const val = e.target.value
        if (
            val &&
            shortcutCodeMap &&
            (val.includes(shortcutCodeMap.moveInText) ||
                val.includes(shortcutCodeMap?.fullText))
        ) {
            isShortcut.current = true
            e.target.value = val
                .replace(shortcutCodeMap.moveInText, "")
                .replace(shortcutCodeMap.fullText, "")
        } else {
            isShortcut.current = false
        }

        onChange && onChange(e)
    }

    const handlePressEnter = (e: any) => {
        if (isShortcut.current) {
            isShortcut.current = false
            return
        }
        onPressEnter && onPressEnter(e.target.value)
    }

    return (
        <Input
            ref={ref}
            className={cx("scan-input")}
            size="large"
            onPressEnter={handlePressEnter}
            onChange={handleChange}
            {...rest}
        />
    )
}

export default React.forwardRef(PureScanInput)
