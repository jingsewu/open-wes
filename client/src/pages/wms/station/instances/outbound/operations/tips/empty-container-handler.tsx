import {Result} from "antd"
import React from "react"
import styles from "./empty-container.module.scss"

const dictionary = JSON.parse(localStorage.getItem("dictionary") || "{}")

export default function EmptyContainerHandler(props: any) {
    const {value} = props
    const {
        onCustomActionDispatch,
        containerCode
    } = value

    const containerOperationTypeEnums = dictionary.ContainerOperationType

    const handleConfirm = (type: string) => {
        onCustomActionDispatch({
            eventCode: "EMPTY_CONTAINER_HANDLE",
            data: {
                containerCode,
                containerOperationType: type
            }
        })
    }

    const getButtons = () => {
        return (
            <div className={styles["container"]}>
                {containerOperationTypeEnums.map((item: any) => {
                    return (
                        <div
                            className={styles["btn"]}
                            key={item}
                            onClick={handleConfirm.bind(
                                null,
                                item?.value
                            )}
                        >
                            {item?.label}
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
