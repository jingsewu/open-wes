import { configure } from 'mobx'

let isConfigured = false

export const configureMobX = () => {
    if (isConfigured) return
    
    try {
        configure({
            enforceActions: "never"
            // Do NOT enable disableErrorBoundaries: MobX 4's computed
            // evaluation skips its computationDepth-- when the derivation
            // throws, leaving the global depth stuck and making every later
            // observable write throw an invariant.
        })
        isConfigured = true
        console.log('MobX configured successfully')
    } catch (error) {
        console.warn('MobX configure failed:', error)
    }
}

// 在模块加载时立即配置
configureMobX()
