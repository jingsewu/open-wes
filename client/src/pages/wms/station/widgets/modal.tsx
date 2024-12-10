import React from "react"
import type { ModalProps } from "antd"
import { Modal } from "antd"

interface WorkStationModalProps extends ModalProps {
    fullScreen?: boolean
}

const WorkStationModal = (props: WorkStationModalProps) => {
    const { children, fullScreen, ...rest } = props

    return (
        <Modal
            zIndex={1250}
            destroyOnClose={true}
            className={`${fullScreen ? "full-screen" : ""}`}
            {...rest}
        >
            {children}
        </Modal>
    )
}

export const useWorkStationModal = () => {
    const [modal, contextHolder] = Modal.useModal()

    return {
        confirm: modal.confirm,
        contextHolder
    }
}

export default WorkStationModal
