# DDD Refactoring Guide for WES Modules

> **Purpose**: Step-by-step guide to refactor any WES module from `@Data` entities + domain aggregates to `@Getter @Builder` entities + UseCase pattern. Based on the outbound module refactoring (branch `refactor_outbound_ddd`).

## Prerequisites

- Read `docs/standards/backend.md` — defines the target patterns
- The outbound module (`wes-outbound`) is the reference implementation

## Scope per Module

| Module | Entity Files with @Data | Aggregate Files | Priority |
|--------|------------------------|-----------------|----------|
| wes-inbound | 6 | 3 | High |
| wes-stock | 6 | 3 | High |
| wes-basic | 14 | 6 | Medium |
| wes-stocktake | 5 | 1 | Medium |
| wes-task | 1 | 1 | Low |
| wes-config | 4 | 0 | Low |
| wes-ems-proxy | 3 | 0 | Low |
| wes-printer | 3 | 0 | Low |

## Phase 0: Impact Analysis (read-only)

Before touching code, catalogue every call site. This phase is critical — skipping it causes compile failures mid-refactoring.

### Step 1: Verify MapStruct version

MapStruct 1.5.3.Final+ natively supports `@Builder`. Confirmed in `server/build.gradle`.

### Step 2: Catalogue setter usage on entities

```bash
cd D:/open-wes/server
grep -rn "\.set[A-Z]" modules-wes/<MODULE>/src/main/java/ --include="*.java" \
  | grep -v "\.setModified\|/po/\|/transfer/\|PO\.\|DTO\.\|/context/" | head -80
```

Classify each setter call:
- **Internal mutation** (entity method sets its own field) — keep as-is, it's a domain method
- **Same-entity initialization** (e.g., `detail.setOrderId(order.id)`) — convert to `initialize()` or `@Setter(AccessLevel.PACKAGE)`
- **Builder-style construction** (chain setters to create entity) — convert to `.builder()...build()`
- **External DTO setter** (on DTO/context objects, not entities) — leave alone, these are not domain entities

### Step 3: Catalogue aggregate callers

```bash
grep -rn "Aggregate" modules-wes/<MODULE>/src/main/java/ --include="*.java" \
  | grep -v "AggregatorRoot\|test\|import"
```

For each aggregate class, document:
1. Who calls it (subscriber? ApiImpl? scheduler?)
2. What it does (orchestrates which entities? calls which external APIs?)
3. Whether it has `@Transactional`

### Step 4: Check for external subscribers to domain events

```bash
grep -rn "<EventClassName>" --include="*.java" | grep -v "import\|class\|package"
```

If monitoring or other modules subscribe to events from this module, those events must be preserved.

## Phase 1: Entity @Builder Refactoring

### For each entity file:

**Replace annotations:**
```java
// Before:
@Data
// or
@Data
@Accessors(chain = true)

// After:
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
```

If the entity extends `AggregatorRoot`, also keep `@EqualsAndHashCode(callSuper = true)`.

**Add required imports:**
```java
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
```

Remove: `import lombok.Data;` and `import lombok.experimental.Accessors;`

### Handle special cases:

| Pattern | Solution |
|---------|----------|
| `@Builder.Default` needed | Add to fields with initializers: `@Builder.Default private Integer qty = 0;` |
| `setModified()` from `ModificationAware` | Keep the `@Override` setter method explicitly |
| Package-internal setter needed | Use `@Setter(AccessLevel.PACKAGE)` on the specific field |
| `new Entity()` + chained setters | Convert to `Entity.builder()...build()` |
| `BeanUtils.copyProperties()` | Replace with explicit builder construction |
| Static factory method (`create()`, `copyAndNew()`) | Keep — they access private fields within the class |
| Repository sets FK after save | Add `initialize(Long parentId)` domain method |

### Update ObjectUtils for tests

`ObjectUtils.getRandomObject()` needs `constructor.setAccessible(true)` to handle protected constructors. This was already fixed in the outbound refactoring — verify it's in your codebase:

```java
// In ObjectUtils.getRandomObjectIgnoreFields():
java.lang.reflect.Constructor<T> constructor = tClass.getDeclaredConstructor();
constructor.setAccessible(true);  // Required for @NoArgsConstructor(access = PROTECTED)
T t = constructor.newInstance();
```

### Compile check

```bash
cd D:/open-wes/server && ./gradlew :modules-wes:<MODULE>:compileJava
```

Fix any remaining setter call sites. Common failures:
- Service classes calling `new Entity()` from different packages — convert to builder
- Repository impls setting FK — add `initialize()` method
- Transfer (MapStruct) methods — MapStruct auto-detects builder, just rebuild

### Commit

```bash
git add -A && git commit -m "refactor(<module>): replace @Data with @Getter @Builder on domain entities"
```

## Phase 2: Create Use Case Classes

For each aggregate class, create a corresponding UseCase in `application/usecase/`.

### UseCase template:

```java
package org.openwes.wes.<module>.application.usecase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class <Action><Entity>UseCase {

    // Inject repositories, external APIs (IStockApi, etc.)

    @Transactional(rollbackFor = Exception.class)
    public void execute(<params>) {
        // 1. Load entities from repositories
        // 2. Call domain methods on entities
        // 3. Call external APIs if needed
        // 4. Save entities back to repositories
    }
}
```

### Migration rules:

| Aggregate pattern | UseCase equivalent |
|-------------------|-------------------|
| Aggregate constructor injection | UseCase constructor injection (same) |
| `@Transactional` on aggregate method | `@Transactional` on UseCase `execute()` |
| Subscriber logic before aggregate call | Merge into UseCase (status checks, cache prep) |
| Multiple aggregate methods | One UseCase per business operation |

### Naming convention:

`[Action][Entity]UseCase` — e.g.:
- `CreateInboundPlanOrderUseCase`
- `AcceptInboundOrderUseCase`
- `AdjustStockUseCase`

### Compile check

```bash
cd D:/open-wes/server && ./gradlew :modules-wes:<MODULE>:compileJava
```

### Commit

```bash
git add -A && git commit -m "feat(<module>): add UseCase classes for DDD refactoring"
```

## Phase 3: Redirect Call Chains

### Event subscribers

For each subscriber method that currently calls an aggregate:

```java
// Before:
@Subscribe
public void onCreateEvent(SomeEvent event) {
    // ... prepare data ...
    someAggregate.doSomething(data);
}

// After:
@Subscribe
@Transactional(rollbackFor = Exception.class)  // Add if write path
public void onCreateEvent(SomeEvent event) {
    someUseCase.execute(event.getAggregatorId());
}
```

**Rules:**
- Add `@Transactional(rollbackFor = Exception.class)` on **all write-path** handlers
- Read-only handlers (e.g., callbacks, notifications) don't need `@Transactional`
- Move data preparation logic into the UseCase

### ApiImpl classes

Replace aggregate references with UseCase references:

```java
// Before:
private final SomeAggregate someAggregate;

// After:
private final SomeUseCase someUseCase;
```

### Schedulers

Same pattern — replace aggregate calls with UseCase calls.

### Compile check + commit

```bash
cd D:/open-wes/server && ./gradlew :modules-wes:<MODULE>:compileJava
git add -A && git commit -m "refactor(<module>): redirect call chains to UseCases"
```

## Phase 4: Cleanup

### Verify no remaining aggregate references

```bash
grep -rn "Aggregate" modules-wes/<MODULE>/src/main/java/ --include="*.java" \
  | grep -v "AggregatorRoot\|usecase\|test"
```

### Delete aggregate files

```bash
rm -rf modules-wes/<MODULE>/src/main/java/org/openwes/wes/<module>/domain/aggregate/
```

If some aggregates are still referenced (complex cases deferred), keep them and note in a TODO.

### Clean up unused Transfer methods

Check `*Transfer.java` for methods that referenced aggregate classes.

### Compile + test + commit

```bash
cd D:/open-wes/server && ./gradlew :modules-wes:<MODULE>:compileJava
cd D:/open-wes/server && ./gradlew :modules-wes:<MODULE>:test
git add -A && git commit -m "refactor(<module>): delete aggregate classes, replaced by UseCases"
```

## Phase 5: Full Build Verification

```bash
cd D:/open-wes/server && ./gradlew compileJava
```

Must pass — other modules may depend on changed APIs or events.

## Checklist

Use this checklist for each module:

- [ ] Phase 0: Impact analysis complete, all setter call sites documented
- [ ] Phase 1: All entity `@Data` replaced with `@Getter @Builder`
- [ ] Phase 1: All external setter calls converted (builder / domain method)
- [ ] Phase 1: MapStruct regeneration verified (builders used in generated code)
- [ ] Phase 1: Module compiles
- [ ] Phase 2: UseCase classes created for each aggregate
- [ ] Phase 2: Module compiles
- [ ] Phase 3: Event subscribers redirect to UseCases + `@Transactional` on write paths
- [ ] Phase 3: ApiImpl classes redirect to UseCases
- [ ] Phase 3: Schedulers redirect to UseCases
- [ ] Phase 3: Module compiles
- [ ] Phase 4: Aggregate classes deleted (or deferred with TODO)
- [ ] Phase 4: Unused Transfer methods removed
- [ ] Phase 4: Module compiles and tests pass
- [ ] Phase 5: Full server `compileJava` passes

## Common Pitfalls

1. **Protected constructor access**: `ObjectUtils.getRandomObject()` needs `setAccessible(true)` — already fixed
2. **`@Builder.Default` forgotten**: Fields with initializers (e.g., `= 0`, `= Lists.newArrayList()`) need `@Builder.Default` or the default is lost
3. **`ModificationAware.setModified()`**: Must keep explicit `@Override` setter — it's an interface contract
4. **Cross-module events**: Don't remove domain events that monitoring or other modules subscribe to
5. **`@Data` on context/DTO objects**: It's OK to keep `@Data` on context objects, DTOs, and cancel contexts — only domain entities must use `@Builder`
6. **Entities without `AggregatorRoot`**: Don't add `@EqualsAndHashCode(callSuper = true)` — it's only for subclasses
7. **`final` fields in `AggregatorRoot`**: `ObjectUtils` reflection may fail on `final` fields from superclass — ensure test utilities handle this gracefully
