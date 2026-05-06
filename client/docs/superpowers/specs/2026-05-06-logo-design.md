# Logo 现代化设计文档

**日期：** 2026-05-06
**范围：** `src/components/LayoutHeader.tsx`（仅 logo 区域）

---

## 背景

现有 Header logo 使用 `wes.svg`（仓储几何路径图形）配合 Ant Design `<Icon>` 组件，文字 "OPEN-WES" 为单一深色。整体视觉偏旧，需要现代化更新。

---

## 设计决策

### 风格方向
圆角方形徽标（W 字母）+ 双色 wordmark，取代原有复杂几何路径图标。

### 徽标（SVG 内联）
- 尺寸：34×34px
- 形状：圆角矩形，`rx="9"`
- 填充：线性渐变，左上 → 右下
  - 起点：`#2563eb`（品牌蓝）
  - 终点：`#4f46e5`（靛蓝）
- 文字：白色 "W"，17px，font-weight 900，居中

### 文字（wordmark）
- `Open`：15px，font-weight 500，color `#374151`
- `WES`：15px，font-weight 800，color `#2563eb`
- 无下划线，无副标题

### 实现方式
**方案二：内联 SVG**。直接在 `LayoutHeader.tsx` 的 logo 区域 div 内写 `<svg>` 元素，替换原有的 `<Icon component={LogoIcon}>` 和 `<span>OPEN-WES</span>`。不需要新建文件，不修改 `wes.svg`。

---

## 修改范围

| 文件 | 操作 |
|------|------|
| `src/components/LayoutHeader.tsx` | 替换 logo 区域内的 `<Icon>` + `<span>` 为内联 SVG + 双色 span |

不涉及：登录页、`wes.svg`、CSS 模块、其他组件。

---

## 最终代码片段

```tsx
{/* Logo zone */}
<div
  style={{
    width: "var(--layout-aside-width)",
    height: "100%",
    display: "flex",
    alignItems: "center",
    padding: "0 16px",
    gap: 10,
    flexShrink: 0,
    borderRight: "1px solid #e5e7eb"
  }}
>
  <svg width="34" height="34" viewBox="0 0 34 34" fill="none" xmlns="http://www.w3.org/2000/svg">
    <defs>
      <linearGradient id="logoGrad" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0%" stopColor="#2563eb" />
        <stop offset="100%" stopColor="#4f46e5" />
      </linearGradient>
    </defs>
    <rect width="34" height="34" rx="9" fill="url(#logoGrad)" />
    <text x="17" y="23.5" textAnchor="middle" fill="white" fontSize="17" fontWeight="900" fontFamily="Arial,sans-serif">W</text>
  </svg>
  <span style={{ fontSize: 15, whiteSpace: "nowrap" }}>
    <span style={{ fontWeight: 500, color: "#374151" }}>Open</span>
    <span style={{ fontWeight: 800, color: "#2563eb" }}>WES</span>
  </span>
</div>
```

---

## 验收标准

- Header logo 区域显示蓝靛渐变圆角方形徽标
- "Open" 灰色，"WES" 品牌蓝
- logo 与 sidebar 宽度对齐，borderRight 分隔线保留
- 不影响登录页、侧边栏、其他功能
