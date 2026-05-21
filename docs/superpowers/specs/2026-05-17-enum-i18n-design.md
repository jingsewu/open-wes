# Enum i18n Design — 字典多语言

**Date:** 2026-05-17
**Status:** Design finalized

---

## 背景：当前架构

### 核心组件

| 组件 | 位置 | 作用 |
|------|------|------|
| `IEnum` 接口 | `modules-utils/common-utils/.../dictionary/IEnum.java` | 所有业务枚举的标记接口，定义 `getValue()` / `getLabel()` |
| `DictionaryController` | `modules-wes/wes-config/.../controller/DictionaryController.java` | 提供 `/refresh`、`/getAll`、`/createOrUpdate` 接口 |
| `Dictionary` 实体 | `modules-wes/wes-config/.../entity/Dictionary.java` | 字典领域对象，`DictionaryItem.showContext` 用 `MultiLanguage` 结构存多语言 |
| `DictionaryTransfer` | `modules-wes/wes-config/.../transfer/DictionaryTransfer.java` | DTO ↔ 实体转换，`toDTOItem()` 调 `LanguageContext` 过滤当前语言 |
| `LanguageFilter` | `modules-utils/common-utils/.../language/core/LanguageFilter.java` | 读 `Locale` 请求头 → 写入 `LanguageContext` ThreadLocal |
| `m_dictionary` 表 | DB | 字典持久化，`items` JSON 列存枚举值列表 |

### 数据流（现状）

```
IEnum.getLabel()（只有中文）
  ↓ refresh()
DB: showContext = {"zh-CN": "在库内"}   ← 只存了中文
  ↓ getAll()（Locale 请求头 → LanguageContext → 过滤当前语言）
前端 localStorage.dictionary
  ↓ AMIS ${dictionary.ContainerStatus}
下拉列表 [{label: "在库内", value: "IN_SIDE"}]
```

### 现有问题

1. `refresh()` 只往 `zh-CN` 写数据，其他语言字段为空
2. 英文用户请求 → `language.get("en-US")` → null → 空字符串
3. 要加其他语言必须在管理界面一条条手动编辑，100+ 枚举 × N 语言不可操作

---

## 痛点结论

| 痛点 | 结论 |
|------|------|
| Liquibase SQL 维护 | 维持现有 refresh 流程，枚举变更跟着 Liquibase changeset 提交，可接受 |
| 枚举扩展 | 不独立设计：需业务代码感知→改 Java 枚举；只需展示→直接改 DB items |
| 国际化 | **核心问题，本文设计目标** |

**翻译数据不能放在代码（注解/资源文件）里**，修改需要重新部署，违背"翻译可运营管理"的要求。

**翻译数据唯一合理的位置是数据库**——改完立即生效，无需部署。

---

## 核心设计

### 两列分离：系统默认值 vs 客户覆盖值

**背景**：字典有两个主人——开发（系统默认值）和客户（业务定制）。
若共用同一列，Liquibase 迁移会覆盖客户的修改，产生冲突。

```
DictionaryItem 新结构：
  system_label   MultiLanguage  ← 开发维护，refresh() / Liquibase 写这里
  custom_label   MultiLanguage  ← 客户在管理界面修改，写这里（null = 未定制）
```

**读取优先级（服务端合并）：**
```
custom_label[lang] → system_label[lang] → system_label["zh-CN"]
```

**写入规则：**
- `refresh()` / Liquibase changeset → 只更新 `system_label`，永远不碰 `custom_label`
- 管理界面 → 只写 `custom_label`

**无冲突保证**：开发迁移和客户定制写不同列，互不干扰。
客户恢复默认 → 把 `custom_label` 对应 key 清空即可。

---

## 各层变更

### 1. 领域实体 `Dictionary.DictionaryItem`

```java
// Before
private MultiLanguage showContext;

// After
private MultiLanguage systemLabel;   // 系统默认，由 refresh/Liquibase 维护
private MultiLanguage customLabel;   // 客户覆盖，由管理界面维护（可为 null）
```

### 2. `DictionaryTransfer`

`toDTOItem()` 合并逻辑：

```java
private String resolveLabel(Dictionary.DictionaryItem item) {
    String lang = LanguageContext.getLanguage();
    // 1. 客户覆盖
    if (item.getCustomLabel() != null) {
        String custom = item.getCustomLabel().get(lang);
        if (custom != null && !custom.isEmpty()) return custom;
    }
    // 2. 系统默认（当前语言）
    if (item.getSystemLabel() != null) {
        String system = item.getSystemLabel().get(lang);
        if (system != null && !system.isEmpty()) return system;
        // 3. fallback 到中文
        String zhFallback = item.getSystemLabel().get("zh-CN");
        if (zhFallback != null && !zhFallback.isEmpty()) return zhFallback;
    }
    return "";
}
```

`toDOItem()` 写入：
- 入参 `showContent` → 写入 `systemLabel`（当前语言 key）
- `customLabel` 保持不动

### 3. `refresh()`

逻辑不变：调 `getLabel()` 拿中文 → 经 `toDOItem()` 存入 `system_label["zh-CN"]`。
其他语言 system_label 通过 Liquibase changeset 初始化（见下文）。

### 4. `getAll()`

逻辑不变：仍返回服务端合并后的单语言 label，前端无感知。

### 5. `IEnum` 接口

**不变**。`getLabel()` 仍返回中文，继续用于 refresh() 种子数据。

### 6. 管理界面

- 展示：每个枚举值同时显示 `system_label`（只读参考）和 `custom_label`（可编辑）
- 编辑：按语言 tab，修改后只写 `custom_label`
- 重置：清空 `custom_label` 对应 key，恢复使用 `system_label`

---

## 数据库迁移

### Changeset 1：字段迁移（存量数据）

```xml
<!-- 存量 showContext → system_label，custom_label 新增为 null -->
<changeSet id="dict-multilang-rename" author="dev">
    <preConditions onFail="MARK_RAN">
        <columnExists tableName="m_dictionary" columnName="items"/>
    </preConditions>
    <!-- items JSON 列内 showContext key 改名为 systemLabel，新增 customLabel: null -->
    <!-- 具体实现根据 JSON 列存储方式决定（MapStruct 或 DB JSON 函数） -->
</changeSet>
```

### Changeset 2：初始化全量多语言数据

一次性 SQL 脚本，把所有枚举的英文/日文/韩文 system_label 写入 DB。
格式参考：
```sql
UPDATE m_dictionary_item
SET system_label = JSON_SET(system_label, '$."en-US"', 'In Stock')
WHERE value = 'IN_SIDE' AND dict_code = 'ContainerStatus';
```

### 后续枚举变更

每次新增/修改枚举值 → 写一个 Liquibase changeset 更新 `system_label`，流程与现有一致。

---

## Fallback 策略

```
custom_label[请求语言]
  ↓ null/空
system_label[请求语言]
  ↓ null/空
system_label["zh-CN"]
  ↓ null/空
""（空字符串，不报错）
```

---

## 不在本次范围内

- 自动翻译集成（机器翻译 API）
- 翻译版本历史 / 审核流程
- 枚举扩展（动态添加非 Java 枚举的值）
