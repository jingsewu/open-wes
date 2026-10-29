# Script Plugin Phase 3A — Backend Governance and APIs Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add server-side role enforcement, append-only operation audit, config-value APIs, execution-log query/stats, metadata updates, and real test-run to the script-plugin management plane.

**Architecture:** New authorization and audit services sit between the controllers and the existing `ScriptPluginService`. Every workbench mutation is authorized in the service layer and audited with the acting username. New REST controllers expose config, execution, and test-run surfaces; test-run executes on the wes node through the existing throwaway revision context and execution runtime.

**Tech Stack:** Java 17, Spring Boot 3.2.2, Spring Data JPA, Bean Validation, Liquibase, JUnit 5 + Mockito, Gradle 8.3.

**Spec:** `docs/superpowers/specs/2026-10-22-script-plugin-phase3-workbench-design.md`

## Global Constraints

- Set `JAVA_HOME` before every Gradle command: `export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"`. The default shell JDK is Java 8 and the build fails.
- Backend only. Do not modify `client/` in this plan.
- `ignoreFailures=false`; every test failure blocks the task.
- Put every new client-facing DTO in `script-plugin-api` under `org.openwes.script.plugin.api.dto`; put new enums under `org.openwes.script.plugin.api.enums`.
- `ScriptPluginAuditActionEnum` is a **plain enum**, not `IEnum`; do not add a dictionary changeset for it.
- Enforce authorization in services, not controllers. Controllers use `@Valid` only for bean validation.
- Never write script sources, prompts, request bodies, config values, test-run arguments, or test-run results to logs, exceptions, or audit rows.
- All log messages and user-facing errors are English.
- Use `UserContext.getCurrentUser()` for the caller and `AuthConstants.USERNAME_SYSTEM` (`"system"`) for internal work.
- Do not commit unrelated working-tree changes: `client/src/locales/en-us.json`, `client/src/locales/zh-cn.json`, `client/src/pages/wms/dashboard/index.tsx`, `server/modules-search/search-api/src/main/java/com/openwes/search/api/vo/dashboard/OperatorRanking.java`, `server/modules-search/search-api/src/main/java/com/openwes/search/api/vo/dashboard/WorkstationTask.java`, `client/src/scss/dashboard.scss`, `docs/superpowers/plans/2026-10-22-script-plugin-phase2-ai-pipeline.md`.
- Liquibase: create `db.changelog-20261023.xml`; never edit an applied changeset; use `onFail="MARK_RAN"`; register the new file in `db.changelog-master.xml`.
- Do not claim manual E2E/acceptance passed. Docker is unavailable; tests and builds are the evidence.
- End every commit message with `Co-Authored-By: Claude Code <noreply@anthropic.com>`.

## File Map

| Task | Create | Modify |
|---|---|---|
| 1 | `user-api/.../IUserRoleApi.java`, `user/.../UserRoleApiImpl.java`, `UserRoleApiImplTest.java` | — |
| 2 | `api/enums/ScriptPluginAuditActionEnum.java`, `api/dto/PageResultDTO.java`, `api/dto/ScriptPluginOperationAuditDTO.java`, `core/domain/entity/ScriptPluginOperationAudit.java`, `core/domain/repository/ScriptPluginOperationAuditRepository.java`, `core/domain/service/ScriptPluginOperationAuditService.java`, test, `db.changelog-20261023.xml` | `db.changelog-master.xml` |
| 3 | `core/exception/ScriptPluginWorkbenchErrorDescEnum.java`, `api/dto/PluginCapabilitiesDTO.java`, `api/dto/PluginContractDTO.java`, `core/domain/service/PluginWorkbenchAccessService.java`, `core/domain/service/ScriptPluginContractService.java`, two tests | `script-plugin-core/build.gradle` |
| 4 | — | `ScriptPluginService.java`, `AiDraftGenerationService.java`, `AiDraftGenerationServiceTest.java`, `AiDraftPipelineEndToEndTest.java`, `AiDraftController.java`, `ScriptPluginServiceTest.java` |
| 5 | `api/dto/ScriptPluginMetadataDTO.java`, `core/domain/service/ScriptPluginWorkbenchService.java`, test | `ScriptPlugin.java`, `ScriptPluginService.java`, `ScriptPluginStateTest.java` |
| 6 | `api/dto/ScriptPluginDisableDTO.java` | `ScriptPluginController.java`, `ScriptPluginApiImpl.java`, `ScriptPluginControllerTest.java` |
| 7 | `api/dto/ScriptPluginConfigDTO.java`, `api/dto/ScriptPluginConfigSaveDTO.java`, `core/domain/service/ScriptPluginConfigValidator.java`, `core/domain/service/ScriptPluginConfigService.java`, `core/controller/ScriptPluginConfigController.java`, validator/service/controller tests | `ScriptPluginService.java`, `ScriptPluginServiceTest.java` |
| 8 | `api/dto/ExecutionStatsDTO.java`, `core/domain/repository/ExecutionTypeStatsView.java`, `core/domain/service/ScriptPluginExecutionQueryService.java`, `core/controller/ScriptPluginExecutionController.java`, service/controller tests | `api/dto/ExecutionLogDTO.java`, `ScriptPluginExecutionLogRepository.java` |
| 9 | `api/ScriptPluginTestRunner.java`, `api/dto/TestRunRequestDTO.java`, `api/dto/TestRunResultDTO.java`, `runtime/testrun/TestRunArgumentBuilder.java`, test | `script-plugin-runtime/build.gradle` |
| 10 | `runtime/testrun/ScriptPluginTestRunnerImpl.java`, `core/domain/service/ScriptPluginTestRunService.java`, `core/controller/ScriptPluginTestRunController.java`, three tests | — |
| 11 | `wes-server/src/test/java/org/openwes/config/ScriptPluginSeedChangelogTest.java` | `db.changelog-20261023.xml` |
| 12 | — | `docs/standards/backend.md` |

All paths below are relative to the repository root. Java modules live under `server/`; source roots are `src/main/java` and `src/test/java`.

---

### Task 1: User Role Lookup API

**Files:**
- Create: `server/modules-user/user-api/src/main/java/org/openwes/user/api/IUserRoleApi.java`
- Create: `server/modules-user/user/src/main/java/org/openwes/user/application/impl/UserRoleApiImpl.java`
- Test: `server/modules-user/user/src/test/java/org/openwes/user/application/impl/UserRoleApiImplTest.java`

**Interfaces:**
- Consumes: `UserMapper.findByAccount(String)`, `UserRoleMapper.findByUserId(Long)`, `RoleMapper.findAllById(Iterable<Long>)`.
- Produces: `Set<String> IUserRoleApi.findRoleCodes(String username)` — empty set for blank or unknown users.

- [ ] **Step 1: Write the failing test**

```java
package org.openwes.user.application.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.entity.UserRole;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.UserMapper;
import org.openwes.user.domain.repository.UserRoleMapper;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRoleApiImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private UserRoleMapper userRoleMapper;
    @Mock
    private RoleMapper roleMapper;

    private UserRoleApiImpl api;

    @BeforeEach
    void setUp() {
        api = new UserRoleApiImpl(userMapper, userRoleMapper, roleMapper);
    }

    @Test
    void blankUsername_returnsEmptySetWithoutQuerying() {
        assertTrue(api.findRoleCodes("   ").isEmpty());
        assertTrue(api.findRoleCodes(null).isEmpty());
        verifyNoInteractions(userMapper, userRoleMapper, roleMapper);
    }

    @Test
    void unknownUser_returnsEmptySet() {
        when(userMapper.findByAccount("ghost")).thenReturn(null);

        assertTrue(api.findRoleCodes("ghost").isEmpty());
        verifyNoInteractions(userRoleMapper, roleMapper);
    }

    @Test
    void userWithoutRoles_returnsEmptySet() {
        User user = new User();
        user.setId(7L);
        when(userMapper.findByAccount("alice")).thenReturn(user);
        when(userRoleMapper.findByUserId(7L)).thenReturn(List.of());

        assertTrue(api.findRoleCodes("alice").isEmpty());
        verifyNoInteractions(roleMapper);
    }

    @Test
    void mapsUserRoleIdsToSortedRoleCodes() {
        User user = new User();
        user.setId(7L);
        when(userMapper.findByAccount("alice")).thenReturn(user);
        when(userRoleMapper.findByUserId(7L)).thenReturn(List.of(
                new UserRole().setUserId(7L).setRoleId(1L),
                new UserRole().setUserId(7L).setRoleId(2L),
                new UserRole().setUserId(7L).setRoleId(1L)));
        when(roleMapper.findAllById(anyList())).thenReturn(List.of(
                new Role().setId(1L).setCode("pluginDeveloper"),
                new Role().setId(2L).setCode("pluginAdmin")));

        assertEquals(Set.of("pluginAdmin", "pluginDeveloper"), api.findRoleCodes("alice"));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-user:user:test \
  --tests 'org.openwes.user.application.impl.UserRoleApiImplTest'
```

Expected: compilation failure — `IUserRoleApi` and `UserRoleApiImpl` do not exist.

- [ ] **Step 3: Write the interface**

```java
package org.openwes.user.api;

import java.util.Set;

/**
 * Read-only role lookup for service-layer authorization. Implementations must not cache: role
 * changes take effect on the next call.
 */
public interface IUserRoleApi {

    /** Role codes assigned to the account; empty when the account is blank, unknown, or has no roles. */
    Set<String> findRoleCodes(String username);
}
```

- [ ] **Step 4: Write the implementation**

```java
package org.openwes.user.application.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.openwes.user.api.IUserRoleApi;
import org.openwes.user.domain.entity.Role;
import org.openwes.user.domain.entity.User;
import org.openwes.user.domain.entity.UserRole;
import org.openwes.user.domain.repository.RoleMapper;
import org.openwes.user.domain.repository.UserMapper;
import org.openwes.user.domain.repository.UserRoleMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Primary
@Service
@RequiredArgsConstructor
public class UserRoleApiImpl implements IUserRoleApi {

    private final UserMapper userMapper;
    private final UserRoleMapper userRoleMapper;
    private final RoleMapper roleMapper;

    @Override
    public Set<String> findRoleCodes(String username) {
        if (StringUtils.isBlank(username)) {
            return Set.of();
        }
        User user = userMapper.findByAccount(username);
        if (user == null) {
            return Set.of();
        }
        List<UserRole> links = userRoleMapper.findByUserId(user.getId());
        if (links.isEmpty()) {
            return Set.of();
        }
        List<Long> roleIds = links.stream().map(UserRole::getRoleId).distinct().toList();
        return roleMapper.findAllById(roleIds).stream()
                .map(Role::getCode)
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(TreeSet::new));
    }
}
```

- [ ] **Step 5: Run the test to verify it passes**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-user:user:test \
  --tests 'org.openwes.user.application.impl.UserRoleApiImplTest'
```

Expected: PASS, 4 tests.

- [ ] **Step 6: Commit**

```bash
git add server/modules-user/user-api/src/main/java/org/openwes/user/api/IUserRoleApi.java \
        server/modules-user/user/src/main/java/org/openwes/user/application/impl/UserRoleApiImpl.java \
        server/modules-user/user/src/test/java/org/openwes/user/application/impl/UserRoleApiImplTest.java
git commit -m "feat(user): expose role codes for service-layer authorization

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 2: Append-Only Operation Audit Persistence

**Files:**
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/enums/ScriptPluginAuditActionEnum.java`
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/PageResultDTO.java`
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginOperationAuditDTO.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/entity/ScriptPluginOperationAudit.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/repository/ScriptPluginOperationAuditRepository.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginOperationAuditService.java`
- Create: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261023.xml`
- Modify: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginOperationAuditServiceTest.java`

**Interfaces:**
- Consumes: `UserContext.getCurrentUser()`, `AuthConstants.USERNAME_SYSTEM`.
- Produces:
  - `PageResultDTO<T>(List<T> items, long total, int page, int pageSize)` with static `of(...)`.
  - `ScriptPluginOperationAuditService.record(String pluginKey, ScriptPluginAuditActionEnum action, Long revisionId, boolean success, String detail)`.
  - `recordAs(String pluginKey, ScriptPluginAuditActionEnum action, Long revisionId, boolean success, String detail, String actor)`.
  - `pageForPlugin(String pluginKey, int page, int pageSize)` and `pageAll(int page, int pageSize)`, both returning `PageResultDTO<ScriptPluginOperationAuditDTO>`.
  - Package-private `static String truncate(String value, int max)` for tests.

- [ ] **Step 1: Write the failing audit-service test**

```java
package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginOperationAudit;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginOperationAuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginOperationAuditServiceTest {

    @Mock
    private ScriptPluginOperationAuditRepository repository;

    private ScriptPluginOperationAuditService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new ScriptPluginOperationAuditService(repository);
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
    }

    @Test
    void recordAs_usesExplicitActorAndTruncatesColumns() {
        service.recordAs("packing-wave", ScriptPluginAuditActionEnum.SAVE_REVISION, 11L,
                true, "d".repeat(2000), "u".repeat(100));

        ArgumentCaptor<ScriptPluginOperationAudit> captor =
                ArgumentCaptor.forClass(ScriptPluginOperationAudit.class);
        verify(repository).save(captor.capture());
        ScriptPluginOperationAudit audit = captor.getValue();
        assertEquals("packing-wave", audit.getPluginKey());
        assertEquals(ScriptPluginAuditActionEnum.SAVE_REVISION, audit.getAction());
        assertEquals(11L, audit.getRevisionId());
        assertEquals(64, audit.getOperator().length());
        assertEquals(1024, audit.getDetail().length());
        assertEquals(true, audit.isSuccess());
    }

    @Test
    void record_usesCurrentUserFromContext() {
        UserContext.setAccount("alice");

        service.record("packing-wave", ScriptPluginAuditActionEnum.ENABLE, 11L, true, "enabled");

        ArgumentCaptor<ScriptPluginOperationAudit> captor =
                ArgumentCaptor.forClass(ScriptPluginOperationAudit.class);
        verify(repository).save(captor.capture());
        assertEquals("alice", captor.getValue().getOperator());
    }

    @Test
    void record_blankUserFallsBackToSystem() {
        service.record("packing-wave", ScriptPluginAuditActionEnum.DISABLE, 11L, true, "disabled");

        ArgumentCaptor<ScriptPluginOperationAudit> captor =
                ArgumentCaptor.forClass(ScriptPluginOperationAudit.class);
        verify(repository).save(captor.capture());
        assertEquals("system", captor.getValue().getOperator());
    }

    @Test
    void record_blankPluginKeyUsesUnknownPlaceholder() {
        service.recordAs(" ", ScriptPluginAuditActionEnum.CREATE_DRAFT, null,
                false, "missing role pluginDeveloper", "bob");

        ArgumentCaptor<ScriptPluginOperationAudit> captor =
                ArgumentCaptor.forClass(ScriptPluginOperationAudit.class);
        verify(repository).save(captor.capture());
        assertEquals("unknown", captor.getValue().getPluginKey());
        assertNull(captor.getValue().getRevisionId());
    }

    @Test
    void record_repositoryFailureNeverMasksTheBusinessOperation() {
        when(repository.save(any())).thenThrow(new IllegalStateException("db down"));

        assertDoesNotThrow(() -> service.record("packing-wave",
                ScriptPluginAuditActionEnum.ENABLE, 11L, true, "enabled"));
    }

    @Test
    void pageForPlugin_clampsPageAndPageSizeAndMapsRows() {
        ScriptPluginOperationAudit row = ScriptPluginOperationAudit.record(
                "packing-wave", ScriptPluginAuditActionEnum.ENABLE, 11L, "admin", true, "enabled");
        Page<ScriptPluginOperationAudit> page =
                new PageImpl<>(List.of(row), PageRequest.of(0, 100), 1);
        when(repository.findByPluginKeyOrderByIdDesc(eq("packing-wave"), any(Pageable.class)))
                .thenReturn(page);

        PageResultDTO<ScriptPluginOperationAuditDTO> result =
                service.pageForPlugin("packing-wave", 0, 1000);

        assertEquals(1, result.getPage());
        assertEquals(100, result.getPageSize());
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("admin", result.getItems().get(0).getOperator());
        assertEquals(ScriptPluginAuditActionEnum.ENABLE, result.getItems().get(0).getAction());
    }

    @Test
    void pageAll_returnsAllRows() {
        Page<ScriptPluginOperationAudit> page =
                new PageImpl<>(List.of(), PageRequest.of(1, 20), 0);
        when(repository.findAllByOrderByIdDesc(any(Pageable.class))).thenReturn(page);

        PageResultDTO<ScriptPluginOperationAuditDTO> result = service.pageAll(2, 20);

        assertEquals(2, result.getPage());
        assertEquals(20, result.getPageSize());
        assertEquals(0, result.getTotal());
        assertEquals(0, result.getItems().size());
    }

    @Test
    void truncate_keepsShortValuesAndCapsLongValues() {
        assertNull(ScriptPluginOperationAuditService.truncate(null, 5));
        assertEquals("abc", ScriptPluginOperationAuditService.truncate("abc", 5));
        assertEquals("abcde", ScriptPluginOperationAuditService.truncate("abcdefg", 5));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginOperationAuditServiceTest'
```

Expected: compilation failure — the enum, DTOs, entity, repository, and service do not exist.

- [ ] **Step 3: Create the API enum and DTOs**

```java
package org.openwes.script.plugin.api.enums;

/**
 * Operation-audit vocabulary. Plain enum: these values are not dictionary-backed configuration.
 */
public enum ScriptPluginAuditActionEnum {
    CREATE_DRAFT,
    SAVE_REVISION,
    VALIDATE,
    ENABLE,
    DISABLE,
    ARCHIVE,
    ROLLBACK,
    CONFIG_SAVE,
    METADATA_UPDATE,
    TEST_RUN
}
```

```java
package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResultDTO<T> {
    private List<T> items;
    private long total;
    private int page;
    private int pageSize;

    public static <T> PageResultDTO<T> of(List<T> items, long total, int page, int pageSize) {
        return new PageResultDTO<>(items, total, page, pageSize);
    }
}
```

```java
package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScriptPluginOperationAuditDTO {
    private Long id;
    private String pluginKey;
    private ScriptPluginAuditActionEnum action;
    private Long revisionId;
    private String operator;
    private boolean success;
    private String detail;
    private Long createdAt;
}
```

- [ ] **Step 4: Create the append-only entity and repository**

```java
package org.openwes.script.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;

/**
 * Append-only operation trail. No setters and no update path: rows are written once by the audit
 * service. Payloads (scripts, prompts, config values, request bodies, test arguments/results) are
 * deliberately absent.
 */
@Getter
@Entity
@Table(name = "p_script_plugin_operation_audit", indexes = {
        @Index(name = "idx_script_audit_plugin", columnList = "pluginKey,id"),
        @Index(name = "idx_script_audit_created", columnList = "createdAt")})
public class ScriptPluginOperationAudit {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Plugin key; unknown when the request key was blank")
    private String pluginKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Comment("Audited operation")
    private ScriptPluginAuditActionEnum action;

    @Column
    @Comment("Target revision, when the action has one")
    private Long revisionId;

    @Column(nullable = false, length = 64)
    @Comment("Acting username, or system for internal work")
    private String operator;

    @Column(nullable = false)
    @Comment("Whether the operation succeeded")
    private boolean success;

    @Column(length = 1024)
    @Comment("Failure reason or short summary; never payload data")
    private String detail;

    @Column(nullable = false)
    @Comment("Creation timestamp in epoch milliseconds")
    private Long createdAt;

    private ScriptPluginOperationAudit() {
    }

    public static ScriptPluginOperationAudit record(String pluginKey, ScriptPluginAuditActionEnum action,
                                                    Long revisionId, String operator, boolean success,
                                                    String detail) {
        ScriptPluginOperationAudit audit = new ScriptPluginOperationAudit();
        audit.pluginKey = pluginKey;
        audit.action = action;
        audit.revisionId = revisionId;
        audit.operator = operator;
        audit.success = success;
        audit.detail = detail;
        audit.createdAt = System.currentTimeMillis();
        return audit;
    }
}
```

```java
package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.core.domain.entity.ScriptPluginOperationAudit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScriptPluginOperationAuditRepository
        extends JpaRepository<ScriptPluginOperationAudit, Long> {

    Page<ScriptPluginOperationAudit> findByPluginKeyOrderByIdDesc(String pluginKey, Pageable pageable);

    Page<ScriptPluginOperationAudit> findAllByOrderByIdDesc(Pageable pageable);
}
```

- [ ] **Step 5: Create the audit service**

```java
package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginOperationAudit;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginOperationAuditRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginOperationAuditService {

    static final int DETAIL_MAX_LENGTH = 1024;
    static final int OPERATOR_MAX_LENGTH = 64;
    static final int PLUGIN_KEY_MAX_LENGTH = 64;
    static final int MAX_PAGE_SIZE = 100;

    private final ScriptPluginOperationAuditRepository repository;

    public void record(String pluginKey, ScriptPluginAuditActionEnum action, Long revisionId,
                       boolean success, String detail) {
        recordAs(pluginKey, action, revisionId, success, detail, UserContext.getCurrentUser());
    }

    public void recordAs(String pluginKey, ScriptPluginAuditActionEnum action, Long revisionId,
                         boolean success, String detail, String actor) {
        String operator = StringUtils.defaultIfBlank(actor, AuthConstants.USERNAME_SYSTEM);
        try {
            repository.save(ScriptPluginOperationAudit.record(
                    StringUtils.defaultIfBlank(pluginKey, "unknown"),
                    action,
                    revisionId,
                    truncate(operator, OPERATOR_MAX_LENGTH),
                    success,
                    truncate(detail, DETAIL_MAX_LENGTH)));
        } catch (RuntimeException e) {
            // An audit failure must never mask the business result.
            log.warn("failed to record script plugin audit action {} for plugin {}",
                    action, pluginKey, e);
        }
    }

    public PageResultDTO<ScriptPluginOperationAuditDTO> pageForPlugin(String pluginKey,
                                                                      int page, int pageSize) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedSize);
        Page<ScriptPluginOperationAudit> rows =
                repository.findByPluginKeyOrderByIdDesc(pluginKey, pageable);
        return toPageResult(rows, normalizedPage, normalizedSize);
    }

    public PageResultDTO<ScriptPluginOperationAuditDTO> pageAll(int page, int pageSize) {
        int normalizedPage = normalizePage(page);
        int normalizedSize = normalizePageSize(pageSize);
        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedSize);
        Page<ScriptPluginOperationAudit> rows = repository.findAllByOrderByIdDesc(pageable);
        return toPageResult(rows, normalizedPage, normalizedSize);
    }

    private PageResultDTO<ScriptPluginOperationAuditDTO> toPageResult(
            Page<ScriptPluginOperationAudit> rows, int page, int pageSize) {
        return PageResultDTO.of(rows.getContent().stream().map(this::toDto).toList(),
                rows.getTotalElements(), page, pageSize);
    }

    private ScriptPluginOperationAuditDTO toDto(ScriptPluginOperationAudit audit) {
        return ScriptPluginOperationAuditDTO.builder()
                .id(audit.getId())
                .pluginKey(audit.getPluginKey())
                .action(audit.getAction())
                .revisionId(audit.getRevisionId())
                .operator(audit.getOperator())
                .success(audit.isSuccess())
                .detail(audit.getDetail())
                .createdAt(audit.getCreatedAt())
                .build();
    }

    static int normalizePage(int page) {
        return Math.max(page, 1);
    }

    static int normalizePageSize(int pageSize) {
        return Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
    }

    static String truncate(String value, int max) {
        if (value == null) {
            return null;
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
```

- [ ] **Step 6: Create the audit-table Liquibase changeset and register the file**

Create `db.changelog-20261023.xml` with the audit table. Later tasks append seed changesets before `</databaseChangeLog>`.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<databaseChangeLog
        xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog
        http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-4.20.xsd">

    <changeSet id="create_p_script_plugin_operation_audit" author="dev">
        <preConditions onFail="MARK_RAN">
            <not>
                <tableExists tableName="p_script_plugin_operation_audit"/>
            </not>
        </preConditions>
        <sql>
            CREATE TABLE p_script_plugin_operation_audit (
                id          BIGINT        NOT NULL,
                plugin_key  VARCHAR(64)   NOT NULL,
                action      VARCHAR(32)   NOT NULL,
                revision_id BIGINT        NULL,
                operator    VARCHAR(64)   NOT NULL,
                success     TINYINT(1)    NOT NULL,
                detail      VARCHAR(1024) NULL,
                created_at  BIGINT        NOT NULL,
                PRIMARY KEY (id),
                KEY idx_script_audit_plugin (plugin_key, id),
                KEY idx_script_audit_created (created_at)
            ) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4
              COMMENT = 'Script plugin operation audit';
        </sql>
        <rollback>
            <sql>DROP TABLE p_script_plugin_operation_audit;</sql>
        </rollback>
    </changeSet>

</databaseChangeLog>
```

Add the include to `db.changelog-master.xml` after the 20261022 include:

```xml
    <include file="/db/changelog/db.changelog-20261023.xml"/>
```

- [ ] **Step 7: Run the tests to verify they pass**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginOperationAuditServiceTest'
```

Expected: PASS, 8 tests.

- [ ] **Step 8: Validate the XML parses**

```bash
powershell -NoProfile -Command "[xml](Get-Content -Raw 'server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261023.xml') | Out-Null; Write-Output 'XML OK'"
```

Expected: `XML OK`.

- [ ] **Step 9: Commit**

```bash
git add server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/enums/ScriptPluginAuditActionEnum.java \
        server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/PageResultDTO.java \
        server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginOperationAuditDTO.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/entity/ScriptPluginOperationAudit.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/repository/ScriptPluginOperationAuditRepository.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginOperationAuditService.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginOperationAuditServiceTest.java \
        server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261023.xml \
        server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml
git commit -m "feat(script-plugin): add append-only operation audit

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 3: Access Service, Capabilities, Contract, and Error Codes

**Files:**
- Modify: `server/modules-plugin/script-plugin-core/build.gradle`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/exception/ScriptPluginWorkbenchErrorDescEnum.java`
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/PluginCapabilitiesDTO.java`
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/PluginContractDTO.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/PluginWorkbenchAccessService.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginContractService.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/PluginWorkbenchAccessServiceTest.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginContractServiceTest.java`

**Interfaces:**
- Consumes: `IUserRoleApi.findRoleCodes(String)`, `ScriptPluginOperationAuditService.recordAs(...)`, `PluginContractCatalog`.
- Produces:
  - `PluginCapabilitiesDTO(boolean canDevelop, boolean canAdminister, Set<String> roles)`.
  - `PluginContractDTO(String contractVersion, String applySystem, List<Archetype> archetypes, List<String> lifecycleEntityNames, String promptFragment)`; nested `Archetype(ExtensionType type, String scriptMethod, String hostSignature, boolean blocking)`.
  - `String requireAuthenticated()`.
  - `String requireDeveloper()` / `String requireDeveloper(String pluginKey, ScriptPluginAuditActionEnum action)`.
  - `String requireAdmin()` / `String requireAdmin(String pluginKey, ScriptPluginAuditActionEnum action)`.
  - `PluginCapabilitiesDTO capabilities()`.
  - `PluginContractDTO contract(String applySystem)`.

- [ ] **Step 1: Add the user-api dependency to `script-plugin-core/build.gradle`**

Add this line next to the existing project dependencies:

```gradle
    implementation project(path: ":modules-user:user-api", configuration: 'default')
```

- [ ] **Step 2: Write the failing access-service test**

```java
package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.PluginCapabilitiesDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.user.api.IUserRoleApi;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginWorkbenchAccessServiceTest {

    @Mock
    private IUserRoleApi userRoleApi;
    @Mock
    private ScriptPluginOperationAuditService auditService;

    private PluginWorkbenchAccessService access;

    @BeforeEach
    void setUp() {
        access = new PluginWorkbenchAccessService(userRoleApi, auditService,
                "pluginDeveloper", "pluginAdmin", "superAdmin");
        UserContext.setAccount("alice");
    }

    @AfterEach
    void tearDown() {
        UserContext.removeUser();
    }

    @Test
    void capabilities_developerRole_canDevelopOnly() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("pluginDeveloper"));

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertTrue(capabilities.isCanDevelop());
        assertFalse(capabilities.isCanAdminister());
        assertEquals(Set.of("pluginDeveloper"), capabilities.getRoles());
    }

    @Test
    void capabilities_adminRole_impliesDeveloper() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("pluginAdmin"));

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertTrue(capabilities.isCanDevelop());
        assertTrue(capabilities.isCanAdminister());
    }

    @Test
    void capabilities_bothRoles_areReturnedAndBothChecksPass() {
        when(userRoleApi.findRoleCodes("alice"))
                .thenReturn(Set.of("pluginDeveloper", "pluginAdmin"));

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertTrue(capabilities.isCanDevelop());
        assertTrue(capabilities.isCanAdminister());
        assertEquals(Set.of("pluginDeveloper", "pluginAdmin"), capabilities.getRoles());
    }

    @Test
    void capabilities_superRole_isDeveloperAndAdmin() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("superAdmin"));

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertTrue(capabilities.isCanDevelop());
        assertTrue(capabilities.isCanAdminister());
    }

    @Test
    void capabilities_withoutRoles_isReadOnly() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of());

        PluginCapabilitiesDTO capabilities = access.capabilities();

        assertFalse(capabilities.isCanDevelop());
        assertFalse(capabilities.isCanAdminister());
        assertTrue(capabilities.getRoles().isEmpty());
    }

    @Test
    void blankUser_isRejectedWithAuthenticatedUserCode() {
        UserContext.removeUser();

        WmsException e = assertThrows(WmsException.class, () -> access.requireAuthenticated());

        assertEquals("PL002003", e.getCode());
        verifyNoInteractions(auditService);
    }

    @Test
    void requireDeveloper_denialIsAuditedBeforeThrowing() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of());

        WmsException e = assertThrows(WmsException.class,
                () -> access.requireDeveloper("packing-wave", ScriptPluginAuditActionEnum.SAVE_REVISION));

        assertEquals("PL002001", e.getCode());
        verify(auditService).recordAs(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.SAVE_REVISION), isNull(), eq(false),
                contains("pluginDeveloper"), eq("alice"));
    }

    @Test
    void requireDeveloper_readOnlyDenialIsNotAudited() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of());

        assertThrows(WmsException.class, () -> access.requireDeveloper());

        verifyNoInteractions(auditService);
    }

    @Test
    void requireAdmin_withDeveloperRole_isRejectedAndAudited() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("pluginDeveloper"));

        WmsException e = assertThrows(WmsException.class,
                () -> access.requireAdmin("packing-wave", ScriptPluginAuditActionEnum.ENABLE));

        assertEquals("PL002002", e.getCode());
        verify(auditService).recordAs(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.ENABLE), isNull(), eq(false),
                contains("pluginAdmin"), eq("alice"));
    }

    @Test
    void adminRole_passesBothDeveloperAndAdminChecks() {
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("pluginAdmin"));

        assertEquals("alice", access.requireDeveloper());
        assertEquals("alice", access.requireAdmin());
        verifyNoInteractions(auditService);
    }

    @Test
    void configuredRoleLists_areCommaSeparatedAndTrimmed() {
        PluginWorkbenchAccessService configured = new PluginWorkbenchAccessService(
                userRoleApi, auditService, " devA, devB ", "adminA", "root");
        when(userRoleApi.findRoleCodes("alice")).thenReturn(Set.of("devB"));

        assertTrue(configured.capabilities().isCanDevelop());
        assertFalse(configured.capabilities().isCanAdminister());
    }
}
```

- [ ] **Step 3: Write the failing contract-service test**

```java
package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.PluginContractDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ScriptPluginContractServiceTest {

    @Mock
    private PluginWorkbenchAccessService accessService;

    private ScriptPluginContractService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginContractService(accessService);
    }

    @Test
    void wesContract_excludesStationOnlyArchetypes() {
        PluginContractDTO contract = service.contract("wes");

        assertEquals("1.0", contract.getContractVersion());
        assertEquals("wes", contract.getApplySystem());
        assertTrue(contract.getArchetypes().stream()
                .noneMatch(archetype -> archetype.getType() == ExtensionType.CUSTOM_API));
        assertTrue(contract.getArchetypes().stream()
                .anyMatch(archetype -> archetype.getType() == ExtensionType.CRON));
        assertEquals(PluginContractCatalog.LIFECYCLE_ENTITY_NAMES,
                contract.getLifecycleEntityNames());
        assertTrue(contract.getPromptFragment().contains("Contract version: 1.0"));
        verify(accessService).requireAuthenticated();
    }

    @Test
    void stationContract_containsOnlyStationArchetypes() {
        PluginContractDTO contract = service.contract("station");

        assertEquals(3, contract.getArchetypes().size());
        assertEquals(ExtensionType.CUSTOM_API, contract.getArchetypes().get(0).getType());
    }

    @Test
    void unknownApp_isRejected() {
        assertThrows(CommonException.class, () -> service.contract("unknown"));
    }
}
```

- [ ] **Step 4: Run the tests to verify they fail**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.PluginWorkbenchAccessServiceTest' \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginContractServiceTest'
```

Expected: compilation failure — classes and DTOs do not exist.

- [ ] **Step 5: Create the error enum**

```java
package org.openwes.script.plugin.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.openwes.common.utils.constants.AppCodeEnum;
import org.openwes.common.utils.exception.code_enum.IBaseError;

@Getter
@AllArgsConstructor
public enum ScriptPluginWorkbenchErrorDescEnum implements IBaseError {

    DEVELOPER_ROLE_REQUIRED("PL002001",
            "the plugin developer role is required for this operation",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    ADMIN_ROLE_REQUIRED("PL002002",
            "the plugin administrator role is required for this operation",
            AppCodeEnum.PLUGIN_PLATFORM.name()),
    AUTHENTICATED_USER_REQUIRED("PL002003",
            "an authenticated user is required for this operation",
            AppCodeEnum.PLUGIN_PLATFORM.name());

    private final String code;
    private final String desc;
    private final String appCode;
}
```

- [ ] **Step 6: Create the capability and contract DTOs**

```java
package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginCapabilitiesDTO {
    private boolean canDevelop;
    private boolean canAdminister;
    private Set<String> roles;
}
```

```java
package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginContractDTO {

    private String contractVersion;
    private String applySystem;
    private List<Archetype> archetypes;
    private List<String> lifecycleEntityNames;
    private String promptFragment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Archetype {
        private ExtensionType type;
        private String scriptMethod;
        private String hostSignature;
        private boolean blocking;
    }
}
```

- [ ] **Step 7: Create the access service**

```java
package org.openwes.script.plugin.core.domain.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.dto.PluginCapabilitiesDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.script.plugin.core.exception.ScriptPluginWorkbenchErrorDescEnum;
import org.openwes.user.api.IUserRoleApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Single authorization point for the workbench. Every check reads the caller from UserContext and
 * resolves role codes through IUserRoleApi; no cache, so revocation is immediate.
 */
@Service
@Slf4j
public class PluginWorkbenchAccessService {

    private final IUserRoleApi userRoleApi;
    private final ScriptPluginOperationAuditService auditService;
    private final List<String> developerRoleCodes;
    private final List<String> adminRoleCodes;
    private final String superRoleCode;

    public PluginWorkbenchAccessService(
            IUserRoleApi userRoleApi,
            ScriptPluginOperationAuditService auditService,
            @Value("${plugin.security.developer-role-codes:pluginDeveloper}") String developerRoleCodes,
            @Value("${plugin.security.admin-role-codes:pluginAdmin}") String adminRoleCodes,
            @Value("${superRoleCode:superAdmin}") String superRoleCode) {
        this.userRoleApi = userRoleApi;
        this.auditService = auditService;
        this.developerRoleCodes = parseRoleCodes(developerRoleCodes, "pluginDeveloper");
        this.adminRoleCodes = parseRoleCodes(adminRoleCodes, "pluginAdmin");
        this.superRoleCode = StringUtils.defaultIfBlank(superRoleCode, "superAdmin");
    }

    public PluginCapabilitiesDTO capabilities() {
        String username = requireAuthenticated();
        Set<String> roles = rolesOf(username);
        boolean administer = isAdmin(roles);
        boolean develop = administer || hasAny(roles, developerRoleCodes);
        return new PluginCapabilitiesDTO(develop, administer,
                Collections.unmodifiableSet(new TreeSet<>(roles)));
    }

    public String requireAuthenticated() {
        String username = UserContext.getCurrentUser();
        if (StringUtils.isBlank(username)) {
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.AUTHENTICATED_USER_REQUIRED);
        }
        return username;
    }

    public String requireDeveloper() {
        String username = requireAuthenticated();
        if (!isDeveloper(rolesOf(username))) {
            log.warn("user {} is not a script plugin developer", username);
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.DEVELOPER_ROLE_REQUIRED);
        }
        return username;
    }

    public String requireDeveloper(String pluginKey, ScriptPluginAuditActionEnum action) {
        String username = requireAuthenticated();
        if (!isDeveloper(rolesOf(username))) {
            auditService.recordAs(pluginKey, action, null, false,
                    missingRole(developerRoleCodes), username);
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.DEVELOPER_ROLE_REQUIRED);
        }
        return username;
    }

    public String requireAdmin() {
        String username = requireAuthenticated();
        if (!isAdmin(rolesOf(username))) {
            log.warn("user {} is not a script plugin administrator", username);
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.ADMIN_ROLE_REQUIRED);
        }
        return username;
    }

    public String requireAdmin(String pluginKey, ScriptPluginAuditActionEnum action) {
        String username = requireAuthenticated();
        if (!isAdmin(rolesOf(username))) {
            auditService.recordAs(pluginKey, action, null, false,
                    missingRole(adminRoleCodes), username);
            throw new WmsException(ScriptPluginWorkbenchErrorDescEnum.ADMIN_ROLE_REQUIRED);
        }
        return username;
    }

    private boolean isDeveloper(Set<String> roles) {
        return isAdmin(roles) || hasAny(roles, developerRoleCodes);
    }

    private boolean isAdmin(Set<String> roles) {
        return roles.contains(superRoleCode) || hasAny(roles, adminRoleCodes);
    }

    private static boolean hasAny(Set<String> roles, List<String> codes) {
        return codes.stream().anyMatch(roles::contains);
    }

    private Set<String> rolesOf(String username) {
        Set<String> roles = userRoleApi.findRoleCodes(username);
        return roles == null ? Set.of() : new TreeSet<>(roles);
    }

    private static String missingRole(List<String> codes) {
        return "missing role " + String.join("|", codes);
    }

    private static List<String> parseRoleCodes(String raw, String fallback) {
        if (StringUtils.isBlank(raw)) {
            return List.of(fallback);
        }
        List<String> codes = Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
        return codes.isEmpty() ? List.of(fallback) : codes;
    }
}
```

- [ ] **Step 8: Create the contract service**

```java
package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.PluginContractDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScriptPluginContractService {

    private final PluginWorkbenchAccessService accessService;

    public PluginContractDTO contract(String applySystem) {
        accessService.requireAuthenticated();
        List<ExtensionType> types = PluginContractCatalog.typesForApp(applySystem);
        if (types.isEmpty()) {
            throw new CommonException("unsupported applySystem: " + applySystem);
        }
        List<PluginContractDTO.Archetype> archetypes = PluginContractCatalog.ARCHETYPES.stream()
                .filter(archetype -> types.contains(archetype.type()))
                .map(archetype -> new PluginContractDTO.Archetype(archetype.type(),
                        archetype.scriptMethod(), archetype.hostSignature(), archetype.blocking()))
                .toList();
        return new PluginContractDTO(
                ScriptPluginApiConstants.CONTRACT_VERSION,
                applySystem,
                archetypes,
                PluginContractCatalog.LIFECYCLE_ENTITY_NAMES,
                PluginContractCatalog.toPromptFragment(applySystem, types));
    }
}
```

- [ ] **Step 9: Run the tests to verify they pass**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.PluginWorkbenchAccessServiceTest' \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginContractServiceTest'
```

Expected: PASS, 14 tests.

- [ ] **Step 10: Commit**

```bash
git add server/modules-plugin/script-plugin-core/build.gradle \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/exception/ScriptPluginWorkbenchErrorDescEnum.java \
        server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/PluginCapabilitiesDTO.java \
        server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/PluginContractDTO.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/PluginWorkbenchAccessService.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginContractService.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/PluginWorkbenchAccessServiceTest.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginContractServiceTest.java
git commit -m "feat(script-plugin): add workbench role checks and contract endpoint support

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 4: Revision Provenance and AI Pipeline Gating

**Files:**
- Modify: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginService.java`
- Modify: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/service/AiDraftGenerationService.java`
- Modify: `server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/controller/AiDraftController.java`
- Modify: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java`
- Modify: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/domain/service/AiDraftGenerationServiceTest.java`
- Modify: `server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/AiDraftPipelineEndToEndTest.java`

**Interfaces:**
- Produces: `ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto, RevisionOriginEnum origin, String aiModel, String promptVersion, String actor)`.
- `ScriptPluginService` 5-argument overload delegates to the 6-argument overload with `UserContext.getCurrentUser()`; the 2-argument overload delegates to the 5-argument overload.
- AI service consumes `PluginWorkbenchAccessService.requireDeveloper(pluginKey, SAVE_REVISION)` and `ScriptPluginOperationAuditService.recordAs(...)`.

- [ ] **Step 1: Add failing provenance tests to `ScriptPluginServiceTest`**

Add the imports:

```java
import org.junit.jupiter.api.AfterEach;
import org.openwes.common.utils.user.UserContext;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
```

Add this cleanup method next to `setUp`:

```java
    @AfterEach
    void tearDown() {
        UserContext.removeUser();
    }
```

Add these tests:

```java
    private void stubRevisionSave() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DISABLED);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findTopByPluginIdOrderByRevisionNoDesc(1L)).thenReturn(Optional.empty());
        when(revisionRepository.save(any())).thenAnswer(invocation -> {
            ScriptPluginRevision revision = invocation.getArgument(0);
            revision.setId(12L);
            return revision;
        });
        when(pluginRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void saveRevision_manualSave_recordsTheCurrentUser() {
        UserContext.setAccount("bob");
        stubRevisionSave();

        ScriptPluginRevisionDTO saved = service.saveRevision("packing-wave", revisionDto());

        assertEquals("bob", saved.getCreatedBy());
    }

    @Test
    void saveRevision_explicitActor_overridesTheContext() {
        UserContext.setAccount("bob");
        stubRevisionSave();

        ScriptPluginRevisionDTO saved = service.saveRevision("packing-wave", revisionDto(),
                RevisionOriginEnum.MANUAL, null, null, "alice");

        assertEquals("alice", saved.getCreatedBy());
    }

    @Test
    void saveRevision_blankActor_recordsSystem() {
        stubRevisionSave();

        ScriptPluginRevisionDTO saved = service.saveRevision("packing-wave", revisionDto(),
                RevisionOriginEnum.MANUAL, null, null, " ");

        assertEquals("system", saved.getCreatedBy());
    }
```

- [ ] **Step 2: Run the tests to verify they fail**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest'
```

Expected: compilation failure — the 6-argument overload does not exist.

- [ ] **Step 3: Add the 6-argument overload and provenance**

In `ScriptPluginService.java`, add imports:

```java
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.common.utils.user.UserContext;
```

Replace the two existing `saveRevision` overloads with:

```java
    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto) {
        return saveRevision(pluginKey, dto, RevisionOriginEnum.MANUAL, null, null);
    }

    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto,
                                                RevisionOriginEnum origin, String aiModel, String promptVersion) {
        return saveRevision(pluginKey, dto, origin, aiModel, promptVersion,
                UserContext.getCurrentUser());
    }

    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto,
                                                RevisionOriginEnum origin, String aiModel, String promptVersion,
                                                String actor) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ARCHIVED) {
            throw new CommonException("archived plugins are read-only");
        }
        if (plugin.getStatus() == ScriptPluginStatusEnum.ENABLED) {
            throw new CommonException("disable the plugin before saving a new revision");
        }
        if (dto.getScripts() == null || dto.getScripts().isEmpty()) {
            throw new CommonException("a revision needs at least one script");
        }
        List<ExtensionDeclarationDTO> declarations = dto.getExtensionPoints() == null
                ? List.of() : dto.getExtensionPoints();

        int revisionNo = revisionRepository.findTopByPluginIdOrderByRevisionNoDesc(plugin.getId())
                .map(r -> r.getRevisionNo() + 1).orElse(1);

        ScriptPluginRevision revision = new ScriptPluginRevision();
        revision.setPluginId(plugin.getId());
        revision.setRevisionNo(revisionNo);
        revision.setScripts(writeJson(dto.getScripts()));
        revision.setExtensionPointsSnapshot(writeJson(declarations));
        revision.setConfigSchemaSnapshot(dto.getConfigSchema() == null ? null : writeJson(dto.getConfigSchema()));
        revision.setContentHash(ScriptPluginContentHasher.hash(declarations, dto.getScripts()));
        revision.setChangeSummary(dto.getChangeSummary() == null ? "" : dto.getChangeSummary());
        revision.setOrigin(origin);
        revision.setAiModel(aiModel);
        revision.setPromptVersion(promptVersion);
        revision.setCreatedBy(StringUtils.left(
                StringUtils.defaultIfBlank(actor, AuthConstants.USERNAME_SYSTEM), 60));
        revision.setCreatedAt(System.currentTimeMillis());
        revision = revisionRepository.save(revision);

        plugin.setCurrentRevisionId(revision.getId());
        plugin.setExtensionPoints(revision.getExtensionPointsSnapshot());
        plugin.setConfigSchema(revision.getConfigSchemaSnapshot());
        pluginRepository.save(plugin);
        publishManagementMessage(plugin);

        return toRevisionDto(revision);
    }
```

- [ ] **Step 4: Run the service tests to verify they pass**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest'
```

Expected: PASS, all existing tests plus 3 provenance tests.

- [ ] **Step 5: Add failing AI gating tests**

In `AiDraftGenerationServiceTest`, add imports:

```java
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.script.plugin.core.domain.service.PluginWorkbenchAccessService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginOperationAuditService;
import org.openwes.script.plugin.core.exception.ScriptPluginWorkbenchErrorDescEnum;
```

Add fields next to the other mocks:

```java
    private final PluginWorkbenchAccessService workbenchAccessService =
            mock(PluginWorkbenchAccessService.class);
    private final ScriptPluginOperationAuditService operationAuditService =
            mock(ScriptPluginOperationAuditService.class);
```

In `setUp`, after `properties.setModel("fake-model");`, add:

```java
        when(workbenchAccessService.requireDeveloper(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.SAVE_REVISION))).thenReturn("alice");
```

Change the save-revision stub to the 6-argument overload:

```java
        when(scriptPluginService.saveRevision(anyString(), any(), eq(RevisionOriginEnum.AI),
                anyString(), anyString(), anyString())).thenAnswer(invocation -> {
            ScriptPluginRevisionDTO dto = invocation.getArgument(1);
            dto.setId(55L);
            dto.setRevisionNo(4);
            return dto;
        });
```

Change `serviceWith` to:

```java
    private AiDraftGenerationService serviceWith(ChatModel chatModel) {
        return new AiDraftGenerationService(scriptPluginService, workbenchAccessService,
                operationAuditService, settingsService, quotaService, auditService,
                new AiPromptAssembler(properties), new AiDraftDocumentParser(),
                validator, groundingService, chatModel, properties);
    }
```

Update the two existing verifications to the 6-argument overload:

```java
        verify(scriptPluginService).saveRevision(eq("packing-wave"), any(), eq(RevisionOriginEnum.AI),
                eq("fake-model"), eq(PluginPromptTemplates.PROMPT_VERSION), eq("alice"));
```

```java
        verify(scriptPluginService, never()).saveRevision(anyString(), any(), any(),
                anyString(), anyString(), anyString());
```

Add these tests:

```java
    @Test
    void stream_withoutDeveloperRole_isRejectedBeforeAnyWork() {
        when(workbenchAccessService.requireDeveloper(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.SAVE_REVISION)))
                .thenThrow(new WmsException(
                        ScriptPluginWorkbenchErrorDescEnum.DEVELOPER_ROLE_REQUIRED));

        assertThrows(WmsException.class,
                () -> serviceWith(modelReturning(validWaveDraft())).stream(request()));

        verify(scriptPluginService, never()).detail(anyString());
        verify(quotaService, never()).tryAcquire(anyString(), anyInt());
        verify(auditService, never()).start(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void successfulGeneration_recordsSaveRevisionAuditForTheInitiatingActor() {
        serviceWith(modelReturning(validWaveDraft())).stream(request())
                .collectList().block(Duration.ofSeconds(10));

        verify(operationAuditService).recordAs(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.SAVE_REVISION), eq(55L), eq(true),
                anyString(), eq("alice"));
    }

    @Test
    void generations_requiresDeveloperRole() {
        serviceWith(modelReturning(validWaveDraft())).generations("packing-wave");

        verify(workbenchAccessService).requireDeveloper();
        verify(auditService).recent("packing-wave");
    }

    @Test
    void refreshCorpus_requiresDeveloperRole() {
        serviceWith(modelReturning(validWaveDraft())).refreshCorpus();

        verify(workbenchAccessService).requireDeveloper();
        verify(groundingService).refreshCorpus();
    }
```

- [ ] **Step 6: Run the AI tests to verify they fail**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-ai:ai-plugin:test \
  --tests 'org.openwes.ai.plugin.domain.service.AiDraftGenerationServiceTest'
```

Expected: compilation failure — the constructor and 6-argument overload do not match.

- [ ] **Step 7: Modify `AiDraftGenerationService`**

Add imports:

```java
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;
import org.openwes.script.plugin.core.domain.service.PluginWorkbenchAccessService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginOperationAuditService;
```

Remove the now-unused `org.apache.commons.lang3.StringUtils` import.

Replace the field list with:

```java
    private final ScriptPluginService scriptPluginService;
    private final PluginWorkbenchAccessService workbenchAccessService;
    private final ScriptPluginOperationAuditService operationAuditService;
    private final AiSettingsService settingsService;
    private final AiQuotaService quotaService;
    private final AiGenerationAuditService auditService;
    private final AiPromptAssembler promptAssembler;
    private final AiDraftDocumentParser parser;
    private final AiDraftValidator validator;
    private final PluginGroundingService groundingService;
    private final ChatModel chatModel;
    private final AiPluginProperties properties;
```

Replace the first two lines of `stream`:

```java
    public Flux<AiDraftStreamEvent> stream(AiDraftRequest request) {
        String user = workbenchAccessService.requireDeveloper(
                request.getPluginKey(), ScriptPluginAuditActionEnum.SAVE_REVISION);
        ScriptPluginDTO plugin = scriptPluginService.detail(request.getPluginKey());
```

Change the `GenerationContext` construction to pass the user:

```java
        GenerationContext context = new GenerationContext(request.getRequirement(), plugin, baseRevision,
                settings, maxTokens, audit.getId(), user);
```

Replace `generations` and `refreshCorpus`:

```java
    public List<AiGenerationDTO> generations(String pluginKey) {
        workbenchAccessService.requireDeveloper();
        return auditService.recent(pluginKey);
    }

    public int refreshCorpus() {
        workbenchAccessService.requireDeveloper();
        return groundingService.refreshCorpus();
    }
```

Replace `save` with:

```java
    private ScriptPluginRevisionDTO save(GenerationContext context, AiDraftDocument document) {
        ScriptPluginRevisionDTO revision = new ScriptPluginRevisionDTO();
        revision.setScripts(document.getScripts());
        revision.setExtensionPoints(document.getExtensions());
        revision.setConfigSchema(document.getConfigSchema());
        revision.setChangeSummary(document.getMeta().getChangeSummary());
        ScriptPluginRevisionDTO saved = scriptPluginService.saveRevision(
                context.plugin.getPluginKey(), revision, RevisionOriginEnum.AI,
                properties.getModel(), PluginPromptTemplates.PROMPT_VERSION, context.user);
        operationAuditService.recordAs(context.plugin.getPluginKey(),
                ScriptPluginAuditActionEnum.SAVE_REVISION, saved.getId(), true,
                "AI revision " + saved.getRevisionNo(), context.user);
        return saved;
    }
```

Add the `user` field to `GenerationContext` and its constructor:

```java
        private final String user;

        private GenerationContext(String requirement, ScriptPluginDTO plugin,
                                  ScriptPluginRevisionDTO baseRevision, AiSettings settings,
                                  int maxTokens, Long auditId, String user) {
            this.requirement = requirement;
            this.plugin = plugin;
            this.baseRevision = baseRevision;
            this.settings = settings;
            this.maxTokens = maxTokens;
            this.auditId = auditId;
            this.user = user;
        }
```

Update the `AiDraftController` class javadoc sentence to:

```java
 * Authentication is the gateway JWT today; workbench role checks run inside
 * {@link AiDraftGenerationService} so the same rules apply to direct service calls.
```

- [ ] **Step 8: Update the end-to-end AI test**

In `AiDraftPipelineEndToEndTest`, add the same imports for `PluginWorkbenchAccessService`, `ScriptPluginOperationAuditService`, and `ScriptPluginAuditActionEnum`.

Add fields:

```java
    private final PluginWorkbenchAccessService workbenchAccessService =
            mock(PluginWorkbenchAccessService.class);
    private final ScriptPluginOperationAuditService operationAuditService =
            mock(ScriptPluginOperationAuditService.class);
```

In `setUp`, after `properties.setModel("fake-model");`, add:

```java
        when(workbenchAccessService.requireDeveloper(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.SAVE_REVISION))).thenReturn("alice");
```

Change the save stub to 6 arguments:

```java
        when(scriptPluginService.saveRevision(anyString(), any(), eq(RevisionOriginEnum.AI),
                anyString(), anyString(), anyString())).thenAnswer(invocation -> {
```

Change the service construction to:

```java
        AiDraftGenerationService service = new AiDraftGenerationService(scriptPluginService,
                workbenchAccessService, operationAuditService, settingsService,
                quotaService, auditService, new AiPromptAssembler(properties),
                new AiDraftDocumentParser(), validator, groundingService, model, properties);
```

Change the verification to:

```java
        verify(scriptPluginService).saveRevision(eq("packing-wave"), any(), eq(RevisionOriginEnum.AI),
                eq("fake-model"), eq(PluginPromptTemplates.PROMPT_VERSION), eq("alice"));
```

- [ ] **Step 9: Run both test classes to verify they pass**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-ai:ai-plugin:test \
  --tests 'org.openwes.ai.plugin.domain.service.AiDraftGenerationServiceTest' \
  --tests 'org.openwes.ai.plugin.AiDraftPipelineEndToEndTest'
```

Expected: PASS, all tests in both classes.

- [ ] **Step 10: Commit**

```bash
git add server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginService.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java \
        server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/domain/service/AiDraftGenerationService.java \
        server/modules-ai/ai-plugin/src/main/java/org/openwes/ai/plugin/controller/AiDraftController.java \
        server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/domain/service/AiDraftGenerationServiceTest.java \
        server/modules-ai/ai-plugin/src/test/java/org/openwes/ai/plugin/AiDraftPipelineEndToEndTest.java
git commit -m "feat(script-plugin): record revision provenance and gate AI actions

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 5: Workbench Service and Metadata Update

**Files:**
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginMetadataDTO.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginWorkbenchService.java`
- Modify: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/entity/ScriptPlugin.java`
- Modify: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginService.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginWorkbenchServiceTest.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/entity/ScriptPluginStateTest.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java`

**Interfaces:**
- Consumes: `ScriptPluginService` methods from Task 4 and earlier `list/detail/revisions/createDraft/validate/enable/disable/archive/rollback`, access service from Task 3, audit service from Task 2.
- Produces:
  - `List<ScriptPluginDTO> ScriptPluginWorkbenchService.list()`.
  - `ScriptPluginDTO detail(String pluginKey)` / `List<ScriptPluginRevisionDTO> revisions(String pluginKey)`.
  - `ScriptPluginDTO createDraft(ScriptPluginDTO dto)`.
  - `ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto)`.
  - `ValidationReportDTO validate(String pluginKey)`.
  - `ScriptPluginDTO enable(String pluginKey)`.
  - `ScriptPluginDTO disable(String pluginKey, String reason)`.
  - `ScriptPluginDTO disableBySystem(String pluginKey, String reason)`.
  - `ScriptPluginDTO archive(String pluginKey)`.
  - `ScriptPluginDTO rollback(String pluginKey, long revisionId)`.
  - `ScriptPluginDTO updateMetadata(String pluginKey, ScriptPluginMetadataDTO dto)`.
  - `PageResultDTO<ScriptPluginOperationAuditDTO> auditForPlugin(String pluginKey, int page, int pageSize)`.
  - `PageResultDTO<ScriptPluginOperationAuditDTO> allAudit(int page, int pageSize)`.
  - `ScriptPluginDTO ScriptPluginService.updateMetadata(String pluginKey, String name, String description, String applyModule)`.

- [ ] **Step 1: Write the failing workbench-service test**

```java
package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginMetadataDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.openwes.script.plugin.core.exception.ScriptPluginWorkbenchErrorDescEnum;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ARCHIVE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.CONFIG_SAVE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.CREATE_DRAFT;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.DISABLE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ENABLE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.METADATA_UPDATE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ROLLBACK;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.SAVE_REVISION;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.VALIDATE;

@ExtendWith(MockitoExtension.class)
class ScriptPluginWorkbenchServiceTest {

    @Mock
    private ScriptPluginService pluginService;
    @Mock
    private PluginWorkbenchAccessService accessService;
    @Mock
    private ScriptPluginOperationAuditService auditService;

    private ScriptPluginWorkbenchService workbench;

    @BeforeEach
    void setUp() {
        workbench = new ScriptPluginWorkbenchService(pluginService, accessService, auditService);
    }

    private ScriptPluginDTO pluginDto() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setPluginKey("packing-wave");
        dto.setName("Packing status rule");
        dto.setDescription("");
        dto.setApplyModule("");
        dto.setCurrentRevisionId(11L);
        return dto;
    }

    @Test
    void list_requiresDeveloperRole() {
        when(pluginService.list()).thenReturn(List.of(pluginDto()));

        assertTrue(workbench.list().size() == 1);
        verify(accessService).requireDeveloper();
    }

    @Test
    void detail_requiresDeveloperRole() {
        ScriptPluginDTO dto = pluginDto();
        when(accessService.requireDeveloper()).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(dto);

        assertSame(dto, workbench.detail("packing-wave"));
    }

    @Test
    void createDraft_authorizesAndAudits() {
        ScriptPluginDTO request = pluginDto();
        ScriptPluginDTO created = pluginDto();
        when(accessService.requireDeveloper("packing-wave", CREATE_DRAFT)).thenReturn("alice");
        when(pluginService.createDraft(request)).thenReturn(created);

        assertSame(created, workbench.createDraft(request));
        verify(auditService).recordAs(eq("packing-wave"), eq(CREATE_DRAFT), isNull(),
                eq(true), anyString(), eq("alice"));
    }

    @Test
    void saveRevision_passesActorAndAuditsRevisionId() {
        ScriptPluginRevisionDTO request = new ScriptPluginRevisionDTO();
        ScriptPluginRevisionDTO saved = new ScriptPluginRevisionDTO();
        saved.setId(55L);
        saved.setRevisionNo(4);
        when(accessService.requireDeveloper("packing-wave", SAVE_REVISION)).thenReturn("alice");
        when(pluginService.saveRevision("packing-wave", request, RevisionOriginEnum.MANUAL,
                null, null, "alice")).thenReturn(saved);

        assertSame(saved, workbench.saveRevision("packing-wave", request));
        verify(auditService).recordAs("packing-wave", SAVE_REVISION, 55L, true, "revision 4", "alice");
    }

    @Test
    void validate_auditsPassedReport() {
        ValidationReportDTO report = ValidationReportDTO.passed("packing-wave", 11L, "wes");
        when(accessService.requireDeveloper()).thenReturn("alice");
        when(pluginService.validate("packing-wave")).thenReturn(report);

        assertSame(report, workbench.validate("packing-wave"));
        verify(auditService).recordAs("packing-wave", VALIDATE, 11L, true, "validation passed", "alice");
    }

    @Test
    void validate_auditsFailedReport() {
        ValidationReportDTO report = ValidationReportDTO.failed(
                "packing-wave", 11L, "wes", "compile", "unexpected token");
        when(accessService.requireDeveloper()).thenReturn("alice");
        when(pluginService.validate("packing-wave")).thenReturn(report);

        workbench.validate("packing-wave");

        verify(auditService).recordAs("packing-wave", VALIDATE, 11L, false,
                "validation failed at stage(s): compile", "alice");
    }

    @Test
    void enable_requiresAdminAndAuditsSuccess() {
        when(accessService.requireAdmin("packing-wave", ENABLE)).thenReturn("admin");
        when(pluginService.enable("packing-wave")).thenReturn(pluginDto());

        workbench.enable("packing-wave");

        verify(auditService).recordAs("packing-wave", ENABLE, 11L, true,
                "enabled revision 11", "admin");
    }

    @Test
    void enable_failureIsAuditedThenRethrown() {
        when(accessService.requireAdmin("packing-wave", ENABLE)).thenReturn("admin");
        when(pluginService.enable("packing-wave")).thenThrow(new CommonException("validation failed"));

        assertThrows(CommonException.class, () -> workbench.enable("packing-wave"));

        verify(auditService).recordAs("packing-wave", ENABLE, null, false,
                "enable failed: CommonException", "admin");
    }

    @Test
    void enable_developerRoleIsRejectedBeforeCallingTheDomainService() {
        when(accessService.requireAdmin("packing-wave", ENABLE))
                .thenThrow(new WmsException(
                        ScriptPluginWorkbenchErrorDescEnum.ADMIN_ROLE_REQUIRED));

        assertThrows(WmsException.class, () -> workbench.enable("packing-wave"));

        verifyNoInteractions(pluginService);
    }

    @Test
    void disable_defaultsBlankReasonAndAudits() {
        when(accessService.requireAdmin("packing-wave", DISABLE)).thenReturn("admin");
        when(pluginService.disable("packing-wave", "disabled by user")).thenReturn(pluginDto());

        workbench.disable("packing-wave", " ");

        verify(auditService).recordAs("packing-wave", DISABLE, 11L, true,
                "disabled: disabled by user", "admin");
    }

    @Test
    void disableBySystem_skipsAuthorizationAndAuditsSystem() {
        when(pluginService.disable("packing-wave", "host shutdown")).thenReturn(pluginDto());

        workbench.disableBySystem("packing-wave", "host shutdown");

        verifyNoInteractions(accessService);
        verify(auditService).recordAs("packing-wave", DISABLE, 11L, true,
                "disabled by system: host shutdown", "system");
    }

    @Test
    void archive_andRollback_requireAdminAndAudit() {
        when(accessService.requireAdmin("packing-wave", ARCHIVE)).thenReturn("admin");
        when(accessService.requireAdmin("packing-wave", ROLLBACK)).thenReturn("admin");
        when(pluginService.archive("packing-wave")).thenReturn(pluginDto());
        when(pluginService.rollback("packing-wave", 7L)).thenReturn(pluginDto());

        workbench.archive("packing-wave");
        workbench.rollback("packing-wave", 7L);

        verify(auditService).recordAs("packing-wave", ARCHIVE, 11L, true, "archived", "admin");
        verify(auditService).recordAs("packing-wave", ROLLBACK, 7L, true,
                "rolled back to revision 7", "admin");
    }

    @Test
    void metadataUpdate_auditsOnlyChangedFieldNames() {
        ScriptPluginDTO before = pluginDto();
        before.setDescription("old");
        ScriptPluginDTO after = pluginDto();
        after.setDescription("new");
        after.setApplyModule("mod");
        ScriptPluginMetadataDTO request = new ScriptPluginMetadataDTO();
        request.setName("Packing status rule");
        request.setDescription("new");
        request.setApplyModule("mod");
        when(accessService.requireDeveloper("packing-wave", METADATA_UPDATE)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(before);
        when(pluginService.updateMetadata("packing-wave", "Packing status rule", "new", "mod"))
                .thenReturn(after);

        assertSame(after, workbench.updateMetadata("packing-wave", request));

        verify(auditService).recordAs("packing-wave", METADATA_UPDATE, null, true,
                "changed fields: description, applyModule", "alice");
    }

    @Test
    void auditPages_areGatedByRole() {
        PageResultDTO<ScriptPluginOperationAuditDTO> page = PageResultDTO.of(List.of(), 0, 1, 20);
        when(auditService.pageForPlugin("packing-wave", 1, 20)).thenReturn(page);
        when(auditService.pageAll(1, 20)).thenReturn(page);

        workbench.auditForPlugin("packing-wave", 1, 20);
        workbench.allAudit(1, 20);

        verify(accessService).requireDeveloper();
        verify(accessService).requireAdmin();
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginWorkbenchServiceTest'
```

Expected: compilation failure — the DTO and service do not exist.

- [ ] **Step 3: Create the metadata DTO**

```java
package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScriptPluginMetadataDTO {
    @NotBlank
    @Size(max = 128)
    private String name;
    @Size(max = 512)
    private String description;
    @Size(max = 64)
    private String applyModule;
}
```

- [ ] **Step 4: Add the domain lifecycle method**

In `ScriptPlugin.java`, add:

```java
    public void updateMetadata(String name, String description, String applyModule) {
        if (status == ScriptPluginStatusEnum.ARCHIVED) {
            throw new IllegalStateException("archived plugins cannot be modified");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name is required");
        }
        this.name = name;
        this.description = description == null ? "" : description;
        this.applyModule = applyModule == null ? "" : applyModule;
    }
```

Add these tests to `ScriptPluginStateTest`:

```java
    @Test
    void updateMetadata_changesOnlyMetadataFields() {
        ScriptPlugin plugin = plugin(ScriptPluginStatusEnum.DRAFT);
        plugin.setApplySystem("wes");
        plugin.setApplyModule("old");

        plugin.updateMetadata("New name", "New description", "outbound");

        assertEquals("New name", plugin.getName());
        assertEquals("New description", plugin.getDescription());
        assertEquals("outbound", plugin.getApplyModule());
        assertEquals("wes", plugin.getApplySystem());
        assertEquals("packing-wave", plugin.getPluginKey());
    }

    @Test
    void updateMetadata_onArchivedPlugin_throws() {
        assertThrows(IllegalStateException.class,
                () -> plugin(ScriptPluginStatusEnum.ARCHIVED)
                        .updateMetadata("New name", null, null));
    }

    @Test
    void updateMetadata_requiresName() {
        assertThrows(IllegalArgumentException.class,
                () -> plugin(ScriptPluginStatusEnum.DRAFT).updateMetadata(" ", null, null));
    }
```

- [ ] **Step 5: Add `updateMetadata` to `ScriptPluginService`**

Add the method:

```java
    public ScriptPluginDTO updateMetadata(String pluginKey, String name, String description, String applyModule) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ARCHIVED) {
            throw new CommonException("archived plugins are read-only");
        }
        if (StringUtils.isBlank(name)) {
            throw new CommonException("name is required");
        }
        plugin.updateMetadata(name, description, applyModule);
        pluginRepository.save(plugin);
        publishManagementMessage(plugin);
        return toDto(plugin);
    }
```

Add these tests to `ScriptPluginServiceTest`:

```java
    @Test
    void updateMetadata_changesFieldsAndPublishes() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(pluginRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScriptPluginDTO dto = service.updateMetadata("packing-wave", "New name", "desc", "outbound");

        assertEquals("New name", dto.getName());
        assertEquals("desc", dto.getDescription());
        assertEquals("outbound", dto.getApplyModule());
        verify(pluginRepository).save(plugin);
        verify(redisUtils).publish(any(), any());
    }

    @Test
    void updateMetadata_onArchivedPlugin_isRejectedWithoutWriting() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.ARCHIVED);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));

        CommonException e = assertThrows(CommonException.class,
                () -> service.updateMetadata("packing-wave", "New name", null, null));

        assertEquals("archived plugins are read-only", e.getMessage());
        verify(pluginRepository, never()).save(any());
        verify(redisUtils, never()).publish(any(), any());
    }
```

- [ ] **Step 6: Create the workbench service**

```java
package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.user.AuthConstants;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginMetadataDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.api.enums.RevisionOriginEnum;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ARCHIVE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.CREATE_DRAFT;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.DISABLE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ENABLE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.METADATA_UPDATE;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.ROLLBACK;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.SAVE_REVISION;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.VALIDATE;

/**
 * The management-plane use-case boundary. Every operation authorizes first and audits the outcome;
 * controllers stay thin and the domain service stays free of user/role concerns.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginWorkbenchService {

    private final ScriptPluginService pluginService;
    private final PluginWorkbenchAccessService accessService;
    private final ScriptPluginOperationAuditService auditService;

    public List<ScriptPluginDTO> list() {
        accessService.requireDeveloper();
        return pluginService.list();
    }

    public ScriptPluginDTO detail(String pluginKey) {
        accessService.requireDeveloper();
        return pluginService.detail(pluginKey);
    }

    public List<ScriptPluginRevisionDTO> revisions(String pluginKey) {
        accessService.requireDeveloper();
        return pluginService.revisions(pluginKey);
    }

    public ScriptPluginDTO createDraft(ScriptPluginDTO dto) {
        String actor = accessService.requireDeveloper(dto.getPluginKey(), CREATE_DRAFT);
        ScriptPluginDTO created = pluginService.createDraft(dto);
        auditService.recordAs(created.getPluginKey(), CREATE_DRAFT, null, true,
                "draft created", actor);
        return created;
    }

    public ScriptPluginRevisionDTO saveRevision(String pluginKey, ScriptPluginRevisionDTO dto) {
        String actor = accessService.requireDeveloper(pluginKey, SAVE_REVISION);
        ScriptPluginRevisionDTO saved = pluginService.saveRevision(pluginKey, dto,
                RevisionOriginEnum.MANUAL, null, null, actor);
        auditService.recordAs(pluginKey, SAVE_REVISION, saved.getId(), true,
                "revision " + saved.getRevisionNo(), actor);
        return saved;
    }

    public ValidationReportDTO validate(String pluginKey) {
        String actor = accessService.requireDeveloper();
        try {
            ValidationReportDTO report = pluginService.validate(pluginKey);
            auditService.recordAs(pluginKey, VALIDATE, report.getRevisionId(), report.isPassed(),
                    report.isPassed() ? "validation passed"
                            : "validation failed at stage(s): " + failureStages(report),
                    actor);
            return report;
        } catch (RuntimeException e) {
            auditService.recordAs(pluginKey, VALIDATE, null, false,
                    "validation failed before a report was produced", actor);
            throw e;
        }
    }

    public ScriptPluginDTO enable(String pluginKey) {
        String actor = accessService.requireAdmin(pluginKey, ENABLE);
        try {
            ScriptPluginDTO enabled = pluginService.enable(pluginKey);
            auditService.recordAs(pluginKey, ENABLE, enabled.getCurrentRevisionId(), true,
                    "enabled revision " + enabled.getCurrentRevisionId(), actor);
            return enabled;
        } catch (RuntimeException e) {
            auditService.recordAs(pluginKey, ENABLE, null, false,
                    "enable failed: " + e.getClass().getSimpleName(), actor);
            throw e;
        }
    }

    public ScriptPluginDTO disable(String pluginKey, String reason) {
        String actor = accessService.requireAdmin(pluginKey, DISABLE);
        String effectiveReason = StringUtils.defaultIfBlank(reason, "disabled by user");
        ScriptPluginDTO disabled = pluginService.disable(pluginKey, effectiveReason);
        auditService.recordAs(pluginKey, DISABLE, disabled.getCurrentRevisionId(), true,
                "disabled: " + effectiveReason, actor);
        return disabled;
    }

    /** Internal path used by the Dubbo API (for example the cron circuit breaker); no user role. */
    public ScriptPluginDTO disableBySystem(String pluginKey, String reason) {
        String effectiveReason = StringUtils.defaultIfBlank(reason, "disabled by system");
        ScriptPluginDTO disabled = pluginService.disable(pluginKey, effectiveReason);
        auditService.recordAs(pluginKey, DISABLE, disabled.getCurrentRevisionId(), true,
                "disabled by system: " + effectiveReason, AuthConstants.USERNAME_SYSTEM);
        return disabled;
    }

    public ScriptPluginDTO archive(String pluginKey) {
        String actor = accessService.requireAdmin(pluginKey, ARCHIVE);
        ScriptPluginDTO archived = pluginService.archive(pluginKey);
        auditService.recordAs(pluginKey, ARCHIVE, archived.getCurrentRevisionId(), true,
                "archived", actor);
        return archived;
    }

    public ScriptPluginDTO rollback(String pluginKey, long revisionId) {
        String actor = accessService.requireAdmin(pluginKey, ROLLBACK);
        ScriptPluginDTO rolledBack = pluginService.rollback(pluginKey, revisionId);
        auditService.recordAs(pluginKey, ROLLBACK, revisionId, true,
                "rolled back to revision " + revisionId, actor);
        return rolledBack;
    }

    public ScriptPluginDTO updateMetadata(String pluginKey, ScriptPluginMetadataDTO dto) {
        String actor = accessService.requireDeveloper(pluginKey, METADATA_UPDATE);
        ScriptPluginDTO before = pluginService.detail(pluginKey);
        ScriptPluginDTO updated = pluginService.updateMetadata(pluginKey,
                dto.getName(), dto.getDescription(), dto.getApplyModule());
        List<String> changed = new ArrayList<>();
        if (!Objects.equals(before.getName(), updated.getName())) {
            changed.add("name");
        }
        if (!Objects.equals(normalize(before.getDescription()), normalize(updated.getDescription()))) {
            changed.add("description");
        }
        if (!Objects.equals(normalize(before.getApplyModule()), normalize(updated.getApplyModule()))) {
            changed.add("applyModule");
        }
        auditService.recordAs(pluginKey, METADATA_UPDATE, null, true,
                changed.isEmpty() ? "no field changed" : "changed fields: " + String.join(", ", changed),
                actor);
        return updated;
    }

    public PageResultDTO<ScriptPluginOperationAuditDTO> auditForPlugin(String pluginKey,
                                                                       int page, int pageSize) {
        accessService.requireDeveloper();
        return auditService.pageForPlugin(pluginKey, page, pageSize);
    }

    public PageResultDTO<ScriptPluginOperationAuditDTO> allAudit(int page, int pageSize) {
        accessService.requireAdmin();
        return auditService.pageAll(page, pageSize);
    }

    private static String failureStages(ValidationReportDTO report) {
        if (report.getFailures() == null || report.getFailures().isEmpty()) {
            return "unknown";
        }
        return report.getFailures().stream()
                .map(ValidationReportDTO.ValidationFailureDTO::getStage)
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private static String normalize(String value) {
        return value == null ? "" : value;
    }
}
```

- [ ] **Step 7: Run the tests to verify they pass**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginWorkbenchServiceTest' \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest' \
  --tests 'org.openwes.script.plugin.core.domain.entity.ScriptPluginStateTest'
```

Expected: PASS. `ScriptPluginServiceTest` includes the Task 4 provenance tests.

- [ ] **Step 8: Commit**

```bash
git add server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginMetadataDTO.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/entity/ScriptPlugin.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginService.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginWorkbenchService.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/entity/ScriptPluginStateTest.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginWorkbenchServiceTest.java
git commit -m "feat(script-plugin): route management actions through an authorized workbench service

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 6: Controller Swap, Capabilities, Contract, Metadata, and Audit Endpoints

**Files:**
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginDisableDTO.java`
- Modify: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/controller/ScriptPluginController.java`
- Modify: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/application/ScriptPluginApiImpl.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/controller/ScriptPluginControllerTest.java`

**Interfaces:**
- Consumes: `ScriptPluginWorkbenchService`, `PluginWorkbenchAccessService.capabilities()`, `ScriptPluginContractService.contract(String)`.
- Produces: REST endpoints from spec §4.9; `POST /{pluginKey}/disable` accepts an optional `{ "reason": "..." }` body; `ScriptPluginApiImpl.disable` uses the system bypass.

- [ ] **Step 1: Rewrite the failing controller test**

Replace `ScriptPluginControllerTest.java` with:

```java
package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.PluginCapabilitiesDTO;
import org.openwes.script.plugin.api.dto.PluginContractDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDisableDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginMetadataDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginStatusEnum;
import org.openwes.script.plugin.core.domain.service.PluginWorkbenchAccessService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginContractService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginWorkbenchService;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginControllerTest {

    private final ScriptPluginWorkbenchService workbenchService =
            mock(ScriptPluginWorkbenchService.class);
    private final PluginWorkbenchAccessService accessService =
            mock(PluginWorkbenchAccessService.class);
    private final ScriptPluginContractService contractService =
            mock(ScriptPluginContractService.class);
    private final ScriptPluginController controller =
            new ScriptPluginController(workbenchService, accessService, contractService);

    @Test
    void list_wrapsServiceResultInSuccessResponse() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setPluginKey("packing-wave");
        dto.setStatus(ScriptPluginStatusEnum.DRAFT);
        when(workbenchService.list()).thenReturn(List.of(dto));

        Response<List<ScriptPluginDTO>> response = controller.list();

        assertEquals(Response.SUCCESS_CODE, response.getCode());
        assertEquals(1, response.getData().size());
        assertEquals("packing-wave", response.getData().get(0).getPluginKey());
    }

    @Test
    void capabilities_returnsCurrentUserCapabilities() {
        PluginCapabilitiesDTO dto = new PluginCapabilitiesDTO(true, false, Set.of("pluginDeveloper"));
        when(accessService.capabilities()).thenReturn(dto);

        assertEquals(dto, controller.capabilities().getData());
    }

    @Test
    void contract_delegatesWithApplySystem() {
        PluginContractDTO dto = new PluginContractDTO();
        when(contractService.contract("wes")).thenReturn(dto);

        assertEquals(dto, controller.contract("wes").getData());
    }

    @Test
    void enable_delegatesToWorkbenchService() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(workbenchService.enable("packing-wave")).thenReturn(dto);

        Response<ScriptPluginDTO> response = controller.enable("packing-wave");

        assertEquals(Response.SUCCESS_CODE, response.getCode());
        verify(workbenchService).enable("packing-wave");
    }

    @Test
    void disable_passesOptionalReason() {
        ScriptPluginDisableDTO body = new ScriptPluginDisableDTO();
        body.setReason("maintenance window");
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(workbenchService.disable("packing-wave", "maintenance window")).thenReturn(dto);

        controller.disable("packing-wave", body);

        verify(workbenchService).disable("packing-wave", "maintenance window");
    }

    @Test
    void disable_withoutBody_passesNullReason() {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(workbenchService.disable("packing-wave", null)).thenReturn(dto);

        controller.disable("packing-wave", null);

        verify(workbenchService).disable("packing-wave", null);
    }

    @Test
    void metadata_delegatesToWorkbenchService() {
        ScriptPluginMetadataDTO body = new ScriptPluginMetadataDTO();
        body.setName("New name");
        ScriptPluginDTO dto = new ScriptPluginDTO();
        when(workbenchService.updateMetadata("packing-wave", body)).thenReturn(dto);

        assertEquals(dto, controller.metadata("packing-wave", body).getData());
    }

    @Test
    void audit_delegatesWithPagination() {
        controller.audit("packing-wave", 2, 50);
        controller.allAudit(3, 10);

        verify(workbenchService).auditForPlugin("packing-wave", 2, 50);
        verify(workbenchService).allAudit(3, 10);
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.controller.ScriptPluginControllerTest'
```

Expected: compilation failure — the controller constructor and methods do not match.

- [ ] **Step 3: Create the disable DTO**

```java
package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ScriptPluginDisableDTO {
    @Size(max = 255)
    private String reason;
}
```

- [ ] **Step 4: Rewrite the controller**

```java
package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.dto.PluginCapabilitiesDTO;
import org.openwes.script.plugin.api.dto.PluginContractDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDisableDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginMetadataDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginOperationAuditDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginRevisionDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.core.domain.service.PluginWorkbenchAccessService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginContractService;
import org.openwes.script.plugin.core.domain.service.ScriptPluginWorkbenchService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Management REST surface. Mapped under the reserved `plugin-platform` prefix; the gateway route
 * for /plugin-platform/** forwards without a RewritePath filter, so this prefix is stable both
 * through the gateway and on the direct service port.
 *
 * <p>All authorization lives in the service layer; these methods only bind and delegate.
 */
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Platform Api")
public class ScriptPluginController {

    private final ScriptPluginWorkbenchService workbenchService;
    private final PluginWorkbenchAccessService accessService;
    private final ScriptPluginContractService contractService;

    @GetMapping("/capabilities")
    public Response<PluginCapabilitiesDTO> capabilities() {
        return Response.success(accessService.capabilities());
    }

    @GetMapping("/contract")
    public Response<PluginContractDTO> contract(@RequestParam("applySystem") String applySystem) {
        return Response.success(contractService.contract(applySystem));
    }

    @GetMapping("/list")
    public Response<List<ScriptPluginDTO>> list() {
        return Response.success(workbenchService.list());
    }

    @GetMapping("/detail/{pluginKey}")
    public Response<ScriptPluginDTO> detail(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.detail(pluginKey));
    }

    @PostMapping("/createDraft")
    public Response<ScriptPluginDTO> createDraft(@Valid @RequestBody ScriptPluginDTO dto) {
        return Response.success(workbenchService.createDraft(dto));
    }

    @PostMapping("/{pluginKey}/revision")
    public Response<ScriptPluginRevisionDTO> saveRevision(@PathVariable("pluginKey") String pluginKey,
                                                          @Valid @RequestBody ScriptPluginRevisionDTO dto) {
        return Response.success(workbenchService.saveRevision(pluginKey, dto));
    }

    @GetMapping("/{pluginKey}/revisions")
    public Response<List<ScriptPluginRevisionDTO>> revisions(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.revisions(pluginKey));
    }

    @PostMapping("/{pluginKey}/validate")
    public Response<ValidationReportDTO> validate(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.validate(pluginKey));
    }

    @PostMapping("/{pluginKey}/enable")
    public Response<ScriptPluginDTO> enable(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.enable(pluginKey));
    }

    @PostMapping("/{pluginKey}/disable")
    public Response<ScriptPluginDTO> disable(
            @PathVariable("pluginKey") String pluginKey,
            @Valid @RequestBody(required = false) ScriptPluginDisableDTO dto) {
        return Response.success(workbenchService.disable(pluginKey,
                dto == null ? null : dto.getReason()));
    }

    @PostMapping("/{pluginKey}/archive")
    public Response<ScriptPluginDTO> archive(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(workbenchService.archive(pluginKey));
    }

    @PostMapping("/{pluginKey}/rollback/{revisionId}")
    public Response<ScriptPluginDTO> rollback(@PathVariable("pluginKey") String pluginKey,
                                              @PathVariable("revisionId") long revisionId) {
        return Response.success(workbenchService.rollback(pluginKey, revisionId));
    }

    @PostMapping("/{pluginKey}/metadata")
    public Response<ScriptPluginDTO> metadata(@PathVariable("pluginKey") String pluginKey,
                                              @Valid @RequestBody ScriptPluginMetadataDTO dto) {
        return Response.success(workbenchService.updateMetadata(pluginKey, dto));
    }

    @GetMapping("/{pluginKey}/audit")
    public Response<PageResultDTO<ScriptPluginOperationAuditDTO>> audit(
            @PathVariable("pluginKey") String pluginKey,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return Response.success(workbenchService.auditForPlugin(pluginKey, page, pageSize));
    }

    @GetMapping("/audit")
    public Response<PageResultDTO<ScriptPluginOperationAuditDTO>> allAudit(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return Response.success(workbenchService.allAudit(page, pageSize));
    }
}
```

- [ ] **Step 5: Route the Dubbo disable path through the system bypass**

In `ScriptPluginApiImpl.java`, add the field and change the `disable` method:

```java
    private final ScriptPluginWorkbenchService workbenchService;
```

```java
    @Override
    public void disable(String pluginKey, String reason) {
        workbenchService.disableBySystem(pluginKey, reason);
    }
```

Remove the now-unused `ScriptPluginService` field only if no other method uses it. `getEnabledPlugins`, `getRegistryVersion`, `getPendingValidation` and `reportValidation` still use it, so keep it.

- [ ] **Step 6: Run the controller and related tests**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.controller.ScriptPluginControllerTest' \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginWorkbenchServiceTest'
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginDisableDTO.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/controller/ScriptPluginController.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/application/ScriptPluginApiImpl.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/controller/ScriptPluginControllerTest.java
git commit -m "feat(script-plugin): expose capabilities, contract, metadata and audit endpoints

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 7: Config-Value APIs with Schema Validation and Hot Reload

**Files:**
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginConfigDTO.java`
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginConfigSaveDTO.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginConfigValidator.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginConfigService.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/controller/ScriptPluginConfigController.java`
- Modify: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginService.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginConfigValidatorTest.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginConfigServiceTest.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/controller/ScriptPluginConfigControllerTest.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java`

**Interfaces:**
- Consumes: `ConfigFieldDTO`, `ScriptPluginConfig`, `ScriptPluginConfigRepository`, `PluginWorkbenchAccessService`, `ScriptPluginOperationAuditService`.
- Produces:
  - `ScriptPluginConfigDTO { Map<String,Object> values; List<ConfigFieldDTO> schema; Long lockVersion; }`.
  - `ScriptPluginConfigSaveDTO { @NotNull Map<String,Object> values; Long lockVersion; }`.
  - `ScriptPluginConfigValidator.validate(List<ConfigFieldDTO>, Map<String,Object>)`.
  - `ScriptPluginService.getConfig(String)` / `saveConfig(String, Map<String,Object>, Long)`.
  - `ScriptPluginConfigService.getConfig(String)` / `saveConfig(String, ScriptPluginConfigSaveDTO)`.
  - `GET|POST /plugin-platform/scriptPlugin/{pluginKey}/config`.

- [ ] **Step 1: Write the failing validator test**

```java
package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScriptPluginConfigValidatorTest {

    private ConfigFieldDTO field(String name, String type, boolean required) {
        ConfigFieldDTO field = new ConfigFieldDTO();
        field.setField(name);
        field.setType(type);
        field.setRequired(required);
        return field;
    }

    @Test
    void validValues_pass() {
        List<ConfigFieldDTO> schema = List.of(
                field("maxPerWave", "INT", true),
                field("threshold", "NUMBER", false),
                field("enabled", "BOOLEAN", false),
                field("label", "STRING", false));

        assertDoesNotThrow(() -> ScriptPluginConfigValidator.validate(schema,
                Map.of("maxPerWave", 20, "threshold", 1.5, "enabled", true, "label", "wave")));
    }

    @Test
    void unknownKey_isRejected() {
        CommonException e = assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(List.of(field("known", "STRING", false)),
                        Map.of("other", "x")));

        assertEquals("unknown config field: other", e.getMessage());
    }

    @Test
    void missingRequiredField_isRejected() {
        CommonException e = assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(List.of(field("maxPerWave", "INT", true)),
                        Map.of()));

        assertEquals("config field 'maxPerWave' is required", e.getMessage());
    }

    @Test
    void intField_acceptsIntegralStringsAndRejectsFractions() {
        List<ConfigFieldDTO> schema = List.of(field("maxPerWave", "INT", false));

        assertDoesNotThrow(() -> ScriptPluginConfigValidator.validate(schema, Map.of("maxPerWave", "42")));
        assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(schema, Map.of("maxPerWave", 1.5)));
    }

    @Test
    void numberField_acceptsDecimalsAndRejectsText() {
        List<ConfigFieldDTO> schema = List.of(field("threshold", "NUMBER", false));

        assertDoesNotThrow(() -> ScriptPluginConfigValidator.validate(schema, Map.of("threshold", "1.25")));
        assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(schema, Map.of("threshold", "abc")));
    }

    @Test
    void booleanField_acceptsOnlyTrueOrFalse() {
        List<ConfigFieldDTO> schema = List.of(field("enabled", "BOOLEAN", false));

        assertDoesNotThrow(() -> ScriptPluginConfigValidator.validate(schema, Map.of("enabled", "false")));
        assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(schema, Map.of("enabled", "yes")));
    }

    @Test
    void stringField_requiresText() {
        List<ConfigFieldDTO> schema = List.of(field("label", "STRING", false));

        assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(schema, Map.of("label", 5)));
    }

    @Test
    void unsupportedType_isRejectedAsSchemaError() {
        CommonException e = assertThrows(CommonException.class,
                () -> ScriptPluginConfigValidator.validate(List.of(field("when", "DATE", false)),
                        Map.of("when", "2026-10-23")));

        assertEquals("unsupported config field type 'DATE' for field 'when'", e.getMessage());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginConfigValidatorTest'
```

Expected: compilation failure — the validator does not exist.

- [ ] **Step 3: Create the config DTOs and validator**

```java
package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptPluginConfigDTO {
    private Map<String, Object> values;
    private List<ConfigFieldDTO> schema;
    private Long lockVersion;
}
```

```java
package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
public class ScriptPluginConfigSaveDTO {
    @NotNull
    private Map<String, Object> values;
    private Long lockVersion;
}
```

```java
package org.openwes.script.plugin.core.domain.service;

import org.apache.commons.lang3.StringUtils;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Stateless config-schema validation. Error messages name the field and expected type; they never
 * echo submitted values, which may be sensitive.
 */
public final class ScriptPluginConfigValidator {

    private ScriptPluginConfigValidator() {
    }

    public static void validate(List<ConfigFieldDTO> schema, Map<String, Object> values) {
        List<ConfigFieldDTO> fields = schema == null ? List.of() : schema;
        Map<String, Object> submitted = values == null ? Map.of() : values;
        Set<String> known = new HashSet<>();
        for (ConfigFieldDTO field : fields) {
            if (field == null || StringUtils.isBlank(field.getField())) {
                throw new CommonException("config schema contains a field without a name");
            }
            if (!known.add(field.getField())) {
                throw new CommonException("config schema contains duplicate field '" + field.getField() + "'");
            }
        }
        for (String key : submitted.keySet()) {
            if (!known.contains(key)) {
                throw new CommonException("unknown config field: " + key);
            }
        }
        for (ConfigFieldDTO field : fields) {
            Object value = submitted.get(field.getField());
            if (field.isRequired() && value == null) {
                throw new CommonException("config field '" + field.getField() + "' is required");
            }
            if (value != null) {
                validateType(field, value);
            }
        }
    }

    private static void validateType(ConfigFieldDTO field, Object value) {
        String type = StringUtils.upperCase(StringUtils.trimToEmpty(field.getType()));
        boolean valid = switch (type) {
            case "STRING" -> value instanceof CharSequence;
            case "INT", "INTEGER" -> isInteger(value);
            case "NUMBER" -> isNumber(value);
            case "BOOLEAN" -> isBoolean(value);
            default -> throw new CommonException("unsupported config field type '"
                    + field.getType() + "' for field '" + field.getField() + "'");
        };
        if (!valid) {
            throw new CommonException("config field '" + field.getField() + "' must be a " + type);
        }
    }

    private static boolean isInteger(Object value) {
        try {
            new BigDecimal(value.toString()).intValueExact();
            return true;
        } catch (NumberFormatException | ArithmeticException e) {
            return false;
        }
    }

    private static boolean isNumber(Object value) {
        try {
            new BigDecimal(value.toString());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isBoolean(Object value) {
        if (value instanceof Boolean) {
            return true;
        }
        if (!(value instanceof String text)) {
            return false;
        }
        return "true".equalsIgnoreCase(text) || "false".equalsIgnoreCase(text);
    }
}
```

- [ ] **Step 4: Add config read/write to `ScriptPluginService`**

Add these methods after `updateMetadata`:

```java
    public ScriptPluginConfigDTO getConfig(String pluginKey) {
        ScriptPlugin plugin = require(pluginKey);
        ScriptPluginConfigDTO dto = new ScriptPluginConfigDTO();
        dto.setValues(resolveConfig(plugin));
        dto.setSchema(readConfigSchema(plugin));
        dto.setLockVersion(configRepository.findByPluginId(plugin.getId())
                .map(ScriptPluginConfig::getLockVersion).orElse(null));
        return dto;
    }

    public ScriptPluginConfigDTO saveConfig(String pluginKey, Map<String, Object> values, Long lockVersion) {
        ScriptPlugin plugin = require(pluginKey);
        if (plugin.getStatus() == ScriptPluginStatusEnum.ARCHIVED) {
            throw new CommonException("archived plugins are read-only");
        }
        Map<String, Object> safeValues = values == null ? Map.of() : values;
        ScriptPluginConfigValidator.validate(readConfigSchema(plugin), safeValues);

        ScriptPluginConfig row = configRepository.findByPluginId(plugin.getId()).orElse(null);
        if (row == null) {
            if (lockVersion != null) {
                throw new CommonException(
                        "config was modified by another user; current lockVersion is null");
            }
            row = new ScriptPluginConfig();
            row.setPluginId(plugin.getId());
        } else if (!Objects.equals(row.getLockVersion(), lockVersion)) {
            throw new CommonException("config was modified by another user; current lockVersion is "
                    + row.getLockVersion());
        }
        row.setConfigJson(writeJson(safeValues));
        configRepository.save(row);
        publishManagementMessage(plugin);
        return getConfig(pluginKey);
    }

    private List<ConfigFieldDTO> readConfigSchema(ScriptPlugin plugin) {
        if (plugin.getConfigSchema() == null) {
            return List.of();
        }
        return readJson(plugin.getConfigSchema(), new TypeReference<>() {
        });
    }
```

- [ ] **Step 5: Add failing config tests to `ScriptPluginServiceTest`**

Add these tests:

```java
    @Test
    void getConfig_mergesSchemaDefaultsAndReturnsLockVersion() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[{\"field\":\"maxPerWave\",\"type\":\"INT\",\"required\":true,\"defaultValue\":\"20\"}]");
        ScriptPluginConfig row = new ScriptPluginConfig();
        row.setPluginId(1L);
        row.setConfigJson("{\"maxPerWave\":25}");
        row.setLockVersion(3L);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.of(row));

        ScriptPluginConfigDTO dto = service.getConfig("packing-wave");

        assertEquals(25, dto.getValues().get("maxPerWave"));
        assertEquals("maxPerWave", dto.getSchema().get(0).getField());
        assertEquals(3L, dto.getLockVersion());
    }

    @Test
    void saveConfig_withMatchingLockVersion_updatesAndPublishes() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[{\"field\":\"maxPerWave\",\"type\":\"INT\",\"required\":true}]");
        ScriptPluginConfig row = new ScriptPluginConfig();
        row.setPluginId(1L);
        row.setConfigJson("{\"maxPerWave\":20}");
        row.setLockVersion(3L);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.of(row));
        when(configRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        ScriptPluginConfigDTO saved = service.saveConfig("packing-wave", Map.of("maxPerWave", 30), 3L);

        assertEquals(30, saved.getValues().get("maxPerWave"));
        verify(configRepository).save(row);
        verify(redisUtils).publish(any(), any());
    }

    @Test
    void saveConfig_withStaleLockVersion_isRejectedWithoutWriting() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[{\"field\":\"maxPerWave\",\"type\":\"INT\",\"required\":true}]");
        ScriptPluginConfig row = new ScriptPluginConfig();
        row.setPluginId(1L);
        row.setConfigJson("{\"maxPerWave\":20}");
        row.setLockVersion(4L);
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.of(row));

        CommonException e = assertThrows(CommonException.class,
                () -> service.saveConfig("packing-wave", Map.of("maxPerWave", 30), 3L));

        assertEquals("config was modified by another user; current lockVersion is 4", e.getMessage());
        verify(configRepository, never()).save(any());
        verify(redisUtils, never()).publish(any(), any());
    }

    @Test
    void saveConfig_withUnknownField_isRejected() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[]");
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));

        assertThrows(CommonException.class,
                () -> service.saveConfig("packing-wave", Map.of("unknown", 1), null));
        verify(configRepository, never()).save(any());
    }

    @Test
    void saveConfig_whenAbsent_insertsARowWithThePluginId() {
        ScriptPlugin plugin = plugin("packing-wave", "wes", ScriptPluginStatusEnum.DRAFT);
        plugin.setConfigSchema("[{\"field\":\"maxPerWave\",\"type\":\"INT\",\"required\":true}]");
        when(pluginRepository.findByPluginKey("packing-wave")).thenReturn(Optional.of(plugin));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        when(configRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        service.saveConfig("packing-wave", Map.of("maxPerWave", 30), null);

        ArgumentCaptor<ScriptPluginConfig> captor = ArgumentCaptor.forClass(ScriptPluginConfig.class);
        verify(configRepository).save(captor.capture());
        assertEquals(1L, captor.getValue().getPluginId());
        assertTrue(captor.getValue().getConfigJson().contains("30"));
    }
```

Add imports to the test: `org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO` and `org.openwes.script.plugin.core.domain.entity.ScriptPluginConfig`.

- [ ] **Step 6: Run the service tests**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest' \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginConfigValidatorTest'
```

Expected: PASS.

- [ ] **Step 7: Write the failing config-service test**

```java
package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigSaveDTO;
import org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginConfigServiceTest {

    @Mock
    private ScriptPluginService pluginService;
    @Mock
    private PluginWorkbenchAccessService accessService;
    @Mock
    private ScriptPluginOperationAuditService auditService;

    private ScriptPluginConfigService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginConfigService(pluginService, accessService, auditService);
    }

    @Test
    void getConfig_requiresAdminAndDelegates() {
        ScriptPluginConfigDTO dto = new ScriptPluginConfigDTO(Map.of(), List.of(), 3L);
        when(accessService.requireAdmin()).thenReturn("admin");
        when(pluginService.getConfig("packing-wave")).thenReturn(dto);

        assertSame(dto, service.getConfig("packing-wave"));
    }

    @Test
    void saveConfig_auditsFieldNamesOnly() {
        ScriptPluginConfigSaveDTO request = new ScriptPluginConfigSaveDTO();
        request.setValues(Map.of("maxPerWave", 25, "enabled", true));
        request.setLockVersion(3L);
        ScriptPluginConfigDTO saved = new ScriptPluginConfigDTO(request.getValues(), List.of(), 4L);
        when(accessService.requireAdmin("packing-wave", ScriptPluginAuditActionEnum.CONFIG_SAVE))
                .thenReturn("admin");
        when(pluginService.saveConfig("packing-wave", request.getValues(), 3L)).thenReturn(saved);

        assertSame(saved, service.saveConfig("packing-wave", request));

        verify(auditService).recordAs(eq("packing-wave"),
                eq(ScriptPluginAuditActionEnum.CONFIG_SAVE), eq(null), eq(true),
                eq("saved config fields: enabled, maxPerWave"), eq("admin"));
    }
}
```

- [ ] **Step 8: Create the config service**

```java
package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigSaveDTO;
import org.springframework.stereotype.Service;

import java.util.TreeSet;

import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.CONFIG_SAVE;

@Service
@RequiredArgsConstructor
public class ScriptPluginConfigService {

    private final ScriptPluginService pluginService;
    private final PluginWorkbenchAccessService accessService;
    private final ScriptPluginOperationAuditService auditService;

    public ScriptPluginConfigDTO getConfig(String pluginKey) {
        accessService.requireAdmin();
        return pluginService.getConfig(pluginKey);
    }

    public ScriptPluginConfigDTO saveConfig(String pluginKey, ScriptPluginConfigSaveDTO dto) {
        String actor = accessService.requireAdmin(pluginKey, CONFIG_SAVE);
        ScriptPluginConfigDTO saved = pluginService.saveConfig(pluginKey, dto.getValues(),
                dto.getLockVersion());
        String fields = dto.getValues() == null ? "" : String.join(", ",
                new TreeSet<>(dto.getValues().keySet()));
        auditService.recordAs(pluginKey, CONFIG_SAVE, null, true,
                fields.isEmpty() ? "saved config with no fields" : "saved config fields: " + fields,
                actor);
        return saved;
    }
}
```

- [ ] **Step 9: Create the controller and its test**

```java
package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigSaveDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginConfigService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Config Api")
public class ScriptPluginConfigController {

    private final ScriptPluginConfigService configService;

    @GetMapping("/{pluginKey}/config")
    public Response<ScriptPluginConfigDTO> getConfig(@PathVariable("pluginKey") String pluginKey) {
        return Response.success(configService.getConfig(pluginKey));
    }

    @PostMapping("/{pluginKey}/config")
    public Response<ScriptPluginConfigDTO> saveConfig(
            @PathVariable("pluginKey") String pluginKey,
            @Valid @RequestBody ScriptPluginConfigSaveDTO dto) {
        return Response.success(configService.saveConfig(pluginKey, dto));
    }
}
```

```java
package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigSaveDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginConfigService;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginConfigControllerTest {

    private final ScriptPluginConfigService service = mock(ScriptPluginConfigService.class);
    private final ScriptPluginConfigController controller = new ScriptPluginConfigController(service);

    @Test
    void getConfig_returnsServiceResult() {
        ScriptPluginConfigDTO dto = new ScriptPluginConfigDTO(Map.of("maxPerWave", 20), List.of(), 3L);
        when(service.getConfig("packing-wave")).thenReturn(dto);

        assertEquals(dto, controller.getConfig("packing-wave").getData());
    }

    @Test
    void saveConfig_delegates() {
        ScriptPluginConfigSaveDTO body = new ScriptPluginConfigSaveDTO();
        body.setValues(Map.of("maxPerWave", 25));
        body.setLockVersion(3L);
        ScriptPluginConfigDTO dto = new ScriptPluginConfigDTO(body.getValues(), List.of(), 4L);
        when(service.saveConfig("packing-wave", body)).thenReturn(dto);

        assertEquals(dto, controller.saveConfig("packing-wave", body).getData());
        verify(service).saveConfig("packing-wave", body);
    }
}
```

- [ ] **Step 10: Run all config tests**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginConfigValidatorTest' \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginConfigServiceTest' \
  --tests 'org.openwes.script.plugin.core.controller.ScriptPluginConfigControllerTest' \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest'
```

Expected: PASS.

- [ ] **Step 11: Commit**

```bash
git add server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginConfigDTO.java \
        server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ScriptPluginConfigSaveDTO.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginConfigValidator.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginConfigService.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/controller/ScriptPluginConfigController.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginService.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginConfigValidatorTest.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginConfigServiceTest.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/controller/ScriptPluginConfigControllerTest.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java
git commit -m "feat(script-plugin): add validated config-value APIs with hot reload

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 8: Execution-Log Query and Aggregated Stats

**Files:**
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ExecutionStatsDTO.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/repository/ExecutionTypeStatsView.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginExecutionQueryService.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/controller/ScriptPluginExecutionController.java`
- Modify: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ExecutionLogDTO.java`
- Modify: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/repository/ScriptPluginExecutionLogRepository.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginExecutionQueryServiceTest.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/controller/ScriptPluginExecutionControllerTest.java`

**Interfaces:**
- Consumes: `ScriptPluginExecutionLogRepository`, `PluginWorkbenchAccessService`, `PageResultDTO`.
- Produces:
  - `ExecutionLogDTO.id` (`Long`, populated by queries only).
  - `ExecutionStatsDTO { long total; long failed; double failureRate; long avgDurationMs; long maxDurationMs; List<TypeStats> byType; }`.
  - `TypeStats { ExtensionType extensionType; long total; long failed; long avgDurationMs; long maxDurationMs; }`.
  - `ScriptPluginExecutionQueryService.page(String pluginKey, int page, int pageSize, Boolean success, ExtensionType extensionType)`.
  - `ScriptPluginExecutionQueryService.stats(String pluginKey, int hours)`.
  - `GET /{pluginKey}/executions` and `GET /{pluginKey}/executions/stats`.

- [ ] **Step 1: Write the failing query-service test**

```java
package org.openwes.script.plugin.core.domain.service;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExecutionStatsDTO;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginExecutionLog;
import org.openwes.script.plugin.core.domain.repository.ExecutionTypeStatsView;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginExecutionLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginExecutionQueryServiceTest {

    @Mock
    private ScriptPluginExecutionLogRepository repository;
    @Mock
    private PluginWorkbenchAccessService accessService;

    private ScriptPluginExecutionQueryService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginExecutionQueryService(repository, accessService);
    }

    private ScriptPluginExecutionLog log() {
        ScriptPluginExecutionLog row = new ScriptPluginExecutionLog();
        row.setId(7L);
        row.setPluginKey("packing-wave");
        row.setRevisionId(11L);
        row.setExtensionType(ExtensionType.CRON);
        row.setSuccess(true);
        row.setDurationMs(5);
        row.setCreatedAt(1000L);
        return row;
    }

    private ExecutionTypeStatsView view(ExtensionType type, long total, long failed,
                                        Double avg, Long max) {
        ExecutionTypeStatsView view = mock(ExecutionTypeStatsView.class);
        when(view.getExtensionType()).thenReturn(type);
        when(view.getTotal()).thenReturn(total);
        when(view.getFailed()).thenReturn(failed);
        when(view.getAvgDurationMs()).thenReturn(avg);
        when(view.getMaxDurationMs()).thenReturn(max);
        return view;
    }

    @Test
    void page_clampsPageAndSizeAndMapsTheId() {
        Page<ScriptPluginExecutionLog> page =
                new PageImpl<>(List.of(log()), PageRequest.of(0, 100), 1);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        PageResultDTO<ExecutionLogDTO> result = service.page("packing-wave", 0, 500, null, null);

        assertEquals(1, result.getPage());
        assertEquals(100, result.getPageSize());
        assertEquals(1, result.getTotal());
        assertEquals(7L, result.getItems().get(0).getId());
        verify(accessService).requireDeveloper();
        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(repository).findAll(any(Specification.class), captor.capture());
        assertEquals(0, captor.getValue().getPageNumber());
        assertEquals(100, captor.getValue().getPageSize());
    }

    @SuppressWarnings("unchecked")
    @Test
    void page_appliesSuccessAndExtensionTypeFilters() {
        Page<ScriptPluginExecutionLog> page =
                new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(repository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);

        service.page("packing-wave", 1, 20, true, ExtensionType.CRON);

        ArgumentCaptor<Specification<ScriptPluginExecutionLog>> captor =
                ArgumentCaptor.forClass(Specification.class);
        verify(repository).findAll(captor.capture(), any(Pageable.class));

        Root<ScriptPluginExecutionLog> root = mock(Root.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        Path<Object> pluginKeyPath = mock(Path.class);
        Path<Object> successPath = mock(Path.class);
        Path<Object> typePath = mock(Path.class);
        Predicate pluginKeyPredicate = mock(Predicate.class);
        Predicate successPredicate = mock(Predicate.class);
        Predicate typePredicate = mock(Predicate.class);
        when(root.get("pluginKey")).thenReturn((Path) pluginKeyPath);
        when(root.get("success")).thenReturn((Path) successPath);
        when(root.get("extensionType")).thenReturn((Path) typePath);
        when(cb.equal(pluginKeyPath, "packing-wave")).thenReturn(pluginKeyPredicate);
        when(cb.equal(successPath, true)).thenReturn(successPredicate);
        when(cb.equal(typePath, ExtensionType.CRON)).thenReturn(typePredicate);
        when(cb.and(any(Predicate[].class))).thenReturn(pluginKeyPredicate);

        captor.getValue().toPredicate(root, query, cb);

        verify(cb).equal(successPath, true);
        verify(cb).equal(typePath, ExtensionType.CRON);
    }

    @Test
    void stats_aggregatesTotalsFailureRateWeightedAverageAndMax() {
        when(repository.statsByType(eq("packing-wave"), anyLong())).thenReturn(List.of(
                view(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION, 3, 1, 10.0, 100L),
                view(ExtensionType.CRON, 1, 0, 40.0, 40L)));

        ExecutionStatsDTO stats = service.stats("packing-wave", 24);

        assertEquals(4, stats.getTotal());
        assertEquals(1, stats.getFailed());
        assertEquals(25.0, stats.getFailureRate());
        assertEquals(18, stats.getAvgDurationMs());
        assertEquals(100, stats.getMaxDurationMs());
        assertEquals(2, stats.getByType().size());
        assertEquals(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION,
                stats.getByType().get(0).getExtensionType());
        assertEquals(1, stats.getByType().get(0).getFailed());
        verify(accessService).requireDeveloper();
    }

    @Test
    void stats_emptyWindow_returnsZeros() {
        when(repository.statsByType(eq("packing-wave"), anyLong())).thenReturn(List.of());

        ExecutionStatsDTO stats = service.stats("packing-wave", 24);

        assertEquals(0, stats.getTotal());
        assertEquals(0, stats.getFailed());
        assertEquals(0.0, stats.getFailureRate());
        assertEquals(0, stats.getAvgDurationMs());
        assertEquals(0, stats.getMaxDurationMs());
        assertEquals(List.of(), stats.getByType());
    }

    @Test
    void normalizeHours_clampsToTheSupportedWindow() {
        assertEquals(1, ScriptPluginExecutionQueryService.normalizeHours(0));
        assertEquals(1, ScriptPluginExecutionQueryService.normalizeHours(-5));
        assertEquals(720, ScriptPluginExecutionQueryService.normalizeHours(10_000));
        assertEquals(24, ScriptPluginExecutionQueryService.normalizeHours(24));
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionQueryServiceTest'
```

Expected: compilation failure — the service, view, and stats DTO do not exist.

- [ ] **Step 3: Add the DTO id, the stats DTO, and the projection**

In `ExecutionLogDTO.java`, add the first field:

```java
    /** Populated by queries; ignored on ingest. Stable React key for the UI. */
    private Long id;
```

```java
package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionStatsDTO {

    private long total;
    private long failed;
    /** Percentage in [0, 100], rounded to two decimals. */
    private double failureRate;
    private long avgDurationMs;
    private long maxDurationMs;
    private List<TypeStats> byType;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeStats {
        private ExtensionType extensionType;
        private long total;
        private long failed;
        private long avgDurationMs;
        private long maxDurationMs;
    }
}
```

```java
package org.openwes.script.plugin.core.domain.repository;

import org.openwes.script.plugin.api.enums.ExtensionType;

/** JPQL projection for stats grouped by extension type. */
public interface ExecutionTypeStatsView {
    ExtensionType getExtensionType();

    long getTotal();

    long getFailed();

    Double getAvgDurationMs();

    Long getMaxDurationMs();
}
```

- [ ] **Step 4: Extend the execution-log repository**

Replace the repository declaration and add the stats query:

```java
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScriptPluginExecutionLogRepository
        extends JpaRepository<ScriptPluginExecutionLog, Long>,
        JpaSpecificationExecutor<ScriptPluginExecutionLog> {

    long countByPluginKey(String pluginKey);

    @Query("""
            select l.extensionType as extensionType,
                   count(l) as total,
                   sum(case when l.success = false then 1 else 0 end) as failed,
                   avg(l.durationMs) as avgDurationMs,
                   max(l.durationMs) as maxDurationMs
            from ScriptPluginExecutionLog l
            where l.pluginKey = :pluginKey and l.createdAt >= :since
            group by l.extensionType
            """)
    List<ExecutionTypeStatsView> statsByType(@Param("pluginKey") String pluginKey,
                                             @Param("since") long since);
```

Keep the existing `pruneToKeep` query unchanged.

- [ ] **Step 5: Create the query service**

```java
package org.openwes.script.plugin.core.domain.service;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExecutionStatsDTO;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.entity.ScriptPluginExecutionLog;
import org.openwes.script.plugin.core.domain.repository.ExecutionTypeStatsView;
import org.openwes.script.plugin.core.domain.repository.ScriptPluginExecutionLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScriptPluginExecutionQueryService {

    static final int MAX_PAGE_SIZE = 100;
    static final int MAX_HOURS = 720;

    private final ScriptPluginExecutionLogRepository repository;
    private final PluginWorkbenchAccessService accessService;

    public PageResultDTO<ExecutionLogDTO> page(String pluginKey, int page, int pageSize,
                                               Boolean success, ExtensionType extensionType) {
        accessService.requireDeveloper();
        int normalizedPage = Math.max(page, 1);
        int normalizedSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(normalizedPage - 1, normalizedSize,
                Sort.by(Sort.Direction.DESC, "id"));
        Specification<ScriptPluginExecutionLog> specification = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("pluginKey"), pluginKey));
            if (success != null) {
                predicates.add(cb.equal(root.get("success"), success));
            }
            if (extensionType != null) {
                predicates.add(cb.equal(root.get("extensionType"), extensionType));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        Page<ScriptPluginExecutionLog> rows = repository.findAll(specification, pageable);
        return PageResultDTO.of(rows.getContent().stream().map(this::toDto).toList(),
                rows.getTotalElements(), normalizedPage, normalizedSize);
    }

    public ExecutionStatsDTO stats(String pluginKey, int hours) {
        accessService.requireDeveloper();
        long since = System.currentTimeMillis() - normalizeHours(hours) * 3_600_000L;
        List<ExecutionTypeStatsView> rows = repository.statsByType(pluginKey, since);

        long total = rows.stream().mapToLong(ExecutionTypeStatsView::getTotal).sum();
        long failed = rows.stream().mapToLong(ExecutionTypeStatsView::getFailed).sum();
        double weightedAverage = rows.stream()
                .mapToDouble(row -> safe(row.getAvgDurationMs()) * row.getTotal()).sum();
        long avg = total == 0 ? 0 : Math.round(weightedAverage / total);
        long max = rows.stream()
                .map(row -> row.getMaxDurationMs() == null ? 0L : row.getMaxDurationMs())
                .mapToLong(Long::longValue).max().orElse(0);
        double failureRate = total == 0 ? 0 : Math.round(failed * 10_000.0 / total) / 100.0;
        List<ExecutionStatsDTO.TypeStats> byType = rows.stream()
                .map(row -> new ExecutionStatsDTO.TypeStats(row.getExtensionType(), row.getTotal(),
                        row.getFailed(), Math.round(safe(row.getAvgDurationMs())),
                        row.getMaxDurationMs() == null ? 0 : row.getMaxDurationMs()))
                .toList();
        return new ExecutionStatsDTO(total, failed, failureRate, avg, max, byType);
    }

    static int normalizeHours(int hours) {
        return Math.min(Math.max(hours, 1), MAX_HOURS);
    }

    private static double safe(Double value) {
        return value == null ? 0 : value;
    }

    private ExecutionLogDTO toDto(ScriptPluginExecutionLog row) {
        ExecutionLogDTO dto = new ExecutionLogDTO();
        dto.setId(row.getId());
        dto.setPluginKey(row.getPluginKey());
        dto.setRevisionId(row.getRevisionId());
        dto.setExtensionType(row.getExtensionType());
        dto.setSuccess(row.isSuccess());
        dto.setDurationMs(row.getDurationMs());
        dto.setErrorMessage(row.getErrorMessage());
        dto.setCreatedAt(row.getCreatedAt());
        return dto;
    }
}
```

- [ ] **Step 6: Run the query-service test**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionQueryServiceTest'
```

Expected: PASS, 5 tests.

- [ ] **Step 7: Create the controller and its test**

```java
package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExecutionStatsDTO;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionQueryService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Execution Api")
public class ScriptPluginExecutionController {

    private final ScriptPluginExecutionQueryService queryService;

    @GetMapping("/{pluginKey}/executions")
    public Response<PageResultDTO<ExecutionLogDTO>> executions(
            @PathVariable("pluginKey") String pluginKey,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "success", required = false) Boolean success,
            @RequestParam(value = "extensionType", required = false) ExtensionType extensionType) {
        return Response.success(queryService.page(pluginKey, page, pageSize, success, extensionType));
    }

    @GetMapping("/{pluginKey}/executions/stats")
    public Response<ExecutionStatsDTO> stats(
            @PathVariable("pluginKey") String pluginKey,
            @RequestParam(value = "hours", defaultValue = "24") int hours) {
        return Response.success(queryService.stats(pluginKey, hours));
    }
}
```

```java
package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.ExecutionLogDTO;
import org.openwes.script.plugin.api.dto.ExecutionStatsDTO;
import org.openwes.script.plugin.api.dto.PageResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionQueryService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginExecutionControllerTest {

    private final ScriptPluginExecutionQueryService queryService =
            mock(ScriptPluginExecutionQueryService.class);
    private final ScriptPluginExecutionController controller =
            new ScriptPluginExecutionController(queryService);

    @Test
    void executions_delegatesAllFilters() {
        PageResultDTO<ExecutionLogDTO> page = PageResultDTO.of(List.of(), 0, 1, 20);
        when(queryService.page("packing-wave", 1, 20, true,
                ExtensionType.OUTBOUND_WAVE_WAVE_ACTION)).thenReturn(page);

        assertEquals(page, controller.executions("packing-wave", 1, 20, true,
                ExtensionType.OUTBOUND_WAVE_WAVE_ACTION).getData());
    }

    @Test
    void stats_delegatesHours() {
        ExecutionStatsDTO stats = new ExecutionStatsDTO(0, 0, 0, 0, 0, List.of());
        when(queryService.stats("packing-wave", 48)).thenReturn(stats);

        assertEquals(stats, controller.stats("packing-wave", 48).getData());
        verify(queryService).stats("packing-wave", 48);
    }
}
```

- [ ] **Step 8: Run all execution tests**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionQueryServiceTest' \
  --tests 'org.openwes.script.plugin.core.controller.ScriptPluginExecutionControllerTest' \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginExecutionLogServiceTest'
```

Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ExecutionLogDTO.java \
        server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/ExecutionStatsDTO.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/repository/ExecutionTypeStatsView.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/repository/ScriptPluginExecutionLogRepository.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginExecutionQueryService.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/controller/ScriptPluginExecutionController.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginExecutionQueryServiceTest.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/controller/ScriptPluginExecutionControllerTest.java
git commit -m "feat(script-plugin): add execution-log query and stats APIs

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 9: Test-Run Contract and Argument Builder

**Files:**
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/ScriptPluginTestRunner.java`
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/TestRunRequestDTO.java`
- Create: `server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/TestRunResultDTO.java`
- Create: `server/modules-plugin/script-plugin-runtime/src/main/java/org/openwes/script/plugin/runtime/testrun/TestRunArgumentBuilder.java`
- Modify: `server/modules-plugin/script-plugin-runtime/build.gradle`
- Test: `server/modules-plugin/script-plugin-runtime/src/test/java/org/openwes/script/plugin/runtime/testrun/TestRunArgumentBuilderTest.java`

**Interfaces:**
- Consumes: `ExtensionType`, host DTOs `OutboundPlanOrderDTO`, `OutboundWaveDTO`, `RequestHandleContext`, `CustomApiParameter`.
- Produces:
  - `TestRunResultDTO ScriptPluginTestRunner.run(String pluginKey, ScriptPluginBundleDTO bundle, TestRunRequestDTO request)`.
  - `Object[] TestRunArgumentBuilder.build(ExtensionType type, Map<String,Object> arguments)` — arguments only, before `ctx` is appended.

- [ ] **Step 1: Add the runtime host dependencies**

Replace `script-plugin-runtime/build.gradle` with:

```gradle
dependencies {
    implementation project(path: ":modules-plugin:script-plugin-api", configuration: 'default')
    implementation project(path: ":modules-plugin:plugin-sdk", configuration: 'default')
    implementation project(path: ":modules-plugin:plugin-extension", configuration: 'default')
    implementation project(path: ":modules-utils:script-engine", configuration: 'default')
    implementation project(path: ":modules-utils:common-utils", configuration: 'default')
    implementation project(path: ":modules-utils:distribute-lock", configuration: 'default')
    implementation project(path: ":modules-wes:wes-api", configuration: 'default')
    implementation project(path: ":modules-api-platform:api-platform-api", configuration: 'default')

    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.apache.dubbo:dubbo-spring-boot-starter'
    implementation 'org.apache.commons:commons-lang3'
    implementation 'com.fasterxml.jackson.core:jackson-databind'
}
```

The explicit main dependencies are required because Gradle `implementation` is not transitive; the API module's host types are not visible from the runtime without them.

- [ ] **Step 2: Write the failing argument-builder test**

```java
package org.openwes.script.plugin.runtime.testrun;

import org.junit.jupiter.api.Test;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.business.station.CustomApiParameter;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.OutboundWaveDTO;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRunArgumentBuilderTest {

    @Test
    void httpEndpoint_passesBodyThrough() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.HTTP_ENDPOINT,
                Map.of("body", Map.of("orderNo", "SO-1")));

        assertEquals(1, arguments.length);
        assertEquals(Map.of("orderNo", "SO-1"), arguments[0]);
    }

    @Test
    void cron_hasNoArguments() {
        assertEquals(0, TestRunArgumentBuilder.build(ExtensionType.CRON, Map.of()).length);
    }

    @Test
    void lifecycleListener_buildsThreeTypedValues() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.LIFECYCLE_LISTENER,
                Map.of("entityId", 42, "entityOrderNo", "SO-1", "newStatus", "DONE"));

        assertEquals(3, arguments.length);
        assertEquals(42L, arguments[0]);
        assertEquals("SO-1", arguments[1]);
        assertEquals("DONE", arguments[2]);
    }

    @Test
    void outboundWaveAction_convertsOrdersToHostDtos() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION,
                Map.of("orders", List.of(Map.of("customerOrderNo", "SO-1", "warehouseCode", "WH1"))));

        List<?> orders = assertInstanceOf(List.class, arguments[0]);
        OutboundPlanOrderDTO order = assertInstanceOf(OutboundPlanOrderDTO.class, orders.get(0));
        assertEquals("SO-1", order.getCustomerOrderNo());
        assertEquals("WH1", order.getWarehouseCode());
    }

    @Test
    void outboundWaveSplitAction_convertsWaveToHostDto() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.OUTBOUND_WAVE_SPLIT_ACTION,
                Map.of("wave", Map.of("waveNo", "W-1", "warehouseCode", "WH1")));

        OutboundWaveDTO wave = assertInstanceOf(OutboundWaveDTO.class, arguments[0]);
        assertEquals("W-1", wave.getWaveNo());
        assertEquals("WH1", wave.getWarehouseCode());
    }

    @Test
    void requestArchetypes_convertTheRequestContext() {
        for (ExtensionType type : List.of(ExtensionType.REQUEST_VALIDATE,
                ExtensionType.REQUEST_SUPPLY, ExtensionType.REQUEST_AFTER_INVOKE,
                ExtensionType.CUSTOM_REQUEST_INVOKE)) {
            Object[] arguments = TestRunArgumentBuilder.build(type,
                    Map.of("context", Map.of("apiType", "outbound", "body", "{}")));

            RequestHandleContext context = assertInstanceOf(RequestHandleContext.class, arguments[0]);
            assertEquals("outbound", context.getApiType());
            assertEquals("{}", context.getBody());
        }
    }

    @Test
    void customApi_constructsTheParameterFromNamedFields() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.CUSTOM_API,
                Map.of("parameter", Map.of("workStationId", 9,
                        "requestParameter", Map.of("code", "ABC"))));

        CustomApiParameter parameter = assertInstanceOf(CustomApiParameter.class, arguments[0]);
        assertEquals(9L, parameter.getWorkStationId());
        assertInstanceOf(Map.class, parameter.getRequestParameter());
    }

    @Test
    void missingArguments_becomeNullArguments() {
        Object[] arguments = TestRunArgumentBuilder.build(ExtensionType.LIFECYCLE_LISTENER, Map.of());

        assertEquals(3, arguments.length);
        assertTrue(arguments[0] == null && arguments[1] == null && arguments[2] == null);
    }
}
```

- [ ] **Step 3: Run the test to verify it fails**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-runtime:test \
  --tests 'org.openwes.script.plugin.runtime.testrun.TestRunArgumentBuilderTest'
```

Expected: compilation failure — the builder and DTOs do not exist.

- [ ] **Step 4: Create the API contract and DTOs**

```java
package org.openwes.script.plugin.api;

import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;

/** Executes one declared script method for real on the hosting node. */
public interface ScriptPluginTestRunner {
    TestRunResultDTO run(String pluginKey, ScriptPluginBundleDTO bundle, TestRunRequestDTO request);
}
```

```java
package org.openwes.script.plugin.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.Map;

@Data
public class TestRunRequestDTO {
    @NotNull
    private ExtensionType extensionType;
    @NotBlank
    @Size(max = 128)
    private String script;
    @NotBlank
    @Size(max = 128)
    private String method;
    private Map<String, Object> arguments;
}
```

```java
package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestRunResultDTO {
    private boolean success;
    /** JSON text, truncated to 8 KB. */
    private String result;
    private boolean resultTruncated;
    private String error;
    private long durationMs;
}
```

- [ ] **Step 5: Create the argument builder**

```java
package org.openwes.script.plugin.runtime.testrun;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openwes.api.platform.api.dto.request.RequestHandleContext;
import org.openwes.plugin.extension.business.station.CustomApiParameter;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.wes.api.outbound.dto.OutboundPlanOrderDTO;
import org.openwes.wes.api.outbound.dto.OutboundWaveDTO;

import java.util.List;
import java.util.Map;

/**
 * Maps the user's named JSON test arguments to the host signature. The returned array does not
 * include the trailing ctx; the runner appends it.
 */
public final class TestRunArgumentBuilder {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TestRunArgumentBuilder() {
    }

    public static Object[] build(ExtensionType type, Map<String, Object> arguments) {
        Map<String, Object> args = arguments == null ? Map.of() : arguments;
        return switch (type) {
            case HTTP_ENDPOINT -> new Object[]{args.get("body")};
            case CRON -> new Object[0];
            case LIFECYCLE_LISTENER -> new Object[]{
                    convert(args.get("entityId"), Long.class),
                    convert(args.get("entityOrderNo"), String.class),
                    convert(args.get("newStatus"), String.class)};
            case OUTBOUND_WAVE_WAVE_ACTION -> new Object[]{
                    convertList(args.get("orders"), OutboundPlanOrderDTO.class)};
            case OUTBOUND_WAVE_SPLIT_ACTION -> new Object[]{
                    convert(args.get("wave"), OutboundWaveDTO.class)};
            case REQUEST_VALIDATE, REQUEST_SUPPLY, REQUEST_AFTER_INVOKE, CUSTOM_REQUEST_INVOKE ->
                    new Object[]{convert(args.get("context"), RequestHandleContext.class)};
            case CUSTOM_API -> new Object[]{customApiParameter(args.get("parameter"))};
        };
    }

    private static CustomApiParameter customApiParameter(Object value) {
        if (value == null) {
            return new CustomApiParameter(null, null);
        }
        Map<String, Object> map = OBJECT_MAPPER.convertValue(value, new TypeReference<>() {
        });
        Long workStationId = map.get("workStationId") == null
                ? null : OBJECT_MAPPER.convertValue(map.get("workStationId"), Long.class);
        return new CustomApiParameter(workStationId, map.get("requestParameter"));
    }

    private static <T> T convert(Object value, Class<T> type) {
        return value == null ? null : OBJECT_MAPPER.convertValue(value, type);
    }

    private static <T> List<T> convertList(Object value, Class<T> elementType) {
        if (value == null) {
            return List.of();
        }
        JavaType listType = OBJECT_MAPPER.getTypeFactory()
                .constructCollectionType(List.class, elementType);
        return OBJECT_MAPPER.convertValue(value, listType);
    }
}
```

- [ ] **Step 6: Run the builder test**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-runtime:test \
  --tests 'org.openwes.script.plugin.runtime.testrun.TestRunArgumentBuilderTest'
```

Expected: PASS, 8 tests.

- [ ] **Step 7: Commit**

```bash
git add server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/ScriptPluginTestRunner.java \
        server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/TestRunRequestDTO.java \
        server/modules-plugin/script-plugin-api/src/main/java/org/openwes/script/plugin/api/dto/TestRunResultDTO.java \
        server/modules-plugin/script-plugin-runtime/src/main/java/org/openwes/script/plugin/runtime/testrun/TestRunArgumentBuilder.java \
        server/modules-plugin/script-plugin-runtime/build.gradle \
        server/modules-plugin/script-plugin-runtime/src/test/java/org/openwes/script/plugin/runtime/testrun/TestRunArgumentBuilderTest.java
git commit -m "feat(script-plugin): add the test-run contract and typed argument builder

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 10: Test-Run Execution, Service, and Endpoint

**Files:**
- Create: `server/modules-plugin/script-plugin-runtime/src/main/java/org/openwes/script/plugin/runtime/testrun/ScriptPluginTestRunnerImpl.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginTestRunService.java`
- Create: `server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/controller/ScriptPluginTestRunController.java`
- Test: `server/modules-plugin/script-plugin-runtime/src/test/java/org/openwes/script/plugin/runtime/testrun/ScriptPluginTestRunnerImplTest.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginTestRunServiceTest.java`
- Test: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/controller/ScriptPluginTestRunControllerTest.java`

**Interfaces:**
- Consumes: `ScriptPluginTestRunner` from Task 9, `ScriptRevisionContextManager`, `GroovyScriptEngine`, `ScriptExecutionRuntime`, `ScriptHttpClient`, `ScriptPluginService.getBundle/detail`, `PluginWorkbenchAccessService`, `ScriptPluginOperationAuditService`.
- Produces:
  - `ScriptPluginTestRunService.run(String pluginKey, TestRunRequestDTO request)`.
  - `POST /plugin-platform/scriptPlugin/{pluginKey}/test-run`.

- [ ] **Step 1: Write the failing runtime runner test**

```java
package org.openwes.script.plugin.runtime.testrun;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptExecutionException;
import org.openwes.script.engine.ScriptExecutionRuntime;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.api.host.ScriptHttpClient;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScriptPluginTestRunnerImplTest {

    @Mock
    private ScriptRevisionContextManager contextManager;
    @Mock
    private GroovyScriptEngine scriptEngine;
    @Mock
    private ScriptExecutionRuntime executionRuntime;
    @Mock
    private ScriptHttpClient httpClient;

    private ScriptPluginTestRunnerImpl runner;
    private AnnotationConfigApplicationContext context;
    private Object scriptInstance;

    @BeforeEach
    void setUp() {
        runner = new ScriptPluginTestRunnerImpl(contextManager, scriptEngine, executionRuntime, httpClient);
        context = mock(AnnotationConfigApplicationContext.class);
        scriptInstance = new Object();
    }

    private ScriptRevisionContextManager.ValidationScope scope(Map<String, Object> instances) {
        return new ScriptRevisionContextManager.ValidationScope(context, instances);
    }

    private ScriptPluginBundleDTO bundle() {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setRevisionId(11L);
        bundle.setScripts(Map.of("Wave.groovy", "class Wave {}"));
        bundle.setConfig(Map.of());
        bundle.setConfigSchema(List.of());
        return bundle;
    }

    private TestRunRequestDTO request(String script, String method) {
        TestRunRequestDTO request = new TestRunRequestDTO();
        request.setExtensionType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        request.setScript(script);
        request.setMethod(method);
        request.setArguments(Map.of("orders", List.of()));
        return request;
    }

    @Test
    void successfulRun_serializesTheResultAndClosesTheScope() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));
        when(scriptEngine.respondsTo(scriptInstance, "wave")).thenReturn(true);
        when(scriptEngine.invoke(eq(scriptInstance), eq("wave"), any(Object[].class)))
                .thenReturn(Map.of("ok", true));
        when(executionRuntime.execute(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Wave.groovy", "wave"));

        assertTrue(result.isSuccess());
        assertTrue(result.getResult().contains("\"ok\""));
        assertFalse(result.isResultTruncated());
        assertTrue(result.getDurationMs() >= 0);
        verify(context).close();
    }

    @Test
    void scriptException_returnsFailureAndStillClosesTheScope() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));
        when(scriptEngine.respondsTo(scriptInstance, "wave")).thenReturn(true);
        when(scriptEngine.invoke(eq(scriptInstance), eq("wave"), any(Object[].class)))
                .thenThrow(new ScriptExecutionException("boom"));
        when(executionRuntime.execute(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Wave.groovy", "wave"));

        assertFalse(result.isSuccess());
        assertEquals("boom", result.getError());
        verify(context).close();
    }

    @Test
    void timeout_isReportedAsFailureAndStillClosesTheScope() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));
        when(scriptEngine.respondsTo(scriptInstance, "wave")).thenReturn(true);
        when(executionRuntime.execute(any())).thenThrow(
                new ScriptExecutionException("script timed out after 50ms"));

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Wave.groovy", "wave"));

        assertFalse(result.isSuccess());
        assertEquals("script timed out after 50ms", result.getError());
        verify(context).close();
    }

    @Test
    void unknownScript_isReportedWithoutInvoking() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Missing.groovy", "wave"));

        assertFalse(result.isSuccess());
        assertTrue(result.getError().contains("not part of the current revision"));
        verify(context).close();
    }

    @Test
    void largeResult_isTruncatedToEightKilobytes() {
        when(contextManager.openValidationScope("packing-wave",
                Map.of("Wave.groovy", "class Wave {}")))
                .thenReturn(scope(Map.of("Wave.groovy", scriptInstance)));
        when(scriptEngine.respondsTo(scriptInstance, "wave")).thenReturn(true);
        when(scriptEngine.invoke(eq(scriptInstance), eq("wave"), any(Object[].class)))
                .thenReturn("x".repeat(9000));
        when(executionRuntime.execute(any())).thenAnswer(invocation -> {
            Supplier<?> supplier = invocation.getArgument(0);
            return supplier.get();
        });

        TestRunResultDTO result = runner.run("packing-wave", bundle(),
                request("Wave.groovy", "wave"));

        assertTrue(result.isSuccess());
        assertTrue(result.isResultTruncated());
        assertEquals(8 * 1024, result.getResult().length());
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-runtime:test \
  --tests 'org.openwes.script.plugin.runtime.testrun.ScriptPluginTestRunnerImplTest'
```

Expected: compilation failure — the runner implementation does not exist.

- [ ] **Step 3: Create the runtime runner**

```java
package org.openwes.script.plugin.runtime.testrun;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.engine.GroovyScriptEngine;
import org.openwes.script.engine.ScriptExecutionRuntime;
import org.openwes.script.plugin.api.ScriptPluginTestRunner;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.api.host.ScriptHttpClient;
import org.openwes.script.plugin.runtime.host.DefaultScriptContext;
import org.openwes.script.plugin.runtime.materialize.ScriptRevisionContextManager;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScriptPluginTestRunnerImpl implements ScriptPluginTestRunner {

    static final int MAX_RESULT_LENGTH = 8 * 1024;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ScriptRevisionContextManager revisionContextManager;
    private final GroovyScriptEngine scriptEngine;
    private final ScriptExecutionRuntime executionRuntime;
    private final ScriptHttpClient scriptHttpClient;

    @Override
    public TestRunResultDTO run(String pluginKey, ScriptPluginBundleDTO bundle, TestRunRequestDTO request) {
        TestRunResultDTO result = new TestRunResultDTO();
        long startedAt = System.nanoTime();
        try (ScriptRevisionContextManager.ValidationScope scope = revisionContextManager
                .openValidationScope(pluginKey, bundle.getScripts())) {
            Object instance = scope.instances().get(request.getScript());
            if (instance == null) {
                throw new CommonException("script '" + request.getScript()
                        + "' is not part of the current revision");
            }
            if (!scriptEngine.respondsTo(instance, request.getMethod())) {
                throw new CommonException("script '" + request.getScript()
                        + "' does not declare method '" + request.getMethod() + "'");
            }
            DefaultScriptContext context = new DefaultScriptContext(pluginKey, scope.context(),
                    bundle.getConfig() == null ? Map.of() : bundle.getConfig(),
                    bundle.getConfigSchema() == null ? List.of() : bundle.getConfigSchema(),
                    scriptHttpClient);
            Object[] arguments = TestRunArgumentBuilder.build(request.getExtensionType(),
                    request.getArguments());
            Object[] invocationArguments = Arrays.copyOf(arguments, arguments.length + 1);
            invocationArguments[arguments.length] = context;
            Object value = executionRuntime.execute(() ->
                    scriptEngine.invoke(instance, request.getMethod(), invocationArguments));
            String json = serialize(value);
            result.setSuccess(true);
            result.setResultTruncated(json != null && json.length() > MAX_RESULT_LENGTH);
            result.setResult(truncate(json));
        } catch (RuntimeException e) {
            result.setSuccess(false);
            result.setError(e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage());
        } finally {
            result.setDurationMs((System.nanoTime() - startedAt) / 1_000_000);
        }
        return result;
    }

    private static String serialize(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            return value.toString();
        }
    }

    private static String truncate(String value) {
        if (value == null || value.length() <= MAX_RESULT_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_RESULT_LENGTH);
    }
}
```

- [ ] **Step 4: Run the runtime runner test**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-runtime:test \
  --tests 'org.openwes.script.plugin.runtime.testrun.ScriptPluginTestRunnerImplTest'
```

Expected: PASS, 5 tests.

- [ ] **Step 5: Write the failing core test-run service test**

```java
package org.openwes.script.plugin.core.domain.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.ScriptPluginTestRunner;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.TEST_RUN;

@ExtendWith(MockitoExtension.class)
class ScriptPluginTestRunServiceTest {

    @Mock
    private ScriptPluginService pluginService;
    @Mock
    private PluginWorkbenchAccessService accessService;
    @Mock
    private ScriptPluginOperationAuditService auditService;
    @Mock
    private ScriptPluginTestRunner testRunner;

    private ScriptPluginTestRunService service;

    @BeforeEach
    void setUp() {
        service = new ScriptPluginTestRunService(pluginService, accessService, auditService, testRunner);
    }

    private ScriptPluginDTO plugin(String applySystem) {
        ScriptPluginDTO dto = new ScriptPluginDTO();
        dto.setPluginKey("packing-wave");
        dto.setApplySystem(applySystem);
        dto.setCurrentRevisionId(11L);
        return dto;
    }

    private ScriptPluginBundleDTO bundle(String script, String method) {
        ExtensionDeclarationDTO declaration = new ExtensionDeclarationDTO();
        declaration.setType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        declaration.setScript(script);
        declaration.setMethod(method);
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("packing-wave");
        bundle.setRevisionId(11L);
        bundle.setDeclarations(List.of(declaration));
        return bundle;
    }

    private TestRunRequestDTO request() {
        TestRunRequestDTO request = new TestRunRequestDTO();
        request.setExtensionType(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION);
        request.setScript("Wave.groovy");
        request.setMethod("wave");
        return request;
    }

    @Test
    void run_executesDeclaredExtensionAndAuditsTheOutcome() {
        when(accessService.requireDeveloper("packing-wave", TEST_RUN)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(plugin("wes"));
        when(pluginService.getBundle("packing-wave")).thenReturn(bundle("Wave.groovy", "wave"));
        TestRunResultDTO result = new TestRunResultDTO(true, "{}", false, null, 5);
        when(testRunner.run("packing-wave", bundle("Wave.groovy", "wave"), request()))
                .thenReturn(result);

        assertSame(result, service.run("packing-wave", request()));

        verify(auditService).recordAs("packing-wave", TEST_RUN, 11L, true,
                "test-run OUTBOUND_WAVE_WAVE_ACTION Wave.groovy#wave durationMs=5", "alice");
    }

    @Test
    void run_rejectsStationPluginsBeforeLoadingTheBundle() {
        when(accessService.requireDeveloper("packing-wave", TEST_RUN)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(plugin("station"));

        assertThrows(CommonException.class, () -> service.run("packing-wave", request()));

        verify(pluginService, never()).getBundle(any());
        verifyNoInteractions(testRunner);
    }

    @Test
    void run_rejectsUndeclaredExtensionTuples() {
        when(accessService.requireDeveloper("packing-wave", TEST_RUN)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(plugin("wes"));
        when(pluginService.getBundle("packing-wave")).thenReturn(bundle("Other.groovy", "wave"));

        assertThrows(CommonException.class, () -> service.run("packing-wave", request()));

        verifyNoInteractions(testRunner);
    }

    @Test
    void run_auditsRunnerFailuresAsUnsuccessful() {
        when(accessService.requireDeveloper("packing-wave", TEST_RUN)).thenReturn("alice");
        when(pluginService.detail("packing-wave")).thenReturn(plugin("wes"));
        when(pluginService.getBundle("packing-wave")).thenReturn(bundle("Wave.groovy", "wave"));
        when(testRunner.run("packing-wave", bundle("Wave.groovy", "wave"), request()))
                .thenReturn(new TestRunResultDTO(false, null, false, "boom", 5));

        service.run("packing-wave", request());

        verify(auditService).recordAs("packing-wave", TEST_RUN, 11L, false,
                "test-run OUTBOUND_WAVE_WAVE_ACTION Wave.groovy#wave durationMs=5", "alice");
    }
}
```

- [ ] **Step 6: Create the core test-run service**

```java
package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.ScriptPluginTestRunner;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginDTO;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.TEST_RUN;

@Service
@RequiredArgsConstructor
public class ScriptPluginTestRunService {

    private final ScriptPluginService pluginService;
    private final PluginWorkbenchAccessService accessService;
    private final ScriptPluginOperationAuditService auditService;
    private final ScriptPluginTestRunner testRunner;

    public TestRunResultDTO run(String pluginKey, TestRunRequestDTO request) {
        String actor = accessService.requireDeveloper(pluginKey, TEST_RUN);
        ScriptPluginDTO plugin = pluginService.detail(pluginKey);
        if (!ScriptPluginApiConstants.APP_WES.equals(plugin.getApplySystem())) {
            throw new CommonException("test-run is only available for wes plugins on this node");
        }
        ScriptPluginBundleDTO bundle = pluginService.getBundle(pluginKey);
        if (bundle == null) {
            throw new CommonException("plugin has no revision to test-run");
        }
        boolean declared = bundle.getDeclarations() != null && bundle.getDeclarations().stream()
                .anyMatch(declaration -> declaration.getType() == request.getExtensionType()
                        && Objects.equals(request.getScript(), declaration.getScript())
                        && Objects.equals(request.getMethod(), declaration.getMethod()));
        if (!declared) {
            throw new CommonException("the current revision does not declare this extension");
        }
        TestRunResultDTO result;
        try {
            result = testRunner.run(pluginKey, bundle, request);
        } catch (RuntimeException e) {
            auditService.recordAs(pluginKey, TEST_RUN, bundle.getRevisionId(), false,
                    "test-run " + request.getExtensionType() + " " + request.getScript()
                            + "#" + request.getMethod() + " failed: " + e.getClass().getSimpleName(),
                    actor);
            throw e;
        }
        auditService.recordAs(pluginKey, TEST_RUN, bundle.getRevisionId(), result.isSuccess(),
                "test-run " + request.getExtensionType() + " " + request.getScript()
                        + "#" + request.getMethod() + " durationMs=" + result.getDurationMs(), actor);
        return result;
    }
}
```

- [ ] **Step 7: Create the controller and its test**

```java
package org.openwes.script.plugin.core.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.http.Response;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginTestRunService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("plugin-platform/scriptPlugin")
@Tag(name = "Script Plugin Test-Run Api")
public class ScriptPluginTestRunController {

    private final ScriptPluginTestRunService testRunService;

    @PostMapping("/{pluginKey}/test-run")
    public Response<TestRunResultDTO> run(@PathVariable("pluginKey") String pluginKey,
                                          @Valid @RequestBody TestRunRequestDTO request) {
        return Response.success(testRunService.run(pluginKey, request));
    }
}
```

```java
package org.openwes.script.plugin.core.controller;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.dto.TestRunRequestDTO;
import org.openwes.script.plugin.api.dto.TestRunResultDTO;
import org.openwes.script.plugin.core.domain.service.ScriptPluginTestRunService;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ScriptPluginTestRunControllerTest {

    private final ScriptPluginTestRunService service = mock(ScriptPluginTestRunService.class);
    private final ScriptPluginTestRunController controller = new ScriptPluginTestRunController(service);

    @Test
    void run_delegatesToTheService() {
        TestRunRequestDTO request = new TestRunRequestDTO();
        TestRunResultDTO result = new TestRunResultDTO(true, "{}", false, null, 3);
        when(service.run("packing-wave", request)).thenReturn(result);

        assertSame(result, controller.run("packing-wave", request).getData());
        verify(service).run("packing-wave", request);
    }
}
```

- [ ] **Step 8: Run all test-run tests**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :modules-plugin:script-plugin-runtime:test \
  --tests 'org.openwes.script.plugin.runtime.testrun.*' \
  :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginTestRunServiceTest' \
  --tests 'org.openwes.script.plugin.core.controller.ScriptPluginTestRunControllerTest'
```

Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add server/modules-plugin/script-plugin-runtime/src/main/java/org/openwes/script/plugin/runtime/testrun/ScriptPluginTestRunnerImpl.java \
        server/modules-plugin/script-plugin-runtime/src/test/java/org/openwes/script/plugin/runtime/testrun/ScriptPluginTestRunnerImplTest.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/domain/service/ScriptPluginTestRunService.java \
        server/modules-plugin/script-plugin-core/src/main/java/org/openwes/script/plugin/core/controller/ScriptPluginTestRunController.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginTestRunServiceTest.java \
        server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/controller/ScriptPluginTestRunControllerTest.java
git commit -m "feat(script-plugin): execute declared extensions as a real test-run

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 11: Seed Roles, Menus, Permissions, and Grants

**Files:**
- Modify: `server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261023.xml` (created in Task 2)
- Test: `server/server/wes-server/src/test/java/org/openwes/config/ScriptPluginSeedChangelogTest.java`

**Interfaces:**
- Consumes: the existing `u_role`, `u_menu`, and `u_role_menu` tables (see `init_menu.sql` and `init_create_table.sql`).
- Produces: role codes `pluginDeveloper` and `pluginAdmin`; menu system `plugin-platform`; menu paths `/plugin-platform/plugins` and `/plugin-platform/audit`; permissions `get:/plugin-platform/` and `post:/plugin-platform/`.

The menu `title` values are i18n keys. Plan 3B adds these keys to `en-us.json` and `zh-cn.json`; until then a non-super user sees the raw keys in the sidebar, which is acceptable for the backend-first rollout.

Both roles also receive the root system row. Without it, `MenuServiceImpl.buildMenuTree(menus, 0L)` drops the child menus as orphans and the app never appears for the role.

- [ ] **Step 1: Write the failing seed guard test**

```java
package org.openwes.config;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/** Guards the Phase 3 workbench seed so a role or menu key cannot silently disappear. */
class ScriptPluginSeedChangelogTest {

    private static final String CHANGELOG =
            "server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261023.xml";
    private static final String MASTER =
            "server/server/wes-server/src/main/resources/db/changelog/db.changelog-master.xml";

    private static String read(String relativePath) throws Exception {
        Path directory = Path.of("").toAbsolutePath();
        while (directory != null) {
            Path candidate = directory.resolve(relativePath);
            if (Files.exists(candidate)) {
                return Files.readString(candidate, StandardCharsets.UTF_8);
            }
            directory = directory.getParent();
        }
        throw new IllegalStateException(relativePath + " not found above "
                + Path.of("").toAbsolutePath());
    }

    @Test
    void masterRegistersTheWorkbenchChangelog() throws Exception {
        assertTrue(read(MASTER).contains("db.changelog-20261023.xml"));
    }

    @Test
    void seedsBothWorkbenchRoles() throws Exception {
        String xml = read(CHANGELOG);

        assertTrue(xml.contains("'pluginDeveloper'"));
        assertTrue(xml.contains("'pluginAdmin'"));
        assertTrue(xml.contains("1900000100"));
        assertTrue(xml.contains("1900000101"));
    }

    @Test
    void seedsTheAppMenuAndGatewayPermissions() throws Exception {
        String xml = read(CHANGELOG);

        assertTrue(xml.contains("'plugin-platform'"));
        assertTrue(xml.contains("'pluginPlatform.system.title'"));
        assertTrue(xml.contains("'/plugin-platform/plugins'"));
        assertTrue(xml.contains("'/plugin-platform/audit'"));
        assertTrue(xml.contains("'get:/plugin-platform/'"));
        assertTrue(xml.contains("'post:/plugin-platform/'"));
    }

    @Test
    void grantsTheMenuSetToBothRolesAndAuditToAdmin() throws Exception {
        String xml = read(CHANGELOG);

        for (String id : List.of("1900000301", "1900000302", "1900000303", "1900000304",
                "1900000311", "1900000312", "1900000313", "1900000314", "1900000315")) {
            assertTrue(xml.contains(id), "missing role-menu grant " + id);
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :server:wes-server:test \
  --tests 'org.openwes.config.ScriptPluginSeedChangelogTest'
```

Expected: FAIL — the seed SQL is absent.

- [ ] **Step 3: Add the roles changeset**

Append to `db.changelog-20261023.xml`, before `</databaseChangeLog>`:

```xml
    <changeSet id="seed_script_plugin_workbench_roles" author="dev">
        <preConditions onFail="MARK_RAN">
            <sqlCheck expectedResult="0">
                SELECT COUNT(*) FROM u_role WHERE code IN ('pluginDeveloper', 'pluginAdmin')
            </sqlCheck>
        </preConditions>
        <sql>
            INSERT INTO u_role
                (id, create_time, create_user, update_time, update_user, code, description, name,
                 status, warehouse_codes)
            SELECT 1900000100, UNIX_TIMESTAMP() * 1000, 'system',
                   UNIX_TIMESTAMP() * 1000, 'system', 'pluginDeveloper',
                   'Can create, edit, validate and test-run script plugins',
                   'Plugin Developer', 1, '[]'
            FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role WHERE code = 'pluginDeveloper');

            INSERT INTO u_role
                (id, create_time, create_user, update_time, update_user, code, description, name,
                 status, warehouse_codes)
            SELECT 1900000101, UNIX_TIMESTAMP() * 1000, 'system',
                   UNIX_TIMESTAMP() * 1000, 'system', 'pluginAdmin',
                   'Can enable, disable, archive, roll back and configure script plugins',
                   'Plugin Administrator', 1, '[]'
            FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role WHERE code = 'pluginAdmin');
        </sql>
        <rollback>
            <sql>DELETE FROM u_role WHERE id IN (1900000100, 1900000101);</sql>
        </rollback>
    </changeSet>
```

- [ ] **Step 4: Add the menu and permission changeset**

Append after the roles changeset:

```xml
    <changeSet id="seed_script_plugin_workbench_menus" author="dev">
        <preConditions onFail="MARK_RAN">
            <sqlCheck expectedResult="0">
                SELECT COUNT(*) FROM u_menu WHERE id BETWEEN 1900000000 AND 1900000004
            </sqlCheck>
        </preConditions>
        <sql>
            INSERT INTO u_menu
                (id, create_user, system_code, parent_id, type, title, description, permissions,
                 order_num, icon, path, enable, create_time, update_time, update_user, iframe_show)
            SELECT 1900000000, 'system', 'plugin-platform', 0, 1, 'pluginPlatform.system.title',
                   NULL, 'plugin-platform', 10, 'pluginapp', '', 1,
                   UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 0
            FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_menu WHERE id = 1900000000);

            INSERT INTO u_menu
                (id, create_user, system_code, parent_id, type, title, description, permissions,
                 order_num, icon, path, enable, create_time, update_time, update_user, iframe_show)
            SELECT 1900000001, 'system', 'plugin-platform', 1900000000, 2,
                   'pluginPlatform.menu.plugins', NULL, '/plugin-platform/plugins', 1, NULL,
                   '/plugin-platform/plugins', 1,
                   UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 0
            FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_menu WHERE id = 1900000001);

            INSERT INTO u_menu
                (id, create_user, system_code, parent_id, type, title, description, permissions,
                 order_num, icon, path, enable, create_time, update_time, update_user, iframe_show)
            SELECT 1900000002, 'system', 'plugin-platform', 1900000000, 2,
                   'pluginPlatform.menu.audit', NULL, '/plugin-platform/audit', 2, NULL,
                   '/plugin-platform/audit', 1,
                   UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 0
            FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_menu WHERE id = 1900000002);

            INSERT INTO u_menu
                (id, create_user, system_code, parent_id, type, title, description, permissions,
                 order_num, icon, path, enable, create_time, update_time, update_user, iframe_show)
            SELECT 1900000003, 'system', 'plugin-platform', 1900000000, 3,
                   'pluginPlatform.permission.get', NULL, 'get:/plugin-platform/', 3, NULL,
                   NULL, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 0
            FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_menu WHERE id = 1900000003);

            INSERT INTO u_menu
                (id, create_user, system_code, parent_id, type, title, description, permissions,
                 order_num, icon, path, enable, create_time, update_time, update_user, iframe_show)
            SELECT 1900000004, 'system', 'plugin-platform', 1900000000, 3,
                   'pluginPlatform.permission.post', NULL, 'post:/plugin-platform/', 4, NULL,
                   NULL, 1, UNIX_TIMESTAMP() * 1000, UNIX_TIMESTAMP() * 1000, 'system', 0
            FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_menu WHERE id = 1900000004);
        </sql>
        <rollback>
            <sql>DELETE FROM u_menu WHERE id BETWEEN 1900000000 AND 1900000004;</sql>
        </rollback>
    </changeSet>
```

- [ ] **Step 5: Add the role-menu grants changeset**

Append after the menus changeset:

```xml
    <changeSet id="seed_script_plugin_workbench_grants" author="dev">
        <preConditions onFail="MARK_RAN">
            <sqlCheck expectedResult="0">
                SELECT COUNT(*) FROM u_role_menu WHERE id BETWEEN 1900000301 AND 1900000315
            </sqlCheck>
        </preConditions>
        <sql>
            INSERT INTO u_role_menu (id, menu_id, role_id)
            SELECT 1900000301, 1900000000, 1900000100 FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role_menu WHERE id = 1900000301);
            INSERT INTO u_role_menu (id, menu_id, role_id)
            SELECT 1900000302, 1900000001, 1900000100 FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role_menu WHERE id = 1900000302);
            INSERT INTO u_role_menu (id, menu_id, role_id)
            SELECT 1900000303, 1900000003, 1900000100 FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role_menu WHERE id = 1900000303);
            INSERT INTO u_role_menu (id, menu_id, role_id)
            SELECT 1900000304, 1900000004, 1900000100 FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role_menu WHERE id = 1900000304);

            INSERT INTO u_role_menu (id, menu_id, role_id)
            SELECT 1900000311, 1900000000, 1900000101 FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role_menu WHERE id = 1900000311);
            INSERT INTO u_role_menu (id, menu_id, role_id)
            SELECT 1900000312, 1900000001, 1900000101 FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role_menu WHERE id = 1900000312);
            INSERT INTO u_role_menu (id, menu_id, role_id)
            SELECT 1900000313, 1900000002, 1900000101 FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role_menu WHERE id = 1900000313);
            INSERT INTO u_role_menu (id, menu_id, role_id)
            SELECT 1900000314, 1900000003, 1900000101 FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role_menu WHERE id = 1900000314);
            INSERT INTO u_role_menu (id, menu_id, role_id)
            SELECT 1900000315, 1900000004, 1900000101 FROM DUAL
            WHERE NOT EXISTS (SELECT 1 FROM u_role_menu WHERE id = 1900000315);
        </sql>
        <rollback>
            <sql>DELETE FROM u_role_menu WHERE id BETWEEN 1900000301 AND 1900000315;</sql>
        </rollback>
    </changeSet>
```

- [ ] **Step 6: Validate the XML and run the guard test**

```bash
powershell -NoProfile -Command "[xml](Get-Content -Raw 'D:/open-wes/server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261023.xml') | Out-Null; Write-Output 'XML OK'"
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server :server:wes-server:test \
  --tests 'org.openwes.config.ScriptPluginSeedChangelogTest'
```

Expected: `XML OK`, then PASS, 4 tests. The test reads the file from the filesystem; it does not start Spring or a database, so it cannot verify SQL execution. Note in the commit body that Liquibase execution against MySQL remains unverified until a live deployment.

- [ ] **Step 7: Commit**

```bash
git add server/server/wes-server/src/main/resources/db/changelog/db.changelog-20261023.xml \
        server/server/wes-server/src/test/java/org/openwes/config/ScriptPluginSeedChangelogTest.java
git commit -m "feat(script-plugin): seed workbench roles, menus and permissions

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

### Task 12: Standards Update and Full Build

**Files:**
- Modify: `docs/standards/backend.md`

**Interfaces:**
- Consumes: the authorization and audit behavior implemented in Tasks 3–10.
- Produces: no code; the standards now describe Phase 3 instead of the pre-Phase-3 limitation.

- [ ] **Step 1: Replace the stale limitation in item 10(d)**

Replace this exact clause in `docs/standards/backend.md`:

```text
(d) the management API relies on the gateway JWT and network isolation until Phase 3 adds roles and audit logging;
```

with:

```text
(d) the workbench authorizes with server-side role checks against configurable role codes (`plugin.security.developer-role-codes`, default `pluginDeveloper`; `plugin.security.admin-role-codes`, default `pluginAdmin`; the existing super role is implicit). The configured codes are read at startup, so changing a code requires a wes-server restart. Every state-changing workbench action is appended to `p_script_plugin_operation_audit`; the gateway's `get:/plugin-platform/` and `post:/plugin-platform/` permissions only gate broad access, not individual actions;
```

- [ ] **Step 2: Extend the AI pipeline item**

Append this sentence to the end of item 11 (after `must disable response buffering for it.`):

```text
Every AI endpoint (`stream`, `generations`, `refreshCorpus`) requires the developer role through `PluginWorkbenchAccessService`, and each accepted AI revision is audited as `SAVE_REVISION` with actor, revision id and origin.
```

- [ ] **Step 3: Add the workbench governance item**

Insert this item after item 11 and before the `## Unit Test Rules` heading:

```markdown
12. **Workbench Authorization and Audit** — `PluginWorkbenchAccessService` is the single authorization point for the workbench; controllers and domain services never read roles directly. Administrator implies developer, and the configured super role is implicitly both. Every management action (`createDraft`, `saveRevision`, `validate`, `enable`, `disable`, `archive`, `rollback`, `updateMetadata`, config save, test-run) goes through a workbench service (`ScriptPluginWorkbenchService`, `ScriptPluginConfigService`, or `ScriptPluginTestRunService`) and is appended to `p_script_plugin_operation_audit` with actor, action, plugin key, revision id, success and a short detail. Audit failures are logged and never change the business result. Denials are audited only for state-changing actions; read-only denials are logged at warn level. Audit details must never contain script bodies, prompts, config values, request bodies, test arguments or test results. `disableBySystem` is the only path that skips the role check, and only for internal callers such as the cron circuit breaker. Test-run executes the real script on the wes node inside a throwaway revision context, writes no execution log and is rejected for non-wes plugins; config saves use the `lockVersion` optimistic lock, and validation never echoes values.
```

- [ ] **Step 4: Run the full backend build**

```bash
export JAVA_HOME="C:/Users/niekang/.jdks/ms-17.0.20.1"
./server/gradlew -p server build
```

Expected: `BUILD SUCCESSFUL`, and every module's tests pass. If a test fails, stop and report the failing test; do not commit a claim of success. Do not add `-x test` or `ignoreFailures`.

- [ ] **Step 5: Commit**

```bash
git add docs/standards/backend.md
git commit -m "docs(script-plugin): document workbench authorization and audit

Co-Authored-By: Claude Code <noreply@anthropic.com>"
```

---

## Plan Complete

After Task 12, all Phase 3A backend deliverables are implemented and verified by unit tests and a full Gradle build. Manual end-to-end acceptance (requirement → enable, rollback, denial audit) still requires a running MySQL, Redis, Nacos and the wes/station servers; it is not claimed by this plan. Plan 3B (client) covers the AMIS lists, the React workbench, Monaco/diff/revisions, test-run UI, AI chat drawer, and the i18n keys for the seeded menus.
