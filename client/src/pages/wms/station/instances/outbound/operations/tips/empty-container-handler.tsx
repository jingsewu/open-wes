/**
 * 空箱处理
 */
import { TipType } from "@/pages/wms/station/instances/outbound/operations/tips/type"
// import IntlMessages from "@/util/IntlMessages"
import { Result } from "antd"
import React, { useMemo } from "react"
// import { useSelector } from "react-redux"
import styles from "./empty-container.module.scss"

const EMPTY_CONTAINER_ENUM_KEY = "EmptyContainerProcessType"

export default function EmptyContainerHandler(props: any) {
    const { value } = props
    const {
        emptyContainerProcessTypeEnums,
        onCustomActionDispatch,
        containerCode
    } = value
    // const { enums } = useSelector(({ enums }: any) => enums)
    const enums = { [EMPTY_CONTAINER_ENUM_KEY]: [] }
    const formatEnums = useMemo(() => {
        const res: any = {}
        if (enums[EMPTY_CONTAINER_ENUM_KEY]) {
            enums[EMPTY_CONTAINER_ENUM_KEY].forEach((item: any) => {
                res[item.enumValue] = {
                    label: item.label,
                    value: item.enumValue
                }
            })
        }
        return res
    }, [enums])

    const handleConfirm = (type: string) => {
        onCustomActionDispatch({
            eventCode: TipType.EMPTY_CONTAINER_HANDLE_TIP,
            data: {
                containerCode,
                emptyContainerProcessType: type
            }
        })
    }

    const getButtons = () => {
        return (
            <div className={styles["container"]}>
                {emptyContainerProcessTypeEnums.map((item: any) => {
                    return (
                        <div
                            className={styles["btn"]}
                            key={item}
                            onClick={handleConfirm.bind(
                                null,
                                formatEnums[item]?.value
                            )}
                        >
                            {formatEnums[item]?.label}
                        </div>
                    )
                })}
            </div>
        )
    }

    return (
        <Result
            status="warning"
            // title={<IntlMessages id="workstaion.common.emptyBox.handle" />}
            title="请进行空箱处理"
            extra={getButtons()}
        />
    )
}
