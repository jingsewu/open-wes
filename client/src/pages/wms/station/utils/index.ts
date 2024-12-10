interface SpeakConfigType {
    /** 要合成的文字内容，字符串 */
    text: string
    /** 读取文字的语速 0.1~10  正常1 */
    speechRate?: number
    /** 读取文字时的语言 */
    lang?: string
    /** 读取时声音的音量 0~1  正常1 */
    volume?: number
    /** 读取时声音的音高 0~2  正常1 */
    pitch?: number
}

export const speak = (
    { text, speechRate = 1, lang, volume = 1, pitch = 1 }: SpeakConfigType,
    startEvent?: () => void,
    endEvent?: () => void
): string => {
    if (!window.SpeechSynthesisUtterance) {
        return "当前浏览器不支持文字转语音服务"
    }
    if (!text) {
        return "无内容"
    }
    const languageId =
        JSON.parse(localStorage.getItem("locale") || "{}")?.languageId || ""
    const localLang = languageId.replace("_", "-")
    // 防止前面语音执行失败
    window.speechSynthesis.cancel()
    const speechUtterance = new SpeechSynthesisUtterance()
    speechUtterance.text = text
    speechUtterance.rate = speechRate
    speechUtterance.lang = lang || localLang || "zh-CN"
    speechUtterance.volume = volume
    speechUtterance.pitch = pitch
    speechUtterance.onstart = function () {
        startEvent && startEvent()
    }
    speechUtterance.onend = function () {
        endEvent && endEvent()
    }
    window.speechSynthesis.speak(speechUtterance)
    return "success"
}

// 获取商品属性字段
export const getSkuDisplayProperty = (property: any[], detailInfo: any) => {
    return (
        property
            ?.filter((item) => item.showTrueOrFalse === "TRUE")
            .map((k) => {
                // 有枚举值的字段，取枚举值的商品显示
                if (k.hasChild === "TRUE") {
                    const info = k.childDisplayProperty.find(
                        (v: any) =>
                            v.showTrueOrFalse === "TRUE" &&
                            v.propertyName === detailInfo?.[k.propertyName]
                    )
                    return info
                        ? {
                              ...info,
                              propertyDesc: k.propertyDesc,
                              propertyValue: info?.propertyDesc
                          }
                        : { ...k, propertyValue: detailInfo?.[k.propertyName] }
                }
                return { ...k, propertyValue: detailInfo?.[k.propertyName] }
            }) || []
    )
}
