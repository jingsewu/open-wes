import type { FC, ReactNode, RefObject } from "react"
import React, { useRef, useEffect, useCallback } from "react"
import { debounce } from "lodash"

import { ModalType } from "@/pages/wms/station/instances/outbound/operations/tips/type"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import { TabActionModalType } from "@/pages/wms/station/tab-actions/types"
import message, { MessageType } from "@/pages/wms/station/widgets/message"
import Modal from "@/pages/wms/station/widgets/modal"
import { speak } from "../utils"
import { DEBOUNCE_TIME } from "@/pages/wms/station/constant"

export interface Config {
    component: FC<any> | ReactNode
    handleSubmit?: (ref: RefObject<any>) => Promise<any>
    modalType: TabActionModalType.NORMAL | TabActionModalType.FULL_SCREEN
    modalConfig?: TabAction["modalConfig"]
}

export interface ConfigControlledModalProps {
    config: Config
    handleClose?: () => void
    handleVoiceClose?: (result?: string) => void
    contentValue?: any
    visible?: boolean
    /** 是否开启回车关闭弹窗 */
    byCloseStatus?: boolean
}

const ConfigControlledModal = (props: ConfigControlledModalProps) => {
    const {
        config,
        handleClose,
        handleVoiceClose,
        contentValue = {},
        visible,
        byCloseStatus = false
    } = props
    const { voiceInfo, tipType, tipCode } = contentValue
    const contentRef = useRef(null)
    const Content = config.component

    useEffect(() => {
        if (voiceInfo) {
            const result = speak({ text: voiceInfo })
            handleVoiceClose && handleVoiceClose(result)
        }
    }, [tipType, tipCode, voiceInfo])

    const handleSubmit = debounce(
        async () => {
            const { handleSubmit } = config
            handleSubmit && (await handleSubmit(contentRef))
        },
        DEBOUNCE_TIME,
        { leading: false }
    )
    const modalConfig = config?.modalConfig || {}
    const isFullScreen = config?.modalType === TabActionModalType.FULL_SCREEN

    const showContent = Content ? (
        typeof Content === "function" ? (
            <Content value={contentValue} refs={contentRef} />
        ) : (
            React.cloneElement(Content as any, {
                value: contentValue,
                refs: contentRef
            })
        )
    ) : null

    const onKeyUp = useCallback(
        (e) => {
            if (!byCloseStatus) return

            if (e.keyCode === 13) {
                handleClose && handleClose()
            }
        },
        [byCloseStatus]
    )

    // 监听键盘事件，
    useEffect(() => {
        window.addEventListener("keyup", onKeyUp)
        return () => {
            window.removeEventListener("keyup", onKeyUp)
        }
    }, [byCloseStatus])

    const chooseModal = {
        [ModalType.CONFIRM]: (
            <Modal
                destroyOnClose={true}
                fullScreen={isFullScreen}
                visible={visible}
                onCancel={handleClose}
                onOk={handleSubmit}
                {...modalConfig}
            >
                {showContent}
            </Modal>
        ),
        [ModalType.TIP]: <div>{showContent}</div>
    }

    if (contentValue.type === ModalType.VOICE) {
        return null
    }
    return (
        chooseModal[
            (contentValue.type || ModalType.CONFIRM) as keyof typeof chooseModal
        ] || null
    )
}

export default ConfigControlledModal
