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

    const handleExit = () => {
        Modal.confirm({
            title: t("modal.confirmExitStation"),
            okText: t("button.confirmExit"),
            cancelText: t("button.cancel"),
            okButtonProps: {danger: true},
            onOk: async () => {
                await onActionDispatch({eventCode: CustomActionType.OFFLINE})
                // 后端已处理下线（HTTP 200），直接重置前端状态并导航回卡片页。
                // 不依赖 MobX 观察者 + useEffect 的异步链来触发跳转。
                store.reset()
                history.push(STATION_MENU_PATH)
            }
        })
    }

    useEffect(() => {
        if (store.workStationStatus === WorkStationStatus.OFFLINE) {
            history.push(STATION_MENU_PATH)
        }
    }, [store.workStationStatus, history])

    if (store.workStationStatus === WorkStationStatus.OFFLINE) {
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
        <div className="workstation-header">
            <div className="workstation-header__identity">
                <span className="workstation-header__station">
                    {t("station.operatingStation")}
                    {workStationEvent?.stationCode && (
                        <b>{workStationEvent.stationCode}</b>
                    )}
                </span>
                <span
                    className="workstation-header__divider"
                    aria-hidden="true"
                />
                <span className="workstation-header__title">
                    {formatTitle}
                </span>
                {extraInfo && (
                    <span className="workstation-header__extra">
                        {extraInfo}
                    </span>
                )}
            </div>
            <button
                type="button"
                className="workstation-header__exit"
                onClick={handleExit}
            >
                {t("station.exitStation")}
            </button>
        </div>
    )
}

export default observer(WorkStationLayoutHeader)
