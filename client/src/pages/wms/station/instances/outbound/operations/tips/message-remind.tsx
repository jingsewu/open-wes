import { useEffect } from "react"

import { MessageType } from "@/pages/wms/station/widgets/message"

export default function MessageRemindHandler(props: any) {
    const { value } = props
    const { message, msg, duration, onActionDispatch, tipType } = value

    useEffect(() => {
        if (!msg) return

        message?.({
            type: MessageType.WARNING,
            content: msg,
            duration: duration / 1000
        })

        onActionDispatch &&
            onActionDispatch({
                eventCode: "CLOSE_TIP",
                data: {
                    tipType
                }
            })
    }, [msg])
    return null
}
