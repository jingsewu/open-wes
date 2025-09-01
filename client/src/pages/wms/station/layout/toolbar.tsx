import Icon from "@ant-design/icons"
import {message as Message} from "antd"
import {debounce} from "lodash"
import history from "history/browser"
import classNames from "classnames/bind"
import type {FunctionComponent} from "react"
import React, {createElement, memo, useContext, useRef, useState} from "react"
import {DEBOUNCE_TIME} from "@/pages/wms/station/constants/constant"
import {WorkStationContext} from "@/pages/wms/station/event-loop/provider"
import type {ToastFn, WorkStationConfig} from "@/pages/wms/station/instances/types"
import ActionHandler from "@/pages/wms/station/tab-actions"
import type {EmitterPayload, TabAction} from "@/pages/wms/station/tab-actions/types"
import {TabActionModalType} from "@/pages/wms/station/tab-actions/types"

import {APIContext, OperationsContext} from "../event-loop/provider"
import Modal, {useWorkStationModal} from "../widgets/modal"
import styles from "./styles.module.scss"

const cx = classNames.bind(styles)

interface FooterProps {
    actions: WorkStationConfig<string>["actions"]
}

const actionHandler = new ActionHandler()

const WorkStationLayoutToolbar = (props: FooterProps) => {
    // const history = props
    const {workStationEvent} = useContext(WorkStationContext)
    const [isModalFullScreen, setIsModalFullScreen] = useState(false)
    const [isModalVisible, setModalVisible] = useState(false)
    const [confirmLoading, setConfirmLoading] = useState(false)
    const [modalContent, setModalContent] = useState<
        FunctionComponent<EmitterPayload> | undefined
    >(undefined)
    const [modalConfig, setModalConfig] = useState<TabAction["modalConfig"]>({})
    const contentRef = useRef()
    const {confirm, contextHolder} = useWorkStationModal()
    const [api, msgContextHolder] = Message.useMessage()
    const message: ToastFn = (props) => {
        const {type, content, duration = 3} = props
        api[type](content, duration)
    }
    const {onActionDispatch} = useContext(APIContext)
    const {operationsMap} = useContext(OperationsContext)
    const {actions} = props
    const actionsList =
        typeof actions === "function"
            ? actions(workStationEvent)
            : actions
    // const currentActions =  returnActions(actionsList)
    const currentActions = actionsList
    const resetModal = () => {
        setModalConfig({})
        setModalVisible(false)
        setModalContent(undefined)
        setIsModalFullScreen(false)
        setConfirmLoading(false)
    }

    return (
        <>
            <Modal
                {...modalConfig}
                confirmLoading={confirmLoading}
                destroyOnClose={true}
                visible={isModalVisible}
                fullScreen={isModalFullScreen}
            >
                {modalContent &&
                    createElement(modalContent, {
                        resetModal,
                        operationsMap,
                        onActionDispatch,
                        history,
                        message,
                        refs: contentRef,
                        setModalVisible,
                        setConfirmLoading,
                        loading: confirmLoading,
                        workStationEvent
                    })}
            </Modal>
            <div
                className="w-full h-16  pt-2.5 gap-x-2"
            >
                {currentActions?.map((action) => {
                    let tabConfig

                    if (typeof action === "string") {
                        tabConfig = actionHandler.get(action)
                    } else {
                        tabConfig = action
                        let originTabConfig
                        if (actionHandler.has(action.key)) {
                            originTabConfig = actionHandler.get(
                                action.key as string
                            )
                        }
                        tabConfig = {...originTabConfig, ...action}
                    }
                    if (!tabConfig) return null

                    const {
                        key,
                        name,
                        icon,
                        position = "left",
                        modalType,
                        emitter,
                        content,
                        modalConfig = {},
                        onSubmit,
                        disabled,
                        hide,
                        testid
                    } = tabConfig as TabAction

                    const isDisabled = (): boolean => {
                        if (!disabled) return false
                        if (typeof disabled === "function") {
                            return disabled(workStationEvent)
                        }
                        return disabled
                    }

                    const isHide = (): boolean => {
                        if (!hide) return false
                        if (typeof hide === "function") {
                            return hide(workStationEvent)
                        }
                        return hide
                    }

                    const defaultModalConfig: TabAction["modalConfig"] = {
                        onCancel: () => setModalVisible(false)
                    }

                    const handleSubmit = debounce(
                        async () => {
                            const res =
                                onSubmit &&
                                (await onSubmit(contentRef, {
                                    history,
                                    onActionDispatch,
                                    operationsMap,
                                    message,
                                    setModalVisible,
                                    workStationEvent
                                }))
                            if (res) {
                                resetModal()
                            }
                        },
                        DEBOUNCE_TIME,
                        {leading: false}
                    )

                    const handleClick = debounce(
                        () => {
                            if (isDisabled()) return
                            switch (modalType) {
                                case TabActionModalType.NONE:
                                    resetModal()
                                    emitter &&
                                    emitter({
                                        history,
                                        onActionDispatch,
                                        operationsMap,
                                        message,
                                        setModalVisible,
                                        workStationEvent
                                    })
                                    break
                                case TabActionModalType.CONFIRM:
                                    resetModal()
                                    confirm({
                                        title: "确认提交？",
                                        // <IntlMessages id="inventoryManagement.confirm.submit" />
                                        onOk() {
                                            emitter &&
                                            emitter({
                                                history,
                                                onActionDispatch,
                                                operationsMap,
                                                message,
                                                setModalVisible,
                                                workStationEvent
                                            })
                                        },
                                        ...modalConfig
                                    })
                                    break
                                case TabActionModalType.FULL_SCREEN:
                                    resetModal()
                                    setIsModalFullScreen(true)
                                    setModalContent(() => content)
                                    setModalConfig({
                                        ...defaultModalConfig,
                                        ...modalConfig,
                                        onOk: handleSubmit
                                    })
                                    emitter &&
                                    emitter({
                                        history,
                                        onActionDispatch,
                                        operationsMap,
                                        message,
                                        setModalVisible,
                                        workStationEvent,
                                    })
                                    break
                                case TabActionModalType.NORMAL:
                                    resetModal()
                                    setModalContent(() => content)
                                    setModalConfig({
                                        ...defaultModalConfig,
                                        ...modalConfig,
                                        onOk: handleSubmit
                                    })
                                    emitter &&
                                    emitter({
                                        history,
                                        onActionDispatch,
                                        operationsMap,
                                        message,
                                        setModalVisible,
                                        workStationEvent
                                    })
                                    break
                                default:
                                    break
                            }
                        },
                        DEBOUNCE_TIME,
                        {leading: false}
                    )

                    return !isHide() ? (
                        <div
                            className={cx(
                                `toolbarBtn pull-${position} ${
                                    isDisabled() ? "disabled" : ""
                                }`
                            )}
                            onClick={handleClick}
                            key={key}
                            data-testid={testid}
                        >
                            <span style={{marginRight: 4}}>
                                <Icon component={() => icon}/>
                            </span>
                            {name}
                        </div>
                    ) : null
                })}
            </div>
            {contextHolder}
            {msgContextHolder}
        </>
    )
}

export default memo(WorkStationLayoutToolbar)
