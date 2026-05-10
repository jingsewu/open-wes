import React from "react"
import { Button, Dropdown } from "antd"
import { GlobalOutlined } from "@ant-design/icons"
import { useTranslation } from "react-i18next"
import type { MenuProps } from "antd"
import store from "@/stores"

export const languageList = [
    {
        label: "中文",
        value: "zh-CN",
        locale: "cn"
    },
    {
        label: "English",
        value: "en-US",
        locale: "en"
    }
]

const languageMenuItems: MenuProps["items"] = languageList.map((o) => ({
    key: o.value,
    label: (
        <span style={{ display: "flex", alignItems: "center", gap: 8 }}>
            <span
                style={{
                    fontSize: 10,
                    fontWeight: 700,
                    fontFamily: "monospace",
                    color: "#fff",
                    background: "#6b7280",
                    borderRadius: 3,
                    padding: "1px 4px",
                    lineHeight: 1.4,
                    flexShrink: 0
                }}
            >
                {o.value === "zh-CN" ? "中" : "EN"}
            </span>
            {o.label}
        </span>
    )
}))

const Language = ({ onLanguageChange }: any) => {
    const { i18n } = useTranslation()

    const handleClick: MenuProps["onClick"] = ({ key }) => {
        const option = languageList.find((o) => o.value === key)
        if (!option) return
        store.toggleLanguage(option)
        i18n.changeLanguage(option.value)
        onLanguageChange && onLanguageChange(option)
    }

    return (
        <Dropdown
            menu={{ items: languageMenuItems, onClick: handleClick, selectedKeys: [store.locale] }}
            trigger={["click"]}
            overlayStyle={{
                borderRadius: 8,
                overflow: "hidden",
                boxShadow: "0 8px 24px rgba(0,0,0,.12)",
                minWidth: 128
            }}
        >
            <Button
                type="text"
                icon={<GlobalOutlined style={{ fontSize: 16, color: "#6b7280" }} />}
                style={{ width: 32, height: 32, padding: 0, borderRadius: 6 }}
            />
        </Dropdown>
    )
}

export default Language
