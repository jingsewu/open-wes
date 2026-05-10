import React, { useState } from "react"
import { useHistory } from "react-router"
import { useTranslation } from "react-i18next"
import { Button, Dropdown, Menu, Select } from "antd"
import { DownOutlined, KeyOutlined, LogoutOutlined } from "@ant-design/icons"
import type { MenuProps } from "antd"

import store from "@/stores"
import { workStationEventLoop } from "@/pages/wms/station/event-loop/eventLoopInstance"

import Language from "@/pages/components/Language"
import ChangePasswordForm from "@/pages/components/ChangePassword"

const Divider = () => (
    <div style={{ width: 1, height: 18, background: "#e5e7eb", margin: "0 4px", flexShrink: 0 }} />
)

const WarehouseSelect = ({
    value,
    options,
    onChange
}: {
    value: string
    options: { value: string; label: string }[]
    onChange: (v: any) => void
}) => {
    const [hovered, setHovered] = useState(false)
    return (
        <div
            style={{
                display: "flex",
                alignItems: "center",
                gap: 6,
                padding: "4px 10px",
                borderRadius: 6,
                background: hovered ? "#f3f4f6" : "transparent",
                transition: "background 0.15s",
                cursor: "pointer"
            }}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
        >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="2">
                <path d="M3 9l9-7 9 7v11a2 2 0 01-2 2H5a2 2 0 01-2-2z" />
                <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
            <Select
                bordered={false}
                value={value}
                options={options}
                onChange={onChange}
                dropdownMatchSelectWidth={false}
                suffixIcon={<DownOutlined style={{ fontSize: 10, color: "#9ca3af" }} />}
                style={{ fontSize: 13, fontWeight: 500, color: "#374151", padding: 0 }}
                dropdownStyle={{
                    borderRadius: 8,
                    boxShadow: "0 8px 24px rgba(0,0,0,.12)",
                    padding: "4px 0",
                    minWidth: 180
                }}
            />
        </div>
    )
}

const UserTrigger = ({ name, ...rest }: { name: string } & React.HTMLAttributes<HTMLDivElement>) => {
    const [hovered, setHovered] = useState(false)
    const initial = name?.[0]?.toUpperCase() ?? "U"
    return (
        <div
            {...rest}
            style={{
                display: "flex",
                alignItems: "center",
                gap: 7,
                padding: "4px 8px",
                borderRadius: 6,
                background: hovered ? "#f3f4f6" : "transparent",
                transition: "background 0.15s",
                cursor: "pointer"
            }}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
        >
            <div
                style={{
                    width: 26,
                    height: 26,
                    borderRadius: "50%",
                    flexShrink: 0,
                    background: "linear-gradient(135deg, #2563eb, #4f46e5)",
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    color: "#fff",
                    fontSize: 11,
                    fontWeight: 700
                }}
            >
                {initial}
            </div>
            <span style={{ fontSize: 13, fontWeight: 500, color: "#374151" }}>{name}</span>
            <DownOutlined style={{ fontSize: 10, color: "#9ca3af" }} />
        </div>
    )
}

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
    const { t } = useTranslation()
    const [isModalOpen, setIsModalOpen] = useState<boolean>(false)

    const logout = () => {
        workStationEventLoop.destroy()
        store.user.logout()
        history.replace(`/login`)
    }

    const handleModalCancel = () => {
        setIsModalOpen(false)
    }

    const userDropdownPanel = (
        <div
            style={{
                background: "#fff",
                borderRadius: 8,
                boxShadow: "0 8px 24px rgba(0,0,0,.12)",
                overflow: "hidden",
                minWidth: 168
            }}
        >
            {/* User info header */}
            <div
                style={{
                    padding: "12px 16px",
                    display: "flex",
                    alignItems: "center",
                    gap: 10,
                    borderBottom: "1px solid #f3f4f6"
                }}
            >
                <div
                    style={{
                        width: 32,
                        height: 32,
                        borderRadius: "50%",
                        flexShrink: 0,
                        background: "linear-gradient(135deg, #2563eb, #4f46e5)",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        color: "#fff",
                        fontSize: 13,
                        fontWeight: 700
                    }}
                >
                    {store.user.name?.[0]?.toUpperCase() ?? "U"}
                </div>
                <span style={{ fontSize: 13, fontWeight: 500, color: "#374151" }}>{store.user.name}</span>
            </div>
            {/* Actions */}
            <div style={{ padding: "4px 0" }}>
                <DropdownItem icon={<KeyOutlined />} onClick={() => setIsModalOpen(true)}>
                    {t("button.changePassword")}
                </DropdownItem>
                <DropdownItem icon={<LogoutOutlined />} onClick={logout} danger>
                    {t("button.exit")}
                </DropdownItem>
            </div>
        </div>
    )

    return (
        <>
            {/* brandBar is hidden via CSS (display:none) — kept so AMIS layout structure is intact */}
            <div className="cxd-Layout-brandBar" />

            <div className="cxd-Layout-headerBar">
                {/* ── Logo zone — visually aligns with sidebar width ── */}
                <div
                    style={{
                        width: "var(--layout-aside-width)",
                        height: "100%",
                        display: "flex",
                        alignItems: "center",
                        padding: "0 16px",
                        gap: 8,
                        flexShrink: 0,
                        borderRight: "1px solid #e5e7eb"
                    }}
                >
                    <svg width="34" height="34" viewBox="0 0 34 34" fill="none" xmlns="http://www.w3.org/2000/svg">
                        <defs>
                            <linearGradient id="logoGrad" x1="0" y1="0" x2="1" y2="1">
                                <stop offset="0%" stopColor="#2563eb" />
                                <stop offset="100%" stopColor="#4f46e5" />
                            </linearGradient>
                        </defs>
                        <rect width="34" height="34" rx="9" fill="url(#logoGrad)" />
                        <text x="17" y="23.5" textAnchor="middle" fill="white" fontSize="17" fontWeight="900" fontFamily="Arial,sans-serif">W</text>
                    </svg>
                    <span style={{ fontSize: 15, whiteSpace: "nowrap" }}>
                        <span style={{ fontWeight: 500, color: "#374151" }}>Open</span>
                        <span style={{ fontWeight: 800, color: "#2563eb" }}>WES</span>
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
                <div style={{ marginLeft: "auto", display: "flex", alignItems: "center", gap: 2, paddingRight: 16 }}>
                    {selectedApp === "wms" && (
                        <>
                            <WarehouseSelect
                                value={selectedWarehouse}
                                options={warehouses}
                                onChange={onWarehouseChange}
                            />
                            <Divider />
                        </>
                    )}
                    <Language onLanguageChange={onLanguageChange} />
                    <Divider />
                    <Dropdown dropdownRender={() => userDropdownPanel} trigger={["click"]}>
                        <UserTrigger name={store.user.name} />
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

const DropdownItem = ({
    icon,
    children,
    onClick,
    danger
}: {
    icon: React.ReactNode
    children: React.ReactNode
    onClick?: () => void
    danger?: boolean
}) => {
    const [hovered, setHovered] = useState(false)
    const color = danger ? "#ef4444" : "#374151"
    const hoverBg = danger ? "#fef2f2" : "#f9fafb"
    return (
        <div
            onClick={onClick}
            onMouseEnter={() => setHovered(true)}
            onMouseLeave={() => setHovered(false)}
            style={{
                padding: "8px 16px",
                fontSize: 13,
                color,
                cursor: "pointer",
                display: "flex",
                alignItems: "center",
                gap: 8,
                background: hovered ? hoverBg : "transparent",
                transition: "background 0.15s"
            }}
        >
            <span style={{ fontSize: 14, color: danger ? "#ef4444" : "#9ca3af" }}>{icon}</span>
            {children}
        </div>
    )
}

export default Header
