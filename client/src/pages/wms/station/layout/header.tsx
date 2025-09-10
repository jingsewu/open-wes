import React from "react"
import {useHistory} from "react-router"
import {useTranslation} from "react-i18next"
import classNames from "classnames/bind"
import {useWorkStation} from "../state"
import style from "./styles.module.scss"
import {WorkStationStatus} from "@/pages/wms/station/event-loop/types"
import {STATION_MENU_PATH} from "@/pages/wms/station/constants/constant"
import type {WorkStationConfig} from "@/pages/wms/station/instances/types"

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
    const {store} = useWorkStation()
    const {workStationEvent} = store

    if (workStationEvent?.workStationStatus === WorkStationStatus.OFFLINE) {
        console.log(
            "%c =====> 当前工作站已下线,重定向回卡片页",
            "color:red;font-size:20px;"
        )
        history.push(STATION_MENU_PATH)
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
        </div>
    )
}

export default WorkStationLayoutHeader
