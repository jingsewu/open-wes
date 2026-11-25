import {
    CheckCircleFilled,
    ExclamationCircleFilled,
    MinusOutlined,
    PlusOutlined
} from "@ant-design/icons"
import {Button, InputNumber, message, Modal} from "antd"
import React, {useEffect, useRef, useState} from "react"
import {useTranslation} from "react-i18next"

import {useWorkStation} from "@/pages/wms/station/state"
import {CustomActionType} from "../../customActionType"

interface QTYFILLProps {
    taskId?: string
    bookQty: number
    isBlind: boolean
}

interface StocktakeSubmitResponse {
    code?: string
    data?: {
        status?: string
        code?: string
    }
}

const QTYFILL = (props: QTYFILLProps) => {
    const {taskId, bookQty, isBlind} = props
    const {t} = useTranslation()
    const {onActionDispatch} = useWorkStation()

    const initialQty = isBlind ? 0 : bookQty
    const [value, setValue] = useState<number | null>(initialQty)
    const [submitting, setSubmitting] = useState(false)
    const [submittedTaskId, setSubmittedTaskId] = useState<string>()
    const inputRef = useRef<HTMLInputElement>(null)

    useEffect(() => {
        setValue(isBlind ? 0 : bookQty)
        // Select the book quantity so typing a different count replaces it.
        inputRef.current?.focus()
        inputRef.current?.select()
    }, [taskId, bookQty, isBlind])

    const alreadySubmitted = !!taskId && submittedTaskId === taskId

    const counted = value ?? 0
    const diff = counted - bookQty
    const hasValue = value !== null
    const digits = String(counted).length
    const countSize = digits >= 5 ? 54 : digits === 4 ? 66 : 86

    const submit = async (qty: number) => {
        if (!taskId) return
        setSubmitting(true)
        try {
            const response = (await onActionDispatch({
                eventCode: CustomActionType.STOCKTAKE_SUBMIT,
                data: {
                    detailId: taskId,
                    stocktakeQty: qty
                }
            })) as StocktakeSubmitResponse | undefined
            const payload = response?.data ?? {}
            const failed =
                response?.code === "-1" ||
                !!payload.status ||
                (payload.code !== undefined && payload.code !== "0")

            if (failed) {
                // The request interceptor already surfaces business errors.
                // SAT010001 is suppressed globally, so cover it here.
                if (payload.status === "SAT010001") {
                    message.error(t("stocktake.station.count.failed"))
                }
                return
            }

            message.success(t("stocktake.station.count.recorded"))
            setSubmittedTaskId(taskId)
            setValue(isBlind ? 0 : bookQty)
        } catch {
            message.error(t("stocktake.station.count.failed"))
        } finally {
            setSubmitting(false)
        }
    }

    const handleSubmit = () => {
        if (!hasValue || counted < 0) {
            message.warning(t("toast.incorrectQty"))
            return
        }
        if (!isBlind && counted !== bookQty) {
            Modal.confirm({
                title: t("stocktake.station.count.confirmTitle"),
                content: t("stocktake.station.count.confirmContent", {
                    qty: counted,
                    system: bookQty
                }),
                okText: t("stocktake.station.count.record"),
                cancelText: t("common.cancel"),
                onOk: () => submit(counted)
            })
            return
        }
        void submit(counted)
    }

    const updateValue = (next: number | null) => {
        if (next === null) {
            setValue(null)
            return
        }
        const safe = Math.max(0, Math.floor(next))
        setValue(Number.isFinite(safe) ? safe : null)
    }

    const scaleMax = Math.max(bookQty, counted, 10)
    const fillPercent = Math.min(100, (counted / scaleMax) * 100)
    const bookPercent = Math.min(100, (bookQty / scaleMax) * 100)

    return (
        <div className="stocktake-count">
            <div className="stocktake-count__head">
                <span className="stocktake-field-label">
                    {t("stocktake.station.count.label")}
                </span>
                {isBlind && (
                    <span className="stocktake-count__blind">
                        {t("stocktake.station.count.blind")}
                    </span>
                )}
            </div>

            <div className="stocktake-counter">
                <button
                    type="button"
                    className="stocktake-counter__step"
                    aria-label={t("stocktake.station.count.decrease")}
                    disabled={counted <= 0 || submitting}
                    onClick={() => updateValue(counted - 1)}
                >
                    <MinusOutlined aria-hidden="true" />
                </button>

                <InputNumber
                    ref={inputRef}
                    className="stocktake-counter__input"
                    style={
                        {
                            "--st-count-size": `${countSize}px`
                        } as React.CSSProperties
                    }
                    controls={false}
                    min={0}
                    precision={0}
                    value={value}
                    aria-label={t("stocktake.station.count.label")}
                    onChange={(next) => {
                        if (next === null) {
                            setValue(null)
                            return
                        }
                        const numeric = Number(next)
                        updateValue(Number.isNaN(numeric) ? null : numeric)
                    }}
                    onPressEnter={handleSubmit}
                />

                <button
                    type="button"
                    className="stocktake-counter__step"
                    aria-label={t("stocktake.station.count.increase")}
                    disabled={submitting}
                    onClick={() => updateValue(counted + 1)}
                >
                    <PlusOutlined aria-hidden="true" />
                </button>
            </div>

            {!isBlind && (
                <>
                    <div className="stocktake-gauge" aria-hidden="true">
                        <div className="stocktake-gauge__track">
                            <div
                                className={
                                    "stocktake-gauge__fill" +
                                    (hasValue && diff !== 0
                                        ? " stocktake-gauge__fill--diff"
                                        : "")
                                }
                                style={{width: `${fillPercent}%`}}
                            />
                            <div
                                className="stocktake-gauge__mark"
                                style={{left: `${bookPercent}%`}}
                            />
                        </div>
                    </div>
                    <div
                        className={
                            "stocktake-diff " +
                            (diff === 0
                                ? "stocktake-diff--match"
                                : "stocktake-diff--warn")
                        }
                        aria-live="polite"
                    >
                        {!hasValue ? null : diff === 0 ? (
                            <>
                                <CheckCircleFilled aria-hidden="true" />
                                <span>
                                    {t(
                                        "stocktake.station.count.matches"
                                    )}
                                </span>
                            </>
                        ) : (
                            <>
                                <ExclamationCircleFilled aria-hidden="true" />
                                <span>
                                    {diff > 0
                                        ? t(
                                              "stocktake.station.count.more",
                                              {count: diff}
                                          )
                                        : t(
                                              "stocktake.station.count.fewer",
                                              {count: Math.abs(diff)}
                                          )}
                                </span>
                            </>
                        )}
                    </div>
                </>
            )}

            <Button
                type="primary"
                className="stocktake-submit"
                data-testid="stocktake-submit"
                loading={submitting}
                disabled={!hasValue || alreadySubmitted}
                onClick={handleSubmit}
            >
                {t("stocktake.station.count.record")}
            </Button>
        </div>
    )
}

export default QTYFILL
