# OPEN-WES 主布局视觉优化设计文档

**日期**: 2026-05-06
**状态**: 已批准
**涉及范围**: 主布局 Shell（Header、Sidebar、Tab 栏、AI 悬浮按钮）

---

## 1. 背景与目标

当前主布局（`src/pages/index.tsx`）使用的是 AMIS 框架默认的蓝色管理后台风格，整体视觉老旧，具体问题：

- 侧边栏和品牌条均为纯平 `#1890ff` 蓝，缺乏层次感
- Header 分两段（蓝色品牌条 + 白色导航条），视觉割裂，左上角空旷
- AI 聊天按钮使用 60px 巨型 SVG，视觉比例失调

目标：以最小改动量将整体风格升级为现代简洁的 SaaS 亮色风格，不改变任何业务逻辑。

---

## 2. 设计决策

| 维度 | 选择 | 理由 |
|---|---|---|
| 整体风格 | 简洁亮色（白色侧边栏） | 现代 SaaS 感，避免过时的纯色蓝模板感 |
| Header 结构 | 全宽单行，横跨侧边栏和内容区 | 消除左上角空白，结构最整齐 |
| Logo 位置 | Header 最左侧（与侧边栏等宽 180px 区域） | 视觉上与侧边栏对齐，整体感强 |
| 实现方案 | 方案二：改 LayoutHeader + SCSS | 改动集中，结构干净，不过度工程化 |

---

## 3. 各区域设计规格

### 3.1 全宽 Header

- **高度**: 52px
- **背景**: `#ffffff`，底部边框 `1px solid #e5e7eb`，轻阴影 `0 1px 4px rgba(0,0,0,0.06)`
- **结构（从左到右）**:
  1. **Logo 区**（宽 180px，右侧有竖向分割线 `#e5e7eb`）：Logo 图标（28×28px 蓝色圆角方块） + "OPEN-WES" 粗体文字
  2. **折叠按钮**：28px 方形，灰色三横线图标，点击收起/展开侧边栏
  3. **应用切换 Menu**：水平排列，当前选中项用 `#eff6ff` 背景 + `#2563eb` 蓝色文字圆角标签
  4. **右侧控件**（flex 尾部）：仓库下拉选择 → 语言切换 → 用户按钮（蓝色圆角，含下拉箭头）

- **与 AMIS 的关系**: 将现有 `cxd-Layout-brandBar` 和 `cxd-Layout-headerBar` 两段合并为一个 `<div>` 输出，通过 `style.scss` 隐藏原始的双层结构

### 3.2 侧边栏

- **宽度**: 180px（与 Header Logo 区等宽）
- **背景**: `#ffffff`，右侧边框 `1px solid #e5e7eb`
- **顶部**: 无品牌条（Header 已覆盖），直接从导航菜单开始（padding-top: 10px）
- **分组标签**: 全大写，10px，`#9ca3af`，letter-spacing: 0.6px
- **菜单项样式**:
  - 默认：`#374151` 文字，hover 时背景 `#f9fafb`
  - 激活（active）：背景 `#eff6ff`，文字 `#2563eb`，左侧 3×16px 竖条 `#2563eb`
  - 激活子项：背景 `#dbeafe`，文字 `#1d4ed8`
- **文字颜色**：从原来的硬编码 `style={{ color: "#fff" }}` 改为 `#374151`（激活时覆盖为 `#2563eb`）

### 3.3 Tab 栏

- **高度**: 36px
- **背景**: `#ffffff`，底部边框 `1px solid #f3f4f6`
- **激活 Tab**: 背景 `#f0f9ff`，文字 `#0284c7`，顶部边框 `1px solid #bae6fd`，底部 `2px solid #0284c7`（下划线），margin-bottom: -1px 贴合边框
- **非激活 Tab**: 背景 `#f9fafb`，文字 `#6b7280`，边框 `1px solid #f3f4f6`
- **实现**: 通过 `style.scss` 覆盖 Ant Design 的 `.ant-tabs-` 类

### 3.4 内容区

- **背景**: `#f9fafb`（原为 `#f4f4f5`，更柔和）
- **卡片（搜索栏、表格容器）**: 背景 `#ffffff`，圆角 `8px`，边框 `1px solid #e5e7eb`，阴影 `0 1px 2px rgba(0,0,0,0.04)`
- **实现**: 覆盖 `.cxd-Layout-content` 背景色，其余卡片样式由已有 AMIS 渲染器负责，无需额外改动

### 3.5 AI 悬浮按钮

- **尺寸**: 40px 圆形（原 60px SVG）
- **背景**: `#2563eb`，阴影 `0 4px 14px rgba(37,99,235,0.4)`
- **hover**: `transform: scale(1.08)`
- **位置**: 固定在右下角 `bottom: 24px; right: 24px`
- **图标**: 保留 `RobotSvg`，通过 `fontSize: 20` 缩小至合适比例

---

## 4. 文件改动范围

| 文件 | 改动类型 | 说明 |
|---|---|---|
| `src/components/LayoutHeader.tsx` | 重构 JSX 结构 | 将 brandBar 和 headerBar 合并为单层 div，Logo 置于左侧 180px 区域 |
| `src/components/LayoutAside.tsx` | 修改样式属性 | 将硬编码 `color: "#fff"` 替换为 CSS 类控制，active 状态添加左边框元素 |
| `src/scss/style.scss` | 更新 CSS 覆盖规则 | 更新 `.cxd-Layout-aside`、`.cxd-Layout-brandBar`、`.cxd-Layout-headerBar`、`.cxd-AsideNav-item` 等覆盖规则；添加 Tab 样式覆盖 |
| `src/pages/index.module.scss` | 更新按钮样式 | 缩小 `.fixButton` 尺寸，更新阴影和 hover 效果 |
| `src/pages/index.tsx` | 微调按钮 JSX | 调整 `RobotSvg` 的 fontSize，移除多余 style |

**不改动的文件**：路由逻辑、业务组件、TabsLayout.tsx、所有功能性代码。

---

## 5. 约束与注意事项

- AMIS 的 `Layout` 组件通过 `aside` 和 `header` prop 接收 ReactNode，Header 的 HTML 结构在 `LayoutHeader.tsx` 中输出，但 AMIS 会在外层包裹 `.cxd-Layout-brandBar` 和 `.cxd-Layout-headerBar` 两个 div。需通过 CSS（`display:flex; align-items:stretch` 等）使其在视觉上合并为单行，或直接在 `style.scss` 中将 `brandBar` 高度设为 0 并把所有内容移入 `headerBar`。
- 侧边栏使用 AMIS 的 `AsideNav` 组件，CSS 类名有 `cxd-` 前缀，覆盖时需要 `!important`。
- 改动后需确认侧边栏折叠（`store.asideFolded`）状态下的图标显示正常。
- 语言切换、仓库切换功能逻辑不变，仅调整视觉样式。
