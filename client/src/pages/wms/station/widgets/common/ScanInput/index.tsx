import { DEBOUNCE_TIME } from "@/pages/wms/station/constant"
import { useDebounceFn } from "ahooks"
import { Input } from "antd"
import classNames from "classnames/bind"
import * as React from "react"
import { useEffect, useState } from "react"
import type { RefObject } from "react"
import { useTranslation } from "react-i18next"
//
import style from "./index.module.scss"

const cx = classNames.bind(style)

interface ComProps {
    placeholder: string
    onChange: (params: string) => {}
    onPressEnter: (param1: string, params2?: boolean) => {}
    refs: RefObject<any>
    value: string
    moveInText: string
    fullText: string
    support: boolean
}

const Index = (props: ComProps, ref: RefObject<any>) => {
    const { t } = useTranslation()
    const {
        placeholder = t("skuArea.scanOrEnterBarcode"),
        // intl.formatMessage({
        //     id: "workstaion.common.tip.pleaseEnterSkuCode"
        // }),
        onChange,
        onPressEnter,
        refs,
        value,
        moveInText = "MOVEIN",
        fullText = "FULL",
        support = false,
        ...rest
    } = props

    const [scanCode, setScanCode] = useState<string>("")
    const [isScan, setIsScan] = useState<boolean>()
    useEffect(() => {
        setScanCode(value)
    }, [value])

    const handleChange = (e: any) => {
        if (
            support &&
            (e.target.value?.indexOf(moveInText) !== -1 ||
                e.target.value?.indexOf(fullText) !== -1)
        ) {
            setScanCode(
                e.target.value?.replace(moveInText, "")?.replace(fullText, "")
            )
            setIsScan(true)
            onChange && onChange(scanCode)
            return
        }
        setScanCode(e.target.value)
        onChange && onChange(scanCode)
    }

    const handlePressEnter = useDebounceFn(
        () => {
            if (isScan) {
                setIsScan(false)
                onPressEnter && onPressEnter(scanCode, true)
                return
            }
            onPressEnter && onPressEnter(scanCode)
        },
        { wait: DEBOUNCE_TIME }
    ).run

    return (
        <Input
            {...rest}
            ref={refs}
            className={cx("barcode")}
            value={scanCode}
            onChange={handleChange}
            onPressEnter={handlePressEnter}
            placeholder={placeholder}
        />
    )
}

export default Index
