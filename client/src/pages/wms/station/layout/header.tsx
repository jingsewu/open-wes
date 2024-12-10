import React, { useContext } from "react"
import { useHistory } from "react-router"
import { useTranslation } from "react-i18next"
import classNames from "classnames/bind"
import { WorkStationContext } from "../event-loop/provider"
import style from "./styles.module.scss"
import { WorkStationStatus } from "@/pages/wms/station/event-loop/types"
import { STATION_MENU_PATH } from "@/pages/wms/station/constant"
import type { WorkStationConfig } from "@/pages/wms/station/instances/types"
const cx = classNames.bind(style)

type HeaderProps = Pick<
    WorkStationConfig<string>,
    "title" | "stepsDescribe" | "extraTitleInfo"
>

const WorkStationLayoutHeader = (props: HeaderProps) => {
    const history = useHistory()
    const { t } = useTranslation()
    // @ts-ignore
    const { title, extraTitleInfo } = props
    const { workStationEvent, workStationInfo } = useContext(WorkStationContext)

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
            ? title(workStationInfo, workStationEvent)
            : title
    const extraInfo =
        typeof extraTitleInfo === "function"
            ? extraTitleInfo?.(workStationInfo, workStationEvent)
            : extraTitleInfo

    return (
        <div
            className="d-flex content-center justify-between pb-3"
            style={{ borderBottom: "1px solid #E4E4E4" }}
            // className={cx("header")}
        >
            <span
                className="font-bold text-xl"
                // className={cx("title")}
            >
                <span className="mr-5">
                    {t("station.operatingStation")}&nbsp;
                    {/* <IntlMessages id="basicInfo.printConfig.column.workstation" /> */}
                    {workStationEvent?.stationCode}
                </span>
                <span>{formatTitle}</span>
                <span
                    className="d-flex mr-4 text-md	font-bold text-current"
                    // className={cx("extra-title-info")}
                >
                    {extraInfo}
                </span>
            </span>
        </div>
    )
}

export default WorkStationLayoutHeader
