import {
    MinusOutlined,
    MinusSquareFilled,
    PlusOutlined,
    PlusSquareFilled
} from "@ant-design/icons"
import type { InputProps } from "antd"
import { Form } from "antd"
import { InputNumber } from "antd"
import classNames from "classnames/bind"
import React, { forwardRef, useEffect, useState } from "react"

import style from "./index.module.scss"
const cx = classNames.bind(style)
export interface AmountDisplayRule {
    base?: number
    calculation?: string
    keepDecimalPlaces?: number
}

interface Iprops {
    wantScant?: boolean
    /** 允许输入的最小值 */
    min?: number
    /** 允许输入的最大值 */
    max?: number
    /** 默认填充数量 */
    value: string | number | null

    /** 默认状态 */
    defaultStatus?: any

    /** 总宽度 */
    width: number | string
    /** 总高度 */
    height: number
    /** 工作站数量输入框/表单数量输入框 */
    isWorkStationInput: boolean
    /** placeholder */
    placeholder: string | undefined
    disable?: boolean
    amountDisplayRule: AmountDisplayRule
    /** 数量改变事件 */
    onChange: (val: number) => void
    /** 回车事件 */
    onSave: () => void
    onBlur: () => void
    onFocus: (e: any) => void
    /** 输入框状态 */
    handleStatusChange: (
        status: InputProps["status"],
        currentInputValue: number
    ) => void
    /** HACK: 禁用时也允许减 */
    allowMinusWhenDisabled?: boolean
    precision: number
    backgroundColor: string
}

const InputComponent = (props: any) => {
    const {
        refs,
        placeholder,
        value,
        disabled,
        status,
        onPressEnter,
        onChange,
        onBlur,
        style,
        bordered,
        controls = false,
        isWorkStationInput = true,
        help,
        size,
        amountDisplayRule,
        precision,
        ...rest
    } = props

    const [inputValue, setInputValue] = useState()

    useEffect(() => {
        setInputValue(value)
    }, [value])

    return (
        <Form.Item
            style={{ margin: 0 }}
            validateStatus={status}
            help={!isWorkStationInput ? help : ""}
        >
            <InputNumber
                {...rest}
                precision={amountDisplayRule?.keepDecimalPlaces || precision}
                controls={controls}
                ref={refs}
                placeholder={placeholder}
                bordered={bordered}
                size={size}
                style={style}
                value={inputValue}
                status={status}
                disabled={disabled}
                onChange={(val) => onChange && onChange(val)}
                onBlur={onBlur}
                onPressEnter={onPressEnter}
                className="count-input"
                type="number"
            />
        </Form.Item>
    )
}

const CountInput = (props: any) => {
    const {
        refs,
        placeholder,
        value,
        disable,
        status,
        onPressEnter,
        onChange,
        onBlur,
        ...rest
    } = props
    return (
        <InputComponent
            {...rest}
            controls={false}
            refs={refs}
            placeholder={placeholder}
            bordered={false}
            size="large"
            style={{
                fontSize: 60,
                color: "#004CAA",
                fontWeight: "bold",
                padding: "0px 8px",
                textAlign: "center",
                width: "100%"
            }}
            value={value}
            status={status}
            disabled={disable}
            onChange={(val: string) => onChange && onChange(val)}
            onBlur={onBlur}
            onPressEnter={onPressEnter}
        />
    )
}

const FormCountInput = (props: any) => {
    const {
        refs,
        placeholder,
        value,
        disable,
        status,
        onPressEnter,
        onChange,
        onBlur,
        minus,
        plus,
        width,
        ...rest
    } = props
    return (
        <InputComponent
            {...rest}
            controls={false}
            refs={refs}
            addonBefore={<MinusOutlined onClick={minus} />}
            addonAfter={<PlusOutlined onClick={plus} />}
            placeholder={placeholder}
            value={value}
            size="large"
            disabled={disable}
            status={status}
            onChange={(val: string) => onChange && onChange(val)}
            onBlur={onBlur}
            onPressEnter={onPressEnter}
            style={{ width }}
        />
    )
}

const Counter = (
    {
        min = 0,
        max = Infinity,
        value,
        defaultStatus = "",
        width = 300,
        disable,
        wantScant = false,
        height = 84,
        isWorkStationInput = false,
        placeholder = "-",
        amountDisplayRule,
        onChange,
        onSave,
        handleStatusChange,
        allowMinusWhenDisabled,
        onFocus,
        precision,
        backgroundColor = "#F8F8F8"
    }: Partial<Iprops>,
    ref: any
) => {
    const [status, setStatus] = useState<InputProps["status"]>(defaultStatus)

    const handleCountChange = (val: number) => {
        if (disable) return
        // 当输入非数字
        if (isNaN(Number(val))) {
            setStatus("error")
            handleStatusChange?.("error", val)
            return
        }

        const isValid = Number(val) <= max && Number(val) >= min

        // 当输入数据不合法
        if (!isValid) {
            setStatus("error")
            handleStatusChange?.("error", val)
            return
        }
        status && setStatus("")
        handleStatusChange?.("", val)
        onChange && onChange(Number(val))
    }

    const minus = () => {
        if ((!allowMinusWhenDisabled && disable) || Number(value) <= min) return
        onChange && onChange(Number(value) - 1)
        status && setStatus("")
        handleStatusChange?.("", Number(value))
    }

    const plus = () => {
        if (disable) return
        if (Number(value) === max) return
        console.log("plus", value, Number(value))
        onChange && onChange(Number(value) + 1)
        status && setStatus("")
        handleStatusChange?.("", Number(value))
    }

    return isWorkStationInput ? (
        <div
            style={{
                width,
                height,
                padding: "0 14px",
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                // border: `${
                //     status === "error"
                //         ? "4px solid #ff4d4f"
                //         : "4px solid #E8E8E8"
                // }`,
                // boxShadow: "0px 0px 0px 6px rgba(216,216,216,0.25)",
                borderRadius: 4,
                backgroundColor
            }}
        >
            <MinusSquareFilled
                onClick={minus}
                style={{
                    userSelect: "none",
                    fontSize: 60,
                    backgroundColor: "#fff",
                    color:
                        disable || !value || Number(value) === 0
                            ? "#E4E4E4"
                            : "#999"
                }}
            />

            <CountInput
                className={cx("counter")}
                refs={ref}
                placeholder={placeholder}
                value={value}
                min={min}
                max={max}
                disable={disable}
                status={status}
                amountDisplayRule={amountDisplayRule}
                precision={precision}
                onChange={handleCountChange}
                onSave={onSave}
                onFocus={onFocus}
            />

            <PlusSquareFilled
                onClick={plus}
                style={{
                    userSelect: "none",
                    fontSize: 60,
                    backgroundColor: "#fff",
                    color: !disable && Number(value) < max ? "#999" : "#E4E4E4"
                }}
            />
        </div>
    ) : (
        <FormCountInput
            refs={ref}
            placeholder={placeholder}
            value={value}
            min={min}
            max={max}
            disable={disable}
            status={status}
            amountDisplayRule={amountDisplayRule}
            onChange={handleCountChange}
            onSave={onSave}
            minus={minus}
            plus={plus}
            isWorkStationInput={isWorkStationInput}
            width={width}
            precision={precision}
        />
    )
}

export default forwardRef(Counter)
