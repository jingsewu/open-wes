# Enum i18n Multi-Language Dictionary Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将字典枚举的 label 从单语言（中文硬编码）升级为双列多语言架构（`systemLabel` + `customLabel`），实现开发维护默认值、客户覆盖不冲突、无需部署即可修改翻译。

**Architecture:** `Dictionary.DictionaryItem` 新增 `systemLabel`（开发维护）和 `customLabel`（客户覆盖）两个 `MultiLanguage` 字段，替换原有 `showContext`。服务端读取时执行 `custom[lang] → system[lang] → system["zh-CN"]` 三级 fallback，前端无感知。`refresh()` 仅用于初始化空表，保持最简单逻辑；日常枚举变更通过 Liquibase changeset 维护。

**Tech Stack:** Java 17, Spring Boot 3.2.2, Hibernate 6 (JSON column via `@JdbcTypeCode(SqlTypes.JSON)`), MapStruct, JUnit 5, React 17 / AMIS JSON schema.

**Design spec:** `docs/superpowers/specs/2026-05-17-enum-i18n-design.md`

---

## File Map

| File | Action | Responsibility |
|------|--------|----------------|
| `server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/domain/entity/Dictionary.java` | Modify | `showContext` → `systemLabel`，新增 `customLabel` |
| `server/modules-wes/wes-api/src/main/java/org/openwes/wes/api/config/dto/DictionaryDTO.java` | Modify | `DictionaryItem` 新增 `systemContent`（只读参考） |
| `server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/domain/transfer/DictionaryTransfer.java` | Modify | 拆分写入路径：`toSystemLabelDOItem()`（refresh 用）/ `toDOItem()`（admin 用）；读取执行三级 fallback |
| `server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/application/DictionaryApiImpl.java` | Modify | `update()` 改为加载已有记录、只更新 `customLabel`，保留 `systemLabel` |
| `server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/controller/DictionaryController.java` | Modify | `refresh()` 改调 `toSystemLabelDOs()` |
| `client/src/pages/wms/config_center/rule/dictionary_management.tsx` | Modify | items 表格新增只读"系统标签"列，`showContent` 列改为"自定义标签" |
| `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20260521.xml` | Create | 迁移已有 `showContext` JSON key → `systemLabel`，为核心枚举补充 `en-US` label |
| `server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml` | Modify | 注册新 changelog 文件 |
| `server/modules-wes/wes-config/src/test/java/org/openwes/wes/config/domain/transfer/DictionaryTransferTest.java` | Create | 三级 fallback 单元测试 |

---

## Task 1: Domain Entity — 字段替换

**Files:**
- Modify: `server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/domain/entity/Dictionary.java`

- [ ] **Step 1: 修改 `DictionaryItem`，将 `showContext` 直接改名为 `systemLabel`，新增 `customLabel`**

```java
@Data
public static class DictionaryItem {
    private String value;
    private MultiLanguage systemLabel;   // 系统默认，由 refresh / Liquibase 写入
    private MultiLanguage customLabel;   // 客户覆盖，由管理界面写入（null = 未定制）
    private int order;
    private boolean defaultItem;
    private MultiLanguage description;
}
```

> 字段从 `showContext` 直接改为 `systemLabel`，无任何向后兼容处理。DB 中的旧 JSON key `showContext` 将由 Task 7 的 Liquibase changeset 统一迁移。

- [ ] **Step 2: 编译确认无报错**

```bash
cd server && ./gradlew :modules-wes:wes-config:compileJava
```

预期：`BUILD SUCCESSFUL`（此时 DictionaryTransfer 等引用 `showContext` 的地方会报错，Step 2 是验证实体本身编译通过）

- [ ] **Step 3: Commit**

```bash
git add server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/domain/entity/Dictionary.java
git commit -m "refactor: rename DictionaryItem.showContext to systemLabel, add customLabel"
```

---

## Task 2: DTO — 新增 `systemContent` 字段

**Files:**
- Modify: `server/modules-wes/wes-api/src/main/java/org/openwes/wes/api/config/dto/DictionaryDTO.java`

- [ ] **Step 1: 在 `DictionaryItem` 新增 `systemContent`**

```java
@Data
public static class DictionaryItem {

    @NotEmpty
    private String value;
    private String showContent;     // 合并后的展示 label（getAll 消费方使用）/ 管理界面写入的 custom label
    private String systemContent;   // 系统默认 label（管理界面只读参考，由 toDTOItem 填充）
    private int order;
    private boolean defaultItem;
    private String description;
}
```

- [ ] **Step 2: 编译**

```bash
cd server && ./gradlew :wes-api:compileJava
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add server/modules-wes/wes-api/src/main/java/org/openwes/wes/api/config/dto/DictionaryDTO.java
git commit -m "feat: add systemContent field to DictionaryDTO.DictionaryItem"
```

---

## Task 3: DictionaryTransfer — 三级 fallback + 双写入路径

**Files:**
- Modify: `server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/domain/transfer/DictionaryTransfer.java`
- Create: `server/modules-wes/wes-config/src/test/java/org/openwes/wes/config/domain/transfer/DictionaryTransferTest.java`

- [ ] **Step 1: 写失败测试**

创建 `DictionaryTransferTest.java`：

```java
package org.openwes.wes.config.domain.transfer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openwes.common.utils.language.MultiLanguage;
import org.openwes.common.utils.language.core.LanguageContext;
import org.openwes.wes.config.domain.entity.Dictionary;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DictionaryTransferTest {

    @BeforeEach
    void setUp() {
        LanguageContext.setLanguage("en-US");
    }

    @AfterEach
    void tearDown() {
        LanguageContext.remove();
    }

    @Test
    void resolveLabel_customLabelTakesPriority() {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setSystemLabel(new MultiLanguage(Map.of("en-US", "System EN", "zh-CN", "系统中文")));
        item.setCustomLabel(new MultiLanguage(Map.of("en-US", "Custom EN")));

        assertThat(DictionaryTransfer.resolveLabel(item)).isEqualTo("Custom EN");
    }

    @Test
    void resolveLabel_fallsBackToSystemLabel_whenNoCustom() {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setSystemLabel(new MultiLanguage(Map.of("en-US", "System EN", "zh-CN", "系统中文")));
        item.setCustomLabel(null);

        assertThat(DictionaryTransfer.resolveLabel(item)).isEqualTo("System EN");
    }

    @Test
    void resolveLabel_fallsBackToZhCN_whenCurrentLangMissing() {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setSystemLabel(new MultiLanguage(Map.of("zh-CN", "系统中文")));
        item.setCustomLabel(null);

        assertThat(DictionaryTransfer.resolveLabel(item)).isEqualTo("系统中文");
    }

    @Test
    void resolveLabel_returnsEmpty_whenBothNull() {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setSystemLabel(null);
        item.setCustomLabel(null);

        assertThat(DictionaryTransfer.resolveLabel(item)).isEmpty();
    }
}
```

- [ ] **Step 2: 运行确认失败（`resolveLabel` 尚未实现）**

```bash
cd server && ./gradlew :modules-wes:wes-config:test --tests "*.DictionaryTransferTest" 2>&1 | tail -10
```

预期：编译错或测试失败

- [ ] **Step 3: 替换 `DictionaryTransfer.java` 完整内容**

```java
package org.openwes.wes.config.domain.transfer;

import org.openwes.common.utils.language.MultiLanguage;
import org.openwes.common.utils.language.core.LanguageContext;
import org.openwes.wes.api.config.dto.DictionaryDTO;
import org.openwes.wes.config.domain.entity.Dictionary;
import org.mapstruct.*;

import java.util.List;
import java.util.stream.Collectors;

import static org.mapstruct.NullValueCheckStrategy.ALWAYS;
import static org.mapstruct.NullValueMappingStrategy.RETURN_NULL;

@Mapper(componentModel = "spring",
        nullValueCheckStrategy = ALWAYS,
        nullValueMappingStrategy = RETURN_NULL,
        unmappedTargetPolicy = ReportingPolicy.IGNORE,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface DictionaryTransfer {

    @Mapping(source = "items", target = "items")
    @Mapping(source = "name", target = "name", qualifiedByName = "toMultiLanguage")
    @Mapping(source = "description", target = "description", qualifiedByName = "toMultiLanguage")
    Dictionary toDO(DictionaryDTO dictionaryDTO);

    List<Dictionary> toDOs(List<DictionaryDTO> dictionaryDTOS);

    @Mapping(source = "name", target = "name", qualifiedByName = "toCurrentLanguage")
    @Mapping(source = "description", target = "description", qualifiedByName = "toCurrentLanguage")
    @Mapping(source = "items", target = "items")
    DictionaryDTO toDTO(Dictionary dictionary);

    /**
     * Admin UI 写入路径：showContent → customLabel。
     * systemContent 若非空则保留写回 systemLabel（保证 admin save 不丢失系统标签）。
     */
    default Dictionary.DictionaryItem toDOItem(DictionaryDTO.DictionaryItem dto) {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setValue(dto.getValue());
        item.setOrder(dto.getOrder());
        item.setDefaultItem(dto.isDefaultItem());
        item.setDescription(new MultiLanguage(LanguageContext.getLanguage(), dto.getDescription()));
        item.setCustomLabel(new MultiLanguage(LanguageContext.getLanguage(), dto.getShowContent()));
        if (dto.getSystemContent() != null && !dto.getSystemContent().isEmpty()) {
            item.setSystemLabel(new MultiLanguage(LanguageContext.getLanguage(), dto.getSystemContent()));
        }
        return item;
    }

    /**
     * refresh() 专用写入路径：showContent → systemLabel，不写 customLabel。
     * refresh 仅用于初始化空表，此时不存在需要保护的 customLabel。
     */
    default Dictionary.DictionaryItem toSystemLabelDOItem(DictionaryDTO.DictionaryItem dto) {
        Dictionary.DictionaryItem item = new Dictionary.DictionaryItem();
        item.setValue(dto.getValue());
        item.setOrder(dto.getOrder());
        item.setDefaultItem(dto.isDefaultItem());
        item.setDescription(new MultiLanguage(LanguageContext.getLanguage(), dto.getDescription()));
        item.setSystemLabel(new MultiLanguage(LanguageContext.getLanguage(), dto.getShowContent()));
        return item;
    }

    /**
     * refresh() 批量转换入口。
     */
    default Dictionary toSystemLabelDO(DictionaryDTO dto) {
        Dictionary dictionary = new Dictionary();
        dictionary.setCode(dto.getCode());
        dictionary.setEditable(dto.isEditable());
        dictionary.setName(toMultiLanguage(dto.getName()));
        dictionary.setDescription(toMultiLanguage(dto.getDescription()));
        dictionary.setItems(dto.getItems().stream()
                .map(this::toSystemLabelDOItem)
                .collect(Collectors.toList()));
        return dictionary;
    }

    default List<Dictionary> toSystemLabelDOs(List<DictionaryDTO> dtos) {
        return dtos.stream().map(this::toSystemLabelDO).collect(Collectors.toList());
    }

    /**
     * 读取路径：三级 fallback → showContent；systemLabel → systemContent（admin 参考用）。
     */
    default DictionaryDTO.DictionaryItem toDTOItem(Dictionary.DictionaryItem item) {
        DictionaryDTO.DictionaryItem dto = new DictionaryDTO.DictionaryItem();
        dto.setValue(item.getValue());
        dto.setOrder(item.getOrder());
        dto.setDefaultItem(item.isDefaultItem());
        dto.setDescription(toCurrentLanguage(item.getDescription()));
        dto.setShowContent(resolveLabel(item));
        dto.setSystemContent(item.getSystemLabel() != null ? toCurrentLanguage(item.getSystemLabel()) : "");
        return dto;
    }

    @Named("toMultiLanguage")
    static MultiLanguage toMultiLanguage(String value) {
        return new MultiLanguage(LanguageContext.getLanguage(), value);
    }

    @Named("toCurrentLanguage")
    static String toCurrentLanguage(MultiLanguage language) {
        if (language == null) {
            return "";
        }
        return language.get();
    }

    /**
     * 三级 fallback：customLabel[lang] → systemLabel[lang] → systemLabel["zh-CN"] → ""
     */
    static String resolveLabel(Dictionary.DictionaryItem item) {
        String lang = LanguageContext.getLanguage();
        if (item.getCustomLabel() != null) {
            String v = item.getCustomLabel().get(lang);
            if (v != null && !v.isEmpty()) return v;
        }
        if (item.getSystemLabel() != null) {
            String v = item.getSystemLabel().get(lang);
            if (v != null && !v.isEmpty()) return v;
            String zh = item.getSystemLabel().get("zh-CN");
            if (zh != null && !zh.isEmpty()) return zh;
        }
        return "";
    }
}
```

- [ ] **Step 4: 运行测试确认全部通过**

```bash
cd server && ./gradlew :modules-wes:wes-config:test --tests "*.DictionaryTransferTest"
```

预期：`4 tests completed, 0 failed`

- [ ] **Step 5: Commit**

```bash
git add server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/domain/transfer/DictionaryTransfer.java \
        server/modules-wes/wes-config/src/test/java/org/openwes/wes/config/domain/transfer/DictionaryTransferTest.java
git commit -m "feat: add resolveLabel fallback and split system/custom write paths in DictionaryTransfer"
```

---

## Task 4: DictionaryApiImpl — admin update 只更新 customLabel

**Files:**
- Modify: `server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/application/DictionaryApiImpl.java`

**Background:** 当前 `update()` 直接 `toDO(dto)` 重建整个对象再保存。这样会把 `systemLabel` 也替换成 DTO 里的值（来自 `systemContent` 回传）。
更安全的做法：加载已有字典，只把 customLabel 替换为 DTO 里的 showContent，然后保存。这样 systemLabel 永远不会被管理界面意外修改。

- [ ] **Step 1: 修改 `DictionaryApiImpl.update()`**

```java
@Override
public void update(DictionaryDTO dictionaryDTO) {
    Dictionary existing = dictionaryRepository.findById(dictionaryDTO.getId());

    // 只更新 customLabel，保留 systemLabel
    String lang = LanguageContext.getLanguage();
    Map<String, DictionaryDTO.DictionaryItem> dtoItemMap = dictionaryDTO.getItems().stream()
            .collect(Collectors.toMap(DictionaryDTO.DictionaryItem::getValue, i -> i));

    for (Dictionary.DictionaryItem existingItem : existing.getItems()) {
        DictionaryDTO.DictionaryItem dtoItem = dtoItemMap.get(existingItem.getValue());
        if (dtoItem == null) continue;
        if (existingItem.getCustomLabel() == null) {
            existingItem.setCustomLabel(new MultiLanguage(lang, dtoItem.getShowContent()));
        } else {
            existingItem.getCustomLabel().put(lang, dtoItem.getShowContent());
        }
    }

    dictionaryRepository.save(existing);
}
```

在文件顶部补充 import：

```java
import java.util.Map;
import java.util.stream.Collectors;
import org.openwes.common.utils.language.MultiLanguage;
import org.openwes.common.utils.language.core.LanguageContext;
```

确认 `DictionaryRepository` 有 `findById(Long id)` 方法（已有）。

- [ ] **Step 2: 编译**

```bash
cd server && ./gradlew :modules-wes:wes-config:compileJava
```

预期：`BUILD SUCCESSFUL`

- [ ] **Step 3: Commit**

```bash
git add server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/application/DictionaryApiImpl.java
git commit -m "feat: admin update() now only modifies customLabel, preserving systemLabel"
```

---

## Task 5: DictionaryController — refresh() 接入 toSystemLabelDOs

**Files:**
- Modify: `server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/controller/DictionaryController.java`

- [ ] **Step 1: 将 `refresh()` 末尾的保存调用改为使用 `toSystemLabelDOs()`**

找到 `refresh()` 末尾：

```java
// 原来
dictionaryRepository.saveAll(dictionaryTransfer.toDOs(dictionaryDTOS));
```

改为：

```java
// 改后
dictionaryRepository.saveAll(dictionaryTransfer.toSystemLabelDOs(dictionaryDTOS));
```

- [ ] **Step 2: 检查 `dictionaryTransfer` 字段是否还有其他用处，若无则保留（refresh 仍需要它）**

```bash
grep -n "dictionaryTransfer" server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/controller/DictionaryController.java
```

- [ ] **Step 3: 编译 + 运行所有 wes-config 测试**

```bash
cd server && ./gradlew :modules-wes:wes-config:test
```

预期：所有测试通过

- [ ] **Step 4: Commit**

```bash
git add server/modules-wes/wes-config/src/main/java/org/openwes/wes/config/controller/DictionaryController.java
git commit -m "feat: refresh() uses toSystemLabelDOs to write only systemLabel"
```

---

## Task 6: 前端字典管理界面

**Files:**
- Modify: `client/src/pages/wms/config_center/rule/dictionary_management.tsx`
- Modify: `client/src/locales/zh-cn.json`
- Modify: `client/src/locales/en-us.json`

- [ ] **Step 1: 更新 items 表格列定义**

找到 `dictionary_management.tsx` items `input-table` 中的 columns 数组，将：

```typescript
{
    name: "showContent",
    label: "table.label",
    type: "input-text"
},
```

改为：

```typescript
{
    name: "systemContent",
    label: "dictionaryManagement.systemLabel",
    type: "static"
},
{
    name: "showContent",
    label: "dictionaryManagement.customLabel",
    type: "input-text"
},
```

- [ ] **Step 2: 添加 i18n key 到 zh-cn.json**

找到含 `dictionaryManagement` 的 key 附近，追加：

```json
"dictionaryManagement.systemLabel": "系统标签",
"dictionaryManagement.customLabel": "自定义标签",
```

- [ ] **Step 3: 添加 i18n key 到 en-us.json**

同样位置追加：

```json
"dictionaryManagement.systemLabel": "System label",
"dictionaryManagement.customLabel": "Custom label",
```

- [ ] **Step 4: 验证 JSON 有效性**

```bash
cd client && node -e "require('./src/locales/zh-cn.json'); require('./src/locales/en-us.json'); console.log('OK')"
```

预期：`OK`

- [ ] **Step 5: Commit**

```bash
git add client/src/pages/wms/config_center/rule/dictionary_management.tsx \
        client/src/locales/zh-cn.json \
        client/src/locales/en-us.json
git commit -m "feat: show system label (readonly) and custom label (editable) in dictionary management UI"
```

---

## Task 7: Liquibase — 迁移旧数据 + 补充 en-US system labels

**Files:**
- Create: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20260521.xml`
- Modify: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml`

**Background:** 旧 DB 数据中 JSON key 是 `showContext`，但 Task 1 已将 Java 字段改名为 `systemLabel`。
Hibernate 保存时会写 `systemLabel`，但读取旧数据时 Jackson 找不到 `systemLabel` key（旧数据只有 `showContext`）——因此需要一次性 SQL 迁移。

- [ ] **Step 1: 创建 changeset 文件**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
    xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <!--
        Changeset 1: 将 m_dictionary.items 中每个条目的 showContext key 重命名为 systemLabel。
        同时新增 customLabel: null 占位（MySQL JSON 无需显式写 null，不存在即为 null）。
    -->
    <changeSet id="dict_rename_showContext_to_systemLabel" author="dev">
        <preConditions onFail="MARK_RAN">
            <and>
                <tableExists tableName="m_dictionary"/>
                <sqlCheck expectedResult="1">
                    SELECT COUNT(*) > 0
                    FROM m_dictionary
                    WHERE JSON_SEARCH(items, 'one', NULL, NULL, '$[*].showContext') IS NOT NULL
                    LIMIT 1
                </sqlCheck>
            </and>
        </preConditions>
        <sql>
            UPDATE m_dictionary
            SET items = (
                SELECT JSON_ARRAYAGG(
                    JSON_REMOVE(
                        JSON_SET(item_val, '$.systemLabel', JSON_EXTRACT(item_val, '$.showContext')),
                        '$.showContext'
                    )
                )
                FROM JSON_TABLE(items, '$[*]' COLUMNS (item_val JSON PATH '$')) AS jt
            )
            WHERE JSON_SEARCH(items, 'one', NULL, NULL, '$[*].showContext') IS NOT NULL;
        </sql>
    </changeSet>

    <!--
        Changeset 2: 补充 en-US system labels 模板。
        按此格式为所有核心枚举逐一补充：

        <changeSet id="dict_en_{code}_{value}" author="dev">
            <preConditions onFail="MARK_RAN">
                <and>
                    <tableExists tableName="m_dictionary"/>
                    <sqlCheck expectedResult="0">
                        SELECT COUNT(*) FROM m_dictionary
                        WHERE code = '{EnumCode}'
                        AND JSON_SEARCH(items, 'one', '{EnglishLabel}', NULL,
                            '$[*].systemLabel.languages.en-US') IS NOT NULL
                    </sqlCheck>
                </and>
            </preConditions>
            <sql>
                UPDATE m_dictionary
                SET items = (
                    SELECT JSON_ARRAYAGG(
                        CASE
                            WHEN JSON_UNQUOTE(JSON_EXTRACT(jt.val, '$.value')) = '{VALUE}'
                            THEN JSON_SET(jt.val, '$.systemLabel.languages.en-US', '{EnglishLabel}')
                            ELSE jt.val
                        END
                    )
                    FROM JSON_TABLE(items, '$[*]' COLUMNS (val JSON PATH '$')) AS jt
                )
                WHERE code = '{EnumCode}';
            </sql>
        </changeSet>

        实际部署前需为所有枚举值补充完整 en-US 数据。
        后续每次新增枚举值，也按此模板在新的 changelog 文件中添加对应 changeset。
    -->

</databaseChangeLog>
```

- [ ] **Step 2: 在 master changelog 末尾注册**

找到 `db.changelog-master.xml` 最后一个 `<include>` 标签后追加：

```xml
<include file="db/changelog/db.changelog-20260521.xml" relativeToChangelogFile="false"/>
```

- [ ] **Step 3: Commit**

```bash
git add server/server/wes-server/src/main/resources/db/changelog/db.changelog-20260521.xml \
        server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml
git commit -m "feat: migrate showContext->systemLabel in JSON and add en-US label changeset template"
```

---

## Manual Verification Checklist

完成所有 Task 后：

1. 启动后端，字典表为空时调用 `POST /config/dictionary/refresh` → 检查 DB 中 `items` JSON 含 `systemLabel` key，无 `showContext`，无 `customLabel`
2. `POST /config/dictionary/getAll`（`Locale: zh-CN`）→ 中文 label 正常返回
3. `POST /config/dictionary/getAll`（`Locale: en-US`）→ 有 en-US Liquibase 数据时返回英文，否则 fallback 到中文
4. 打开字典管理界面 → 每行有"系统标签"（只读）和"自定义标签"（可编辑）两列
5. 修改某枚举值的"自定义标签"，保存 → 再次 `getAll` 检查返回自定义值
6. 再次调用 `refresh` → 检查刚才的自定义标签未被覆盖（DB 中 `customLabel` 仍在）
