import React, { useState } from "react"
import { Translation } from "react-i18next"
import { useHistory } from "react-router"
import { Button, Dropdown, Menu, Space, Select } from "antd"
import { DownOutlined } from "@ant-design/icons"
import type { MenuProps } from "antd"
import Icon from "@ant-design/icons"

import store from "@/stores"
import LogoSvg from "@/icon/icon_logo/wes.svg"

import Language from "@/pages/components/Language"
import ChangePasswordForm from "@/pages/components/ChangePassword"

const items: MenuProps["items"] = [
    {
        label: <Translation>{(t) => t("button.changePassword")}</Translation>,
        key: "changePassword"
    },
    {
        label: <Translation>{(t) => t("button.exit")}</Translation>,
        key: "logout"
    }
]

interface Option {
    value: string
    label: string
}

interface HeaderProps {
    selectedApp: string
    applications: MenuProps["items"]
    selectedWarehouse: string
    warehouses: Option[]
    onApplicationChange: (params: any) => void
    onWarehouseChange: (params: any) => void
    onLanguageChange: (params: any) => void
}

const Header = ({
    selectedApp,
    applications,
    selectedWarehouse,
    warehouses,
    onApplicationChange,
    onWarehouseChange,
    onLanguageChange
}: HeaderProps) => {
    const history = useHistory()
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false)

    const logout = () => {
        store.user.logout()
        history.replace(`/login`)
    }

    const handleMenuClick: MenuProps["onClick"] = (e) => {
        if (e.key === "logout") {
            logout()
        }
        if (e.key === "changePassword") {
            setIsModalOpen(true)
        }
    }

    const handleModalCancel = () => {
        setIsModalOpen(false)
    }

    return (
        <div>
            <div className={`cxd-Layout-brandBar`}>
                <button
                    onClick={store.toggleOffScreen}
                    className="pull-right visible-xs"
                >
                    <i className="fa fa-bars text-white" />
                </button>
                <div
                    className={`cxd-Layout-brand d-flex juftify-center items-center shadow`}
                    style={{ height: 50 }}
                >
                    <Icon
                        component={() => <LogoSvg />}
                        style={{
                            fontSize: "32px",
                            color: "#fff"
                        }}
                    />
                    <span className="text-xl font-bold pl-3">OPEN-WES</span>
                </div>
            </div>
            <div className={`cxd-Layout-headerBar pt-2`}>
                <div className="nav navbar-nav hidden-xs pull-left ">
                    <Button
                        className="no-shadow navbar-btn"
                        type="link"
                        onClick={store.toggleAsideFolded}
                    >
                        <i
                            className={
                                store.asideFolded
                                    ? "fa fa-indent"
                                    : "fa fa-outdent"
                            }
                        />
                    </Button>
                </div>

                <div className="nav navbar-nav hidden-xs pull-left">
                    <Menu
                        onClick={onApplicationChange}
                        selectedKeys={[selectedApp]}
                        mode="horizontal"
                        items={applications}
                        style={{
                            // background: "#fff",
                            marginTop: "-5px",
                            borderBottom: "none"
                        }}
                    />
                </div>

                <div className="m-l-auto hidden-xs pull-right">
                    {selectedApp === "wms" && (
                        <Select
                            placeholder="select warehouse"
                            optionFilterProp="children"
                            onChange={onWarehouseChange}
                            value={selectedWarehouse}
                            options={warehouses}
                        />
                    )}
                    <Language onLanguageChange={onLanguageChange} />
                    <Dropdown
                        menu={{
                            items,
                            onClick: handleMenuClick
                        }}
                        trigger={["click"]}
                    >
                        <Button type="primary" shape="round">
                            <Space>
                                {store.user.name}
                                <DownOutlined />
                            </Space>
                        </Button>
                    </Dropdown>
                    <ChangePasswordForm
                        isModalOpen={isModalOpen}
                        onModalCancel={handleModalCancel}
                    />
                </div>
            </div>
        </div>
    )
}

export default Header
