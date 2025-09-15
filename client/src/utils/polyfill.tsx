

import "core-js/es6/object.js"
import "core-js/es6/array.js"

import "core-js/es6/symbol.js";
import "core-js/es6/set.js";
import "core-js/es6/map.js";


import 'promise/polyfill';


if (!Element.prototype.matches) {
    // @ts-ignore
    Element.prototype.matches = Element.prototype.msMatchesSelector
    || Element.prototype.webkitMatchesSelector;
}

if (!Element.prototype.closest) {
    Element.prototype.closest = function (s:any) {
        var el:any = this;
        if (!document.documentElement.contains(el)) {
            return null;
        }

        do {
            if (el.matches(s)) {
                return el;
            }
            el = el.parentElement;
        } while (el !== null);
        return null;
    };
}

// 全局 ResizeObserver 错误处理
const resizeObserverErrorHandler = (e: ErrorEvent) => {
  if (e.message === 'ResizeObserver loop completed with undelivered notifications.') {
    // 这是一个已知的 ResizeObserver 错误，通常可以安全忽略
    // 它发生在组件卸载时 ResizeObserver 仍在处理尺寸变化事件
    e.stopImmediatePropagation()
    return false
  }
  return true
}

// 添加全局错误监听器
window.addEventListener('error', resizeObserverErrorHandler)

// 添加未处理的 Promise 拒绝监听器
window.addEventListener('unhandledrejection', (e) => {
  if (e.reason && e.reason.message && e.reason.message.includes('ResizeObserver loop completed with undelivered notifications')) {
    e.preventDefault()
  }
})

// ResizeObserver 管理工具
class ResizeObserverManager {
  private static instance: ResizeObserverManager
  private observers: Map<Element, ResizeObserver> = new Map()
  private collectionObserver: ResizeObserver | null = null

  static getInstance(): ResizeObserverManager {
    if (!ResizeObserverManager.instance) {
      ResizeObserverManager.instance = new ResizeObserverManager()
    }
    return ResizeObserverManager.instance
  }

  // 创建或获取 ResizeObserver 实例
  createObserver(element: Element, callback: ResizeObserverCallback): ResizeObserver {
    // 如果已经存在观察器，先断开
    if (this.observers.has(element)) {
      this.disconnectObserver(element)
    }

    // 创建新的观察器
    const observer = new ResizeObserver(callback)
    observer.observe(element)
    this.observers.set(element, observer)
    
    return observer
  }

  // 断开特定元素的观察
  disconnectObserver(element: Element): void {
    const observer = this.observers.get(element)
    if (observer) {
      observer.disconnect()
      this.observers.delete(element)
    }
  }

  // 断开所有观察
  disconnectAll(): void {
    this.observers.forEach(observer => observer.disconnect())
    this.observers.clear()
    
    if (this.collectionObserver) {
      this.collectionObserver.disconnect()
      this.collectionObserver = null
    }
  }

  // 使用 Collection 模式观察多个元素
  observeCollection(elements: Element[], callback: ResizeObserverCallback): ResizeObserver {
    if (this.collectionObserver) {
      this.collectionObserver.disconnect()
    }

    this.collectionObserver = new ResizeObserver(callback)
    elements.forEach(element => {
      this.collectionObserver!.observe(element)
    })
    
    return this.collectionObserver
  }
}

// 将管理器暴露到全局
;(window as any).ResizeObserverManager = ResizeObserverManager.getInstance()

// 重写 ResizeObserver 构造函数以避免多子节点警告
if (typeof ResizeObserver !== 'undefined') {
  const OriginalResizeObserver = ResizeObserver
  
  ResizeObserver = function(callback: ResizeObserverCallback) {
    const observer = new OriginalResizeObserver((entries, observer) => {
      // 使用 requestAnimationFrame 来延迟回调执行，避免多子节点警告
      requestAnimationFrame(() => {
        try {
          callback(entries, observer)
        } catch (error) {
          console.warn('ResizeObserver 回调执行出错:', error)
        }
      })
    })
    
    return observer
  } as any
  
  // 保持原型链
  ResizeObserver.prototype = OriginalResizeObserver.prototype
  ResizeObserver.prototype.constructor = ResizeObserver
}