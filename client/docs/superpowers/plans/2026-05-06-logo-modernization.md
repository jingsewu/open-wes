# Logo 现代化 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将 `LayoutHeader.tsx` 的 logo 区域替换为内联 SVG 圆角方形徽标（蓝靛渐变 W）+ 双色 wordmark。

**Architecture:** 直接在 `LayoutHeader.tsx` 的 logo 区域 div 内写内联 `<svg>` 元素，移除原有的 `LogoSvg` import、`LogoIcon` 组件和 `<Icon>` 用法。单文件改动，无需新建文件。

**Tech Stack:** React (TSX)、内联 SVG、Ant Design（`Icon` import 移除）

---

## File Map

| 操作 | 文件 |
|------|------|
| Modify | `src/components/LayoutHeader.tsx` |

---

### Task 1: 替换 logo 区域为内联 SVG

**Files:**
- Modify: `src/components/LayoutHeader.tsx`

- [ ] **Step 1: 移除不再需要的 import 和组件**

在 `src/components/LayoutHeader.tsx` 中，找到并删除以下三行：

```tsx
// 删除这行
import LogoSvg from "@/icon/icon_logo/wes.svg"

// 删除这行
import Icon from "@ant-design/icons"

// 删除这行
const LogoIcon = () => <LogoSvg />
```

删除后，文件顶部 import 区应如下（无 LogoSvg、无 Icon）：

```tsx
import React, { useState } from "react"
import { Translation } from "react-i18next"
import { useHistory } from "react-router"
import { Button, Dropdown, Menu, Space, Select } from "antd"
import { DownOutlined } from "@ant-design/icons"
import type { MenuProps } from "antd"

import store from "@/stores"

import Language from "@/pages/components/Language"
import ChangePasswordForm from "@/pages/components/ChangePassword"
```

- [ ] **Step 2: 替换 logo 区域内容**

找到 logo 区域 div（带注释 `{/* ── Logo zone ... ──*/}`），将其内部内容从：

```tsx
<Icon
    component={LogoIcon}
    style={{ fontSize: "22px", color: "#2563eb" }}
/>
<span style={{ fontSize: 14, fontWeight: 700, color: "#111827", whiteSpace: "nowrap" }}>
    OPEN-WES
</span>
```

替换为：

```tsx
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
```

完整的 logo 区域 div 最终应为：

```tsx
{/* ── Logo zone — visually aligns with sidebar width ── */}
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

- [ ] **Step 3: 验证编译无报错**

```bash
cd D:/git_workspace/open-wes/client
npm run build 2>&1 | tail -20
```

预期：无 TypeScript 错误，无 "Cannot find module" 警告。如有报错，检查是否还有残留的 `Icon` 或 `LogoSvg` 引用。

- [ ] **Step 4: 视觉验证**

启动开发服务器（若尚未启动）：

```bash
npm run dev
```

在浏览器中登录后，检查 Header 左侧：
- ✅ 显示蓝靛渐变圆角方形，内有白色 "W"
- ✅ "Open" 为灰色，"WES" 为蓝色
- ✅ logo 与 sidebar 宽度对齐
- ✅ borderRight 分隔线正常显示
- ✅ 折叠按钮、导航菜单、右侧控件不受影响

- [ ] **Step 5: Commit**

```bash
cd D:/git_workspace/open-wes/client
git add src/components/LayoutHeader.tsx
git commit -m "feat(header): modernize logo with inline SVG gradient badge"
```
