# Enum i18n Design — Brainstorming In Progress

> Status: **In Progress** — discussion not yet complete, design not finalized.
> Started: 2026-05-17

---

## 背景：当前架构

### 核心组件

| 组件 | 位置 | 作用 |
|------|------|------|
| `IEnum` 接口 | `modules-utils/common-utils/.../dictionary/IEnum.java` | 所有业务枚举的标记接口，定义 `getValue()` / `getLabel()` |
| `DictionaryController` | `modules-wes/wes-config/.../controller/DictionaryController.java` | 提供 `/refresh`、`/getAll`、`/createOrUpdate` 接口 |
| `Dictionary` 实体 | `modules-wes/wes-config/.../entity/Dictionary.java` | 字典领域对象，`items` 字段用 `MultiLanguage` 结构存多语言 |
| `m_dictionary` 表 | DB | 字典持久化，`items` JSON 列存枚举值列表 |

### 数据流

```
IEnum 枚举实现（100+）
    ↓ /refresh（反射扫描，开发期手动调用）
m_dictionary 数据库表
    ↓ /getAll（前端启动时调用一次）
前端 localStorage
    ↓ 渲染时查 dictionary["ContainerStatus"] → 显示 label
```

### 枚举当前写法

```java
@Getter
@AllArgsConstructor
public enum ContainerStatusEnum implements IEnum {
    IN_SIDE("IN_SIDE", "在库内"),
    OUT_SIDE("OUT_SIDE", "在库外");

    private final String value;
    private final String label;  // 硬编码中文
}
```

### `/refresh` 的角色

`refresh()` 是**开发期工具接口**，不在生产自动调用。
流程：新增/修改枚举 → 本地调 `/refresh` 同步进库 → 导出 Liquibase SQL → 提交。

---

## 三个原始痛点

1. **Liquibase SQL 维护** — 每次枚举变更需手动同步 SQL，容易遗漏
2. **国际化** — `getLabel()` 全部返回中文，无多语言支持
3. **枚举扩展** — Java enum 封闭，难以在不改代码的情况下扩展

---

## 讨论结论

### 痛点 1：Liquibase SQL 维护

**结论：维持现有 refresh 流程，可接受。**

`refresh()` 作为开发期工具，手动调用后导出 SQL 的流程本身没有大问题。
不引入「启动自动 refresh」机制。

### 痛点 3：枚举扩展

**结论：问题本身可以拆解掉，不需要专门设计。**

分析：
- 如果新枚举值需要业务代码感知 → 必须改代码 → 直接在 Java 枚举里加常量，这就是正确方式
- 如果只需要前端展示/筛选 → 直接在 `m_dictionary` 的 `items` 里加记录，无需 Java 枚举承载

因此「枚举扩展」不是独立问题，两种场景分别有自然的解法，**本次设计范围排除**。

### 痛点 2：国际化（主要目标）

**初步方向：注解携带多语言默认值 + 数据库作为覆盖层**

```java
public enum ContainerStatusEnum implements IEnum {

    @DictionaryLabel(zhCN = "在库内", enUS = "In Stock", jaJP = "在庫内")
    IN_SIDE("IN_SIDE"),

    @DictionaryLabel(zhCN = "在库外", enUS = "Out of Stock")
    OUT_SIDE("OUT_SIDE");

    private final String value;
}
```

- 代码注解 = 种子数据/默认值
- 数据库 = 最终权威（运营人员可在 UI 覆盖 label）
- `refresh()` 读注解写入 `MultiLanguage`，策略为「不存在则插入，已有不覆盖」

---

## 待解决的开放问题

### 🔲 i18n 接口返回方式（下次继续）

`/getAll` 接口的多语言返回策略：

- **A) 按 Accept-Language 返回单语言** — 服务端根据请求头返回对应语言的 label，前端无感知
- **B) 一次返回所有语言** — 返回 `{"zh-CN": "在库内", "en-US": "In Stock"}`，前端自己选
- **C) 两者都支持**

*尚未决策。*

### 🔲 `@DictionaryLabel` 的缺省策略

当注解缺少某个语言时（如没写 `jaJP`），fallback 到什么？
- fallback 到 `zhCN`？
- fallback 到 `enUS`？
- 留空？

*尚未决策。*

### 🔲 存量枚举的迁移策略

现有 100+ 枚举只有中文。是否要求一次性全部补注解，还是渐进式？

*尚未决策。*

### 🔲 `IEnum` 接口变更

`getLabel()` 是否需要修改签名？如何保持向后兼容？

*尚未决策。*

---

## 下次继续的起点

从「i18n 接口返回方式」的选择开始，然后依次解决 fallback 策略和迁移策略，之后进入完整设计文档编写。
