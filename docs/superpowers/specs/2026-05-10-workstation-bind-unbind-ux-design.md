# 工作站绑定 & 解绑 UX 设计

**日期：** 2026-05-10
**分支：** feature_shelf_ai_refactor

## 背景

当前问题：工作站退出（OFFLINE）后 `clearStation()` 从未被调用，`localStorage` 中的 `stationId` 永久保留，导致用户每次进入工作站都自动绑定上一台，无法主动切换。

## 目标

1. 设计工作站**绑定** UX（替换现有 `SelectStation` 覆盖层）
2. 设计工作站**解绑** UX（退出后在卡片页操作）
3. 删除 `SelectStation.tsx`

---

## 设计

### 场景一：绑定工作站

**触发条件：** 进入工作站路由，`isStationSelected === false`（localStorage 无 stationId）。

**UI：** 全屏居中绑定页（非 overlay），替换现有 `SelectStation`：
- 标题：「选择要绑定的工作站」
- 副标题：「绑定后方可开始作业，退出作业后可解绑」
- 工作站列表（调用现有 `request_work_station()` API）
  - 空闲站点：可点击选中，单选
  - 占用中站点：置灰 + 「不可用」标签
- 「绑定工作站」确认按钮（未选中时禁用）

**行为：** 点击确认 → `selectStation(id)`（写 localStorage）→ 渲染工作站作业 UI。

---

### 场景二：退出工作站

**入口：** 工作站 Header 右侧新增「退出工作站」按钮，**替代** toolbar 中的 EXIT 按钮。

**行为：**
1. 点击 → 确认弹窗（「确认退出工作站？」）
2. 确认 → `onActionDispatch({ eventCode: CustomActionType.OFFLINE })`
3. 服务端 OFFLINE → WebSocket → `workStationStatus = OFFLINE`
4. Header `useEffect` 检测到 OFFLINE → `history.push(STATION_MENU_PATH)`（保留现有自动跳转行为）

---

### 场景三：解绑工作站

**触发条件：** 跳回 `/wms/workStation` 卡片页时，localStorage 中仍有 `stationId`。

**UI：** 卡片页顶部显示橙色 banner：
- 文案：「工作站 {stationCode} 已退出。当前仍绑定此工作站，下次进入将自动连接。」
- 按钮「保留绑定」：关闭 banner，不做任何操作
- 按钮「解绑工作站」：调用 `clearStation()`，banner 消失

**clearStation() 行为（现有逻辑）：**
```
eventLoop.destroy()          // 关闭 WebSocket + store.reset()
localStorage.removeItem("stationId")
setIsStationSelected(false)
```

---

## 用户旅程

| 场景 | 流程 |
|---|---|
| 首次进入（未绑定） | 工作站路由 → 绑定页 → 选站确认 → 进入作业 |
| 再次进入（已绑定） | 工作站路由 → 直接进入作业（跳过绑定页） |
| 退出 | Header「退出工作站」→ 确认 → OFFLINE → 自动跳回卡片页 → banner 出现 |
| 解绑 | Banner「解绑工作站」→ `clearStation()` → 下次进入显示绑定页 |
| 保留绑定 | Banner「保留绑定」/ 忽略 → 下次进入直接进入作业 |
| 关闭浏览器 | stationId 保留，下次打开自动重连同一台 |

---

## 文件变更范围

| 文件 | 变更类型 | 说明 |
|---|---|---|
| `station/SelectStation.tsx` | 删除 | 被新绑定页替代 |
| `station/BindStation.tsx` | 新增 | 新绑定页组件 |
| `station/index.tsx` | 修改 | 渲染 `BindStation` 替换 `SelectStation` |
| `station/layout/header.tsx` | 修改 | 新增「退出工作站」按钮（含确认弹窗）|
| `tab-actions/action-configs/existTask.tsx` | 修改 | 移除 EXIT 按钮配置（或整个文件删除） |
| 工作站卡片页组件 | 修改 | 新增解绑 banner 组件 |

---

## 边界情况

- **网络断开导致 OFFLINE**：同样触发自动跳转 + banner，用户可选择解绑或忽略
- **直接关闭浏览器**：stationId 留在 localStorage，下次打开自动重连（现有行为不变）
- **Header 退出按钮与 toolbar EXIT 并存期间**：toolbar EXIT 保持现有行为，待 header 按钮上线后统一删除 toolbar EXIT
