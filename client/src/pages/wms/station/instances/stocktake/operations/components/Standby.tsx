import React from "react"
import {useTranslation} from "react-i18next"

export type StandbyStatus =
    | "connecting"
    | "waitRobot"
    | "callContainer"
    | "preparing"
    | "waitContainer"
    | "noTask"

interface StandbyProps {
    status: StandbyStatus
}

const COPY: Record<StandbyStatus, {title: string; hint: string}> = {
    connecting: {
        title: "stocktake.station.standby.connecting",
        hint: "stocktake.station.standby.connectingHint"
    },
    waitRobot: {
        title: "stocktake.station.standby.waitRobot",
        hint: "stocktake.station.standby.waitRobotHint"
    },
    callContainer: {
        title: "stocktake.station.standby.callContainer",
        hint: "stocktake.station.standby.callContainerHint"
    },
    preparing: {
        title: "stocktake.station.standby.preparing",
        hint: "stocktake.station.standby.preparingHint"
    },
    waitContainer: {
        title: "stocktake.station.standby.waitContainer",
        hint: "stocktake.station.standby.waitContainerHint"
    },
    noTask: {
        title: "stocktake.station.standby.noTask",
        hint: "stocktake.station.standby.noTaskHint"
    }
}

const Standby = (props: StandbyProps) => {
    const {status} = props
    const {t} = useTranslation()
    const copy = COPY[status]

    return (
        <section className="stocktake-standby">
            <div className="stocktake-standby__panel">
                <div className="stocktake-standby__art" aria-hidden="true">
                    <svg viewBox="0 0 200 150" fill="none">
                        <rect
                            x="22"
                            y="22"
                            width="156"
                            height="106"
                            rx="12"
                            stroke="currentColor"
                            strokeWidth="2"
                        />
                        <path
                            d="M22 58h156M22 94h156"
                            stroke="currentColor"
                            strokeOpacity="0.45"
                            strokeWidth="2"
                        />
                        <path
                            d="M61 22v106M100 22v106M139 22v106"
                            stroke="currentColor"
                            strokeOpacity="0.25"
                            strokeWidth="2"
                        />
                        <rect
                            x="66"
                            y="62"
                            width="68"
                            height="28"
                            rx="5"
                            stroke="#f59e0b"
                            strokeWidth="2"
                            strokeDasharray="6 5"
                        />
                        <circle cx="100" cy="76" r="3" fill="#f59e0b" />
                    </svg>
                </div>
                <div className="stocktake-standby__copy">
                    <h2 className="stocktake-standby__title">
                        {t(copy.title)}
                    </h2>
                    <p className="stocktake-standby__hint">{t(copy.hint)}</p>
                </div>
            </div>
        </section>
    )
}

export default Standby
