import { configure } from 'mobx'

let isConfigured = false

export const configureMobX = () => {
    if (isConfigured) return
    
    try {
        configure({ 
            enforceActions: "never",
            disableErrorBoundaries: true
        })
        isConfigured = true
        console.log('MobX configured successfully')
    } catch (error) {
        console.warn('MobX configure failed:', error)
    }
}

// 在模块加载时立即配置
configureMobX()
