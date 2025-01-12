import dayjs from "dayjs"

// 时间戳格式化-日期
const dateFormat = "YYYY-MM-DD"
// 时间戳格式化-当天
const timeFormat = "HH:mm:ss"
// 时间戳格式化-年月日时间
const dateTimeFormat = `${dateFormat} ${timeFormat}`
export const dateTimeFormatFn = (num: any) =>
    num ? dayjs(Number(num)).format(dateTimeFormat) : null
