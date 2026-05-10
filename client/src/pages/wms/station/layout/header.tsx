import React, {useEffect} from "react"
import {useHistory} from "react-router"
import {useTranslation} from "react-i18next"
import classNames from "classnames/bind"
import {Modal} from "antd"
import {useWorkStation, observer} from "../state"
import style from "./styles.module.scss"
import {WorkStationStatus} from "@/pages/wms/station/event-loop/types"
import {STATION_MENU_PATH} from "@/pages/wms/station/constants/constant"
import type {WorkStationConfig} from "@/pages/wms/station/instances/types"
import {CustomActionType} from "@/pages/wms/station/instances/outbound/customActionType"

const cx = classNames.bind(style)

type HeaderProps = Pick<
    WorkStationConfig<string>,
    "title" | "stepsDescribe" | "extraTitleInfo"
>

const WorkStationLayoutHeader = (props: HeaderProps) => {
    const history = useHistory()
    const {t} = useTranslation()
    // @ts-ignore
    const {title, extraTitleInfo} = props
    const {store, onActionDispatch} = useWorkStation()
    const {workStationEvent} = store
    const workStationStatus = workStationEvent?.workStationStatus

    const handleExit = () => {
        Modal.confirm({
            title: t("modal.confirmExitStation"),
            okText: t("button.confirmExit"),
            cancelText: t("button.cancel"),
            okButtonProps: {danger: true},
            onOk: async () => {
                await onActionDispatch({eventCode: CustomActionType.OFFLINE})
            }
        })
    }

    useEffect(() => {
        if (workStationStatus === WorkStationStatus.OFFLINE) {
            console.log(
                "%c =====> 当前工作站已下线,重定向回卡片页",
                "color:red;font-size:20px;"
            )
            history.push(STATION_MENU_PATH)
        }
    }, [workStationStatus, history])

    if (workStationStatus === WorkStationStatus.OFFLINE) {
        return null
    }

    const formatTitle =
        typeof title === "function"
            ? title(workStationEvent)
            : title
    const extraInfo =
        typeof extraTitleInfo === "function"
            ? extraTitleInfo?.(workStationEvent)
            : extraTitleInfo

    return (
        <div
            className="d-flex content-center justify-between pb-3"
            style={{borderBottom: "1px solid #E4E4E4"}}
        >
            <span
                className="font-bold text-xl"
            >
                <span className="mr-5">
                    {t("station.operatingStation")}&nbsp;
                    {workStationEvent?.stationCode}
                </span>
                <span>{formatTitle}</span>
                <span
                    className="d-flex mr-4 text-md	font-bold text-current"
                >
                    {extraInfo}
                </span>
            </span>
            <button
                onClick={handleExit}
                style={{
                    padding: "5px 14px",
                    background: "#fff",
                    border: "1px solid #d1d5db",
                    borderRadius: 6,
                    fontSize: 13,
                    color: "#374151",
                    cursor: "pointer",
                    flexShrink: 0
                }}
            >
                {t("station.exitStation")}
            </button>
        </div>
    )
}

export default observer(WorkStationLayoutHeader)
