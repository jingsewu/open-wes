/**
 * 封箱处理
 */
// import IntlMessages from "@/util/IntlMessages"
import { Result } from "antd"
import React, { useMemo } from "react"
// import { useSelector } from 'react-redux';

const DESTINATION_ENUM_KEY = "SealReminderDestination"

export default function CloseContainer(props: any) {
    const { value } = props
    const { containerCode, destination } = value
    // const { enums } = useSelector(({ enums }: any) => enums);
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

    return (
        <Result
            status="info"
            title="封箱提醒"
            // {<IntlMessages id="workstaion.common.tip.sealingRemind" />}
            extra={
                <div>
                    <div>
                        周转容器号
                        {/* <IntlMessages id="outboundManage.outboundTask.detail.column.turnoverContainerNo" /> */}
                        : {containerCode}
                    </div>
                    <div>
                        目的地
                        {/* <IntlMessages id="ruleManagement.SealingPromptRule.column.destination" /> */}
                        :{formatEnums[destination]?.label || destination}
                    </div>
                </div>
            }
        />
    )
}
