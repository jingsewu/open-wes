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
        <>
            {/* brandBar is hidden via CSS (display:none) — kept so AMIS layout structure is intact */}
            <div className="cxd-Layout-brandBar" />

            <div className="cxd-Layout-headerBar">
                {/* ── Logo zone — visually aligns with sidebar width ── */}
                <div
                    style={{
                        width: 180,
                        height: "100%",
                        display: "flex",
                        alignItems: "center",
                        padding: "0 16px",
                        gap: 8,
                        flexShrink: 0,
                        borderRight: "1px solid #e5e7eb"
                    }}
                >
                    <Icon
                        component={() => <LogoSvg />}
                        style={{ fontSize: "22px", color: "#2563eb" }}
                    />
                    <span style={{ fontSize: 14, fontWeight: 700, color: "#111827", whiteSpace: "nowrap" }}>
                        OPEN-WES
                    </span>
                </div>

                {/* ── Collapse toggle ── */}
                <Button
                    className="no-shadow navbar-btn"
                    type="text"
                    style={{ marginLeft: 8 }}
                    onClick={store.toggleAsideFolded}
                >
                    <i className={store.asideFolded ? "fa fa-indent" : "fa fa-outdent"} />
                </Button>

                {/* ── App navigation ── */}
                <Menu
                    onClick={onApplicationChange}
                    selectedKeys={[selectedApp]}
                    mode="horizontal"
                    items={applications}
                    style={{ borderBottom: "none", background: "transparent", marginTop: 0 }}
                />

                {/* ── Right controls ── */}
                <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 8, paddingRight: 16 }}>
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
                        menu={{ items, onClick: handleMenuClick }}
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
        </>
    )
}

export default Header
