# Frontend Coding Standards

Supplements the quick-reference rules in `CLAUDE.md`. Read this before writing any frontend code.

---

## Logging

**All log messages must be in English.** This applies to `console.log`, `console.warn`, `console.error`, toast messages, and code comments.

```typescript
// WRONG
console.warn("API 返回数据格式异常:", res)
toast.error("获取工作站数据失败，请检查网络连接")

// CORRECT
console.warn("Unexpected API response format:", res)
toast.error("Failed to fetch workstation data, please check the network connection")
```

**Never commit debug `console.log` calls.** Diagnostic logs added during development must be removed before the code is committed.

```typescript
// WRONG — must be removed before commit
console.log("WorkStationCard - workStationEvent:", workStationEvent)
console.log("%c =====> event loop destroy", "color:red;font-size:20px;")
console.log('🚀 开始建立WebSocket连接...')

// OK — operational logs that belong in production
console.warn("WebSocket connection timed out, closing")
console.error("Failed to fetch workstation data:", error)
```

**Never override `console.error` to suppress errors.** Even in development, silencing errors hides real bugs.

```typescript
// WRONG — masks real MobX state tree bugs
if (process.env.NODE_ENV === "development") {
    const originalError = console.error
    console.error = (...args) => {
        if (args[0]?.includes("no longer part of a state tree")) return
        originalError.apply(console, args)
    }
}

// CORRECT — fix the root cause instead
```

---

## React Patterns

### Never mutate component props

React props are read-only. Mutation silently fails in non-strict mode and throws in strict mode.

```typescript
// WRONG
this.rendererInstance.props.store = null

// CORRECT — only nullify your own ref
this.rendererInstance = null
```

### Know what third-party render functions return

`renderSchema()` from AMIS returns a `React.ReactElement` (a plain object `{ type, props, key }`), **not** a class instance. Never call `destroy()`, `dispose()`, or traverse `.children` on it as if it were a class.

```typescript
// WRONG — ReactElement has no destroy() or dispose()
if (typeof this.rendererInstance.destroy === 'function') {
    this.rendererInstance.destroy()
}
this.cleanupAmisChildren(this.rendererInstance.children) // fiber internals, unreliable

// CORRECT — React handles teardown; just release the ref
componentWillUnmount() {
    this.rendererInstance = null
    this.env = null
}
```

### Don't call global teardown from component unmount

Avoid methods that affect application-wide state (e.g., disconnecting all ResizeObservers globally) in a single component's cleanup. Scope cleanup to what the component itself owns.

```typescript
// WRONG — disconnects every ResizeObserver in the entire app
this.resizeObserverManager.disconnectAll()

// CORRECT — disconnect only what this component registered
this.resizeObserver?.disconnect()
```

---

## MobX + React

Any component that reads from a MobX observable store **must** be wrapped with `observer`. Without it, MobX changes are invisible to React and the component never re-renders.

```typescript
// WRONG
const MyComponent = (props) => {
    const { store } = useWorkStation()
    return <div>{store.workStationStatus}</div>
}
export default MyComponent

// CORRECT
import { useWorkStation, observer } from "../state"
const MyComponent = (props) => {
    const { store } = useWorkStation()
    return <div>{store.workStationStatus}</div>
}
export default observer(MyComponent)
```

`observer` is exported from `state/hooks/useWorkStation.ts` → re-exported from `state/index.ts`. Always import from there for consistency.

---

## SVG

### Use namespaced IDs for SVG definitions

SVG `id` attributes (for `<linearGradient>`, `<clipPath>`, `<filter>`, etc.) are global within the document. If the same component is rendered more than once, duplicate IDs cause the browser to silently use the first definition for all instances.

```typescript
// WRONG — "logoGrad" is a generic name; will break if rendered twice
<linearGradient id="logoGrad">...</linearGradient>
<rect fill="url(#logoGrad)" />

// CORRECT — module-scoped constant, namespaced to this component
const LOGO_GRAD_ID = "openwes-logo-grad"

<linearGradient id={LOGO_GRAD_ID}>...</linearGradient>
<rect fill={`url(#${LOGO_GRAD_ID})`} />
```

---

## Navigation

Navigation must be **event-driven** (WebSocket → store → component reaction), never **response-driven** (HTTP response → immediate `history.push`).

Calling `history.push` directly after an action response:
1. Triggers component unmount → `eventLoop.pause()` → listener detached
2. Subsequent WebSocket messages are lost during transition
3. Fresh API call on re-mount may return stale data (server state not yet committed)

```typescript
// WRONG
const handleConfirm = async () => {
    await request_work_station_event(payload)
    history.push("/wms/workStation/card") // triggers unmount before WS message arrives
}

// CORRECT — let the WebSocket event drive navigation via store update
const handleConfirm = async () => {
    await actionDispatch(payload)
    // WorkStationCard's useEffect watches workStationEvent and navigates when appropriate
}
```

---

## WebSocket Lifecycle (station module)

| Method | Effect | When to call |
|--------|--------|--------------|
| `start()` | Refresh API data + reuse or create WebSocket | On station entry |
| `pause()` | Detach React listener; keep WebSocket alive | On route navigation within station |
| `destroy()` | Close WebSocket + reset store | On logout or explicit station exit |

`start()` is idempotent — safe to call multiple times. `destroy()` must be called before leaving the station app entirely (e.g., `workStationEventLoop.destroy()` in the logout handler).
