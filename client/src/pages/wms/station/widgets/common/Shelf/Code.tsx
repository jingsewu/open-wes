import React from "react"
import { useTranslation } from "react-i18next"

interface Iprops {
    containerCode?: string
    face?: string
    level?: string
    bay?: number
}

const Code = (props: Iprops) => {
    const { containerCode, face, level, bay } = props
    const { t } = useTranslation()
    return (
        <div className="w-full mb-3">
            <div
                className="d-flex py-2 w-full mb-0.5 rounded-md"
                style={{ backgroundColor: "#F8F8F8" }}
            >
                <div className="w-1/4 text-center">
                    {t("workLocationArea.container")}
                </div>
                <div className="border-left w-1/4 text-center">
                    {t("workLocationArea.face")}
                </div>
                <div className="border-left w-1/4 text-center">
                    {t("workLocationArea.layer")}
                </div>
                <div className="border-left border-l w-1/4 text-center">
                    {t("workLocationArea.serialNumber")}
                </div>
            </div>
            <div
                className="d-flex py-2 w-full font-bold rounded-md"
                style={{ backgroundColor: "#F8F8F8" }}
            >
                <div className="w-1/4 text-center" data-testid="containerCode">
                    {containerCode || "-"}
                </div>
                <div className="border-left w-1/4 text-center">
                    {face || "-"}
                </div>
                <div className="border-left w-1/4 text-center">
                    {level || "-"}
                </div>
                <div className="border-left border-l w-1/4 text-center">
                    {bay || "-"}
                </div>
            </div>
        </div>
    )
}

export default Code
