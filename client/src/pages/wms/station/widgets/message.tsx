import { message } from "antd"
import type { ReactNode } from "react"

export enum MessageType {
    SUCCESS = "success",
    ERROR = "error",
    WARNING = "warning"
}

export interface MessageProps {
    type: MessageType
    content: string | ReactNode
    duration?: number
}

const Message = (props: MessageProps): void => {
    const { type, content, duration = 3 } = props
    message[type]({
        content,
        duration,
        style: { marginTop: "5vh" }
    })
}

export default Message
