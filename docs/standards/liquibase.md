# Liquibase Patterns in Open-WES

## File Layout

- Changelogs: `server/server/wes-server/src/main/resources/db/changelog/`
- Naming: `db.changelog-YYYYMMDD.xml`
- Master include list: `db.changelog-master.xml` — append new file at the bottom
- Init SQL (do NOT touch): `server/server/wes-server/src/main/resources/db/sql/init/`

## Iron Rule: Never Modify Already-Applied Files

`db.changelog-1.0.xml` loads init SQL files via `<sqlFile>`. Liquibase checksums every
changeset on startup — if you edit an already-applied file the server refuses to start:

```
ValidationFailedException: 1 change sets check sum ... is not a valid checksum
```

**Never edit:** `init_dictionary.sql`, `init_*.sql`, or any SQL file referenced by an
already-released changelog. Add new data via a new changeset instead.

## Standard Changeset Template

```xml
<changeSet id="descriptive_snake_case_id" author="YourName">
    <preConditions onFail="MARK_RAN">
        <and>
            <tableExists tableName="your_table"/>
            <!-- add column guards here -->
        </and>
    </preConditions>
    <comment>One-line description of what this does</comment>
    <sql>
        ALTER TABLE ...;
    </sql>
</changeSet>
```

Always use `onFail="MARK_RAN"` — lets the server start even if the change was already
applied manually or by Hibernate auto-DDL.

## ADD + MIGRATE + DROP Pattern (3 Separate Changesets)

Never put ADD, UPDATE, and DROP in a single changeset. If Hibernate auto-DDL already
created the new columns before Liquibase runs, the single-changeset approach fails with
"Duplicate column name". Split into three idempotent steps:

```xml
<!-- Step 1: add new columns — skip if already exist -->
<changeSet id="table_add_new_columns" author="...">
    <preConditions onFail="MARK_RAN">
        <and>
            <tableExists tableName="t"/>
            <not><columnExists tableName="t" columnName="new_col"/></not>
        </and>
    </preConditions>
    <sql>ALTER TABLE t ADD COLUMN new_col TEXT COMMENT '...';</sql>
</changeSet>

<!-- Step 2: migrate data — skip if old columns already gone -->
<changeSet id="table_migrate_data" author="...">
    <preConditions onFail="MARK_RAN">
        <and>
            <tableExists tableName="t"/>
            <columnExists tableName="t" columnName="old_col"/>
        </and>
    </preConditions>
    <sql>UPDATE t SET new_col = COALESCE(old_col, fallback_col);</sql>
</changeSet>

<!-- Step 3: drop old columns — skip if already dropped -->
<changeSet id="table_drop_old_columns" author="...">
    <preConditions onFail="MARK_RAN">
        <and>
            <tableExists tableName="t"/>
            <columnExists tableName="t" columnName="old_col"/>
        </and>
    </preConditions>
    <sql>ALTER TABLE t DROP COLUMN old_col, DROP COLUMN other_old_col;</sql>
</changeSet>
```

**Why this is robust:**
- Hibernate added new columns already → step 1 skipped, steps 2+3 run fine
- Completely fresh DB → all 3 steps run in order
- Migration already done manually → all 3 skipped

## Enum Changes Require a Dictionary Changeset

**Rule:** Every time you add or modify an `IEnum` enum value, you **must** include a
Liquibase changeset in the same commit that syncs the dictionary.

| Enum change | Required changeset action |
|-------------|--------------------------|
| Add a new enum value | `JSON_ARRAY_INSERT` — append the new item to the dictionary |
| Rename / change `getValue()` | Update `value` field in the matching item |
| Remove an enum value | `JSON_REMOVE` — delete the item from the array |

`POST /config/dictionary/refresh` is a **dev-only helper** for local iteration; it must
not substitute for a proper changeset in committed code.

## Dictionary Updates via Liquibase

Dictionaries live in `m_dictionary.items` (JSON array). Use `JSON_SEARCH` as the
idempotency guard so re-running never duplicates an entry:

```xml
<changeSet id="dictionary_code_add_value" author="...">
    <preConditions onFail="MARK_RAN">
        <and>
            <tableExists tableName="m_dictionary"/>
            <sqlCheck expectedResult="1">
                SELECT COUNT(*) FROM m_dictionary
                WHERE code = 'DictionaryCode'
                AND JSON_SEARCH(items, 'one', 'NEW_VALUE', NULL, '$[*].value') IS NULL
            </sqlCheck>
        </and>
    </preConditions>
    <sql>
        UPDATE m_dictionary
        SET items = JSON_ARRAY_INSERT(
            items, '$[N]',
            JSON_OBJECT(
                'order', 0, 'value', 'NEW_VALUE', 'defaultItem', false,
                'description', JSON_OBJECT('languages', JSON_OBJECT('en-US', 'Label', 'zh-CN', NULL)),
                'showContext', JSON_OBJECT('languages', JSON_OBJECT('en-US', 'Label', 'zh-CN', '标签'))
            )
        )
        WHERE code = 'DictionaryCode';
    </sql>
</changeSet>
```

Alternatively, an admin can call `POST /config/dictionary/refresh` — it auto-scans all
`IEnum` implementations and updates the DB. But Liquibase is still needed for automated
deployments.
