import defaultConfigs from "./action-configs"
import type { TabAction } from "@/pages/wms/station/tab-actions/types"
import type { TabActionType } from "@/pages/wms/station/tab-actions/constant"

class TabActionHandler {
    private registry: Map<string, TabAction>

    public constructor() {
        this.registry = new Map()
        this.init()
    }

    /**
     * 注册自定义tabAction
     */
    public use(customAction: TabAction) {
        const { key } = customAction
        let originAction = {} as unknown as TabAction
        if (this.registry.has(key)) {
            console.warn(
                `%c Attention: 原有动作${
                    this.registry.get(key)?.name
                }被合并覆盖`,
                "color:red;font-size:20px;"
            )
            originAction = this.registry.get(key) as TabAction
        }
        this.registry.set(key, {
            ...originAction,
            ...customAction
        })
    }

    public get(key: string | TabActionType): TabAction | undefined {
        if (!this.registry.has(key)) {
            throw new Error("tab action 需要使用use方法注册再使用")
        }
        return this.registry.get(key)
    }

    public has(key: string | undefined): boolean {
        if (!key) return false

        return this.registry.has(key)
    }

    /**
     * 加载默认tabActions
     */
    private init() {
        Object.keys(defaultConfigs).forEach((key) => {
            this.use(defaultConfigs[key as keyof typeof defaultConfigs])
        })
    }
}

export default TabActionHandler
