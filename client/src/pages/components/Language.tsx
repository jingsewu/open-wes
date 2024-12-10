import React, { useState } from "react"
// import i18n from "@/react-i18next-config"
import { Select } from "amis"
import { useTranslation } from "react-i18next"
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
const Language = ({ onLanguageChange }: any) => {
    const { t, i18n } = useTranslation()

    const changeLanguage = (e: any) => {
        store.toggleLanguage(e)
        i18n.changeLanguage(e.value)
        onLanguageChange && onLanguageChange(e)
    }
    return (
        <Select
            // value={language}
            // onChange={(e: any) => changeLanguage(e)}
            value={store.locale}
            onChange={(e: any) => changeLanguage(e)}
            options={languageList}
            clearable={false}
            borderMode="none"
            className="bg-none"
        />
    )
}

export default Language
