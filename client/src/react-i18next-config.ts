import { registerFilter } from "amis"
import * as i18n from "i18next"
import { initReactI18next } from "react-i18next"
//i18next-browser-languagedetector插件 这是一个 i18next 语言检测插件，用于检测浏览器中的用户语言，
//详情请访问：https://github.com/i18next/i18next-browser-languageDetector
import LanguageDetector from "i18next-browser-languagedetector"
//引入需要实现国际化的中文、英文两种数据的json文件
import * as cn from "./locales/zh-cn.json"
import * as en from "./locales/en-us.json"
const resources = {
    "zh-CN": {
        translation: cn
    },
    "en-US": {
        translation: en
    }
}
i18n.use(LanguageDetector) //嗅探当前浏览器语言 zh-CN
    .use(initReactI18next) // 将 i18n 向下传递给 react-i18next
    .init({
        //初始化
        resources, //本地多语言数据
        fallbackLng: "zh-CN", //默认当前环境的语言
        detection: {
            caches: ["localStorage", "sessionStorage", "cookie"]
        }
    })

registerFilter("t", (input: string) =>
    typeof input === "string" ? i18n.t(input) : ""
)
export default i18n
