import { Result } from "antd"
import React, { useMemo } from "react"
import {useTranslation} from "react-i18next";

const DESTINATION_ENUM_KEY = "SealReminderDestination"

export default function CloseContainer(props: any) {
    const { value } = props
    const { containerCode, destination } = value
    const enums = { [DESTINATION_ENUM_KEY]: [] }
    const formatEnums = useMemo(() => {
        const res: any = {}
        if (enums[DESTINATION_ENUM_KEY]) {
            enums[DESTINATION_ENUM_KEY].forEach((item: any) => {
                res[item.enumValue] = {
                    label: item.label,
                    value: item.enumValue
                }
            })
        }
        return res
    }, [enums])

    const { t } = useTranslation();

    return (
        <Result
            status="info"
            title={t("outbound.station.sealContainer.remainder.title")}
            extra={
                <div>
                    <div>
                        {t("outbound.station.sealContainer.remainder.transferContainer.code") }
                        : {containerCode}
                    </div>
                    <div>
                        {t("outbound.station.sealContainer.remainder.destination") }
                        :{formatEnums[destination]?.label || destination}
                    </div>
                </div>
            }
        />
    )
}
