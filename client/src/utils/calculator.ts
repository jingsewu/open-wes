// 匹配规则
interface Calculator {
    "+": (x: number | string, y: number) => number
    "-": (x: number | string, y: number) => number
    "*": (x: number | string, y: number) => number
    "/": (x: number | string, y: number) => number
}

// 小数点校验规则
interface Rule {
    baseDesc?: string // 基数描述 如：mg
    base: number // 转换基数 如：100
    calculation: "+" | "-" | "*" | "/" // 转换规则 如：/，最终得到的就是如 1000 / 100 = 10，页面展示的就是10
    displaySuffix?: string // 显示后缀 如：mg
    keepDecimalPlaces?: number // 小数点位数限制  可能是5/8/10等进制，所以小数点后位数只能后端传递决定
}

// 计算匹配 x：当前值 y：base值
const calculator: Calculator = {
    "+": (x: number | string, y: number): number =>
        Number(x ?? 0) + Number(y ?? 0),
    "-": (x: number | string, y: number): number =>
        Number(x ?? 0) - Number(y ?? 0),
    "*": (x: number | string, y: number): number =>
        Number(x ?? 0) * Number(y ?? 0),
    "/": (x: number | string, y: number): number =>
        Number(x ?? 0) / (y === 0 ? 1 : Number(y)) // 分母不能为零
}

// 小数反转计算器 x：当前值(要反转的) y：base值
const reCalculator: Calculator = {
  '+': (x: number | string, y: number): number => Number(x ?? 0) - Number(y ?? 0),
  '-': (x: number | string, y: number): number => Number(x ?? 0) + Number(y ?? 0),
  '*': (x: number | string, y: number): number => Number(x ?? 0) / (y === 0 ? 1 : Number(y)), // 分母不能为零
  '/': (x: number | string, y: number): number => Number(x ?? 0) * Number(y ?? 0),
};

// 计算器  count：当前值(要反转的) display：计算规则  reverse：是否需要反转计算
const stationQtyCalculator = (
    count: number | string,
    display?: Rule,
    reverse?: boolean
): number | string => {
    try {
        // 没有则不需要计算，减少开销
        if (
            (!count && Number(count) !== 0) ||
            !display ||
            !display?.base ||
            !display?.calculation ||
            !display?.keepDecimalPlaces
        ) {
            return count
        }
        const { base, calculation, keepDecimalPlaces } = display
        if (Number(count) === 0) {
            const z = 0
            return reverse ? 0 : z.toFixed(keepDecimalPlaces)
        }
        const res: string | number = reverse
            ? reCalculator[calculation](count, base)
            : calculator[calculation](count, base).toFixed(keepDecimalPlaces) // 保留固定小数位 末尾为0也要保留
        return reverse ? Number(Number(res).toFixed(0)) : res
    } catch (error) {
        return count
    }
}

export default stationQtyCalculator
