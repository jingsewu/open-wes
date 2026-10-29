# Script Plugin Phase 5 — End-to-End Acceptance Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Prove the script-plugin platform works against the live dev VM — Liquibase seed, management API, wes runtime (HTTP, cron, request-supply, wave), station handshake/custom API, governance, AI draft, and the browser workbench — and fix the one known flaky test before merge.

**Architecture:** Three Spring Boot servers run on the Windows host against the VMware VM at `192.168.196.128` (Nacos `test/openwes`, MySQL `3308/openwes`, Redis `6379` DB 3). wes (9010) hosts the management plane and local runtime; station (9040) hosts the remote runtime and validates through a Dubbo handshake; gateway (8090) fronts both. A temporary toolset under `%TEMP%\openwes-e2e` drives JDBC, Redis RESP, HTTP, SSE, and headless Chrome. The only repository change is the deterministic rewrite of one test.

**Tech Stack:** Java 17, Spring Boot 3.2.2, Gradle, Liquibase, MySQL 8, Redis/Redisson, Nacos 2, Dubbo, React 17 + AMIS, Chrome DevTools Protocol, Node 24, ffmpeg.

**Spec:** No single spec — this plan closes the open gates recorded for phases 1b–4. Authoritative contracts: `docs/standards/backend.md` (§Script Plugin Rules), `PluginContractCatalog`, and the phase plans `2026-10-08-script-plugin-phase1b-runtime-materialization.md`, `2026-10-22-script-plugin-phase2-ai-pipeline.md`, `2026-10-23-script-plugin-phase3a-backend-governance.md`, `2026-10-23-script-plugin-phase3b-client-workbench.md`, `2026-10-27-script-plugin-phase4-closing.md`.

---

## Global Constraints

- JDK 17: `JAVA_HOME="$HOME/.jdks/ms-17.0.20.1"`. The default `java` on PATH is JDK 8; never use it.
- VM endpoints: Nacos `192.168.196.128:8848` (namespace `test`, group `openwes`, dataIds `station`/`gateway`/`wes`/`ai`, no `.yml` suffix); MySQL `192.168.196.128:3308/openwes`; Redis `192.168.196.128:6379` DB 3. Servers bind to host ports 8090/9010/9040.
- Every server starts with `-Dnacos.openwes.com=192.168.196.128`.
- **Secrets:** never print, commit, or write a password, token, or API key to a file. Read `OPENWES_DB_PASSWORD` and `OPENWES_ADMIN_PASSWORD` from the shell environment. The dev admin credentials are recorded in the project memory `client-ui-verification-without-build`; do not copy them into this plan or any repository file. The live `wes` Nacos config holds `${DEEPSEEK_API_KEY}`; the real key is supplied through the process environment only — never print the config, the environment value, or the key.
- **Do not edit** any SQL file referenced by an applied Liquibase changeset, and never modify `/db/changelog/*.xml` in this phase.
- **Nacos write requires explicit user approval.** Task 4 stops, shows a diff, and waits. Do not update Nacos before that approval.
- Do not merge, push, or open a PR. Task 10 only presents options.
- Do not commit `docs/superpowers/plans/*`, `.superpowers/`, `.claude/settings.local.json`, or any temp tooling. Only the test file in Task 1 is committed.
- English only in code, logs, commit messages, and docs. No debug `console.log` in committed client code.
- A check is PASS only when the command output in this session shows it. Record failures verbatim; never infer a pass. Items that cannot run are BLOCKED, not PASS.
- Work directory: `E2E=/c/Users/niekang/AppData/Local/Temp/openwes-e2e`. All helper scripts and evidence live outside the repository.
- Evidence: write the observed output of each task to `$E2E/results/taskN.md` (`mkdir -p` first). The final results table in Task 10 references those files.
- Run each task's steps in one shell invocation where possible; evidence blocks reuse task-local variables. Persistent values are `$E2E/.token`, `$E2E/.rev1_id`, `$E2E/.rev2_id`, `$E2E/.srev_id`.
- Existing flake: `ScriptPluginServiceTest.validate_samePluginAndRevision_concurrentCallersShareOnePendingRequest` is load-sensitive and must be deterministic before any merge.

---

## Environment Setup (run once at the start of every shell)

```bash
export E2E="/c/Users/niekang/AppData/Local/Temp/openwes-e2e"
export JAVA_HOME="$HOME/.jdks/ms-17.0.20.1"
export JAVA17="$JAVA_HOME/bin/java"
export NACOS="192.168.196.128:8848"
export DB_HOST="192.168.196.128"
export DB_PORT="3308"
export REDIS_HOST="192.168.196.128"
export REDIS_PORT="6379"
: "${OPENWES_DB_PASSWORD:?export OPENWES_DB_PASSWORD for the dev VM MySQL first}"
: "${OPENWES_ADMIN_PASSWORD:?export OPENWES_ADMIN_PASSWORD for the dev admin user first}"
export OPENWES_DB_USER="${OPENWES_DB_USER:-root}"
export E2E_WIN="$(cygpath -m "$E2E")"
export TOKEN="$(cat "$E2E/.token" 2>/dev/null || true)"
mkdir -p "$E2E"/{tools,payloads,config,results,shots,logs}
cd /d/open-wes
```

Expected: no error. If either `:?` message appears, stop and obtain the credential from the environment owner.

**Shell state does not persist between tool invocations.** Re-run this block in every new shell. Later tasks persist `$TOKEN` in `$E2E/.token` and revision IDs in `$E2E/.rev1_id` / `$E2E/.rev2_id` / `$E2E/.srev_id`; the setup line above reloads the token. Pass `$E2E_WIN` (not `$E2E`) to `node`, `curl --data-binary @...`, `ffmpeg`, and other native Windows tools.

---

## File Structure

| Path | Responsibility |
|---|---|
| `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java` | Task 1: deterministic concurrent-validation test (only committed change) |
| `$E2E/tools/DbQuery.java` | Read-only JDBC query helper; credentials from env |
| `$E2E/tools/DbExec.java` | JDBC DML helper for fixtures and cleanup |
| `$E2E/tools/jsonq.js` | Extract one dot-path value from stdin JSON |
| `$E2E/tools/redis-cmd.js` | Minimal RESP client for simple Redis replies |
| `$E2E/tools/build-payloads.js` | Writes every JSON fixture used by Tasks 5–8 |
| `$E2E/tools/cdp-capture.js` | Headless-Chrome login + 12 screenshots + console/network capture |
| `$E2E/results/taskN.md` | Evidence per task |
| `$E2E/contact-sheet.png` | 3×4 visual contact sheet read in Task 9 |

---

## Task 1: Make the concurrent-validation test deterministic

**Files:**
- Modify: `server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java:31-55` (imports) and `:344-376` (test)

**Interfaces:**
- Consumes: `ScriptPluginService.validate(String)` and `reportValidation(ValidationReportDTO)` (unchanged).
- Produces: a deterministic test that waits until both callers are blocked on the shared future before reporting; no production change.

- [x] **Step 1: Record the flaky baseline**

Run the test five times, each in a fresh Gradle invocation:

```bash
cd /d/open-wes/server
for i in 1 2 3 4 5; do
  JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew -q \
    :modules-plugin:script-plugin-core:test \
    --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest.validate_samePluginAndRevision_concurrentCallersShareOnePendingRequest' \
    --rerun-tasks > "$E2E/logs/flake-baseline-$i.log" 2>&1
  echo "run $i exit=$?"
done
```

Expected: at least one non-zero exit or a failure in `$E2E/logs/flake-baseline-*.log` (the known flake); if all five pass, record that the flake did not reproduce but continue — the fix is still required for CI determinism.

- [x] **Step 2: Replace the imports**

Replace lines 31–55 of `ScriptPluginServiceTest.java` so the import block reads:

```java
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
```

`java.util.concurrent.Callable` is removed; `CountDownLatch`, `ExecutorService`, `Executors`, and `Future` are still used by other tests and by the new test.

- [x] **Step 3: Replace the test method**

Replace the whole method at lines 344–376 with:

```java
    @Test
    void validate_samePluginAndRevision_concurrentCallersShareOnePendingRequest() throws Exception {
        ScriptPlugin plugin = plugin("station-api", "station", ScriptPluginStatusEnum.DRAFT);
        when(pluginRepository.findByPluginKey("station-api")).thenReturn(Optional.of(plugin));
        when(revisionRepository.findById(11L)).thenReturn(Optional.of(revision()));
        when(configRepository.findByPluginId(1L)).thenReturn(Optional.empty());
        org.springframework.test.util.ReflectionTestUtils.setField(service, "validationTimeoutMs", 30_000L);

        CountDownLatch start = new CountDownLatch(1);
        List<ValidationReportDTO> reports = Collections.synchronizedList(new ArrayList<>());
        List<Throwable> failures = Collections.synchronizedList(new ArrayList<>());
        Runnable validation = () -> {
            try {
                start.await();
                reports.add(service.validate("station-api"));
            } catch (Throwable e) {
                failures.add(e);
            }
        };
        Thread first = new Thread(validation, "phase5-validation-1");
        Thread second = new Thread(validation, "phase5-validation-2");
        first.start();
        second.start();
        start.countDown();

        awaitPendingValidation("station");
        awaitBothCallersWaitingOnPendingValidation(first, second);
        service.reportValidation(ValidationReportDTO.passed("station-api", 11L, "station"));

        first.join(5_000L);
        second.join(5_000L);
        assertFalse(first.isAlive(), "first caller must return");
        assertFalse(second.isAlive(), "second caller must return");
        assertTrue(failures.isEmpty(), () -> "validation failed: " + failures);
        assertEquals(2, reports.size());
        assertTrue(reports.get(0).isPassed());
        assertTrue(reports.get(1).isPassed());
        assertSame(reports.get(0), reports.get(1), "both callers must receive the shared report");
        verify(redisUtils, times(1)).publish(any(), any());
    }

    private void awaitBothCallersWaitingOnPendingValidation(Thread... threads) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        while (System.nanoTime() < deadline) {
            boolean allWaiting = true;
            for (Thread thread : threads) {
                allWaiting &= thread.getState() == Thread.State.TIMED_WAITING;
            }
            if (allWaiting) {
                return;
            }
            Thread.sleep(10);
        }
        fail("both validation callers must be waiting on the shared pending validation");
    }
```

- [x] **Step 4: Run the test once**

```bash
cd /d/open-wes/server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew -q \
  :modules-plugin:script-plugin-core:test \
  --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest.validate_samePluginAndRevision_concurrentCallersShareOnePendingRequest' \
  --rerun-tasks
```

Expected: exit 0, BUILD SUCCESSFUL.

- [x] **Step 5: Run it twenty times**

```bash
cd /d/open-wes/server
passes=0
for i in $(seq 1 20); do
  if JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew -q \
      :modules-plugin:script-plugin-core:test \
      --tests 'org.openwes.script.plugin.core.domain.service.ScriptPluginServiceTest.validate_samePluginAndRevision_concurrentCallersShareOnePendingRequest' \
      --rerun-tasks > "$E2E/logs/flake-fixed-$i.log" 2>&1; then
    passes=$((passes + 1))
  else
    echo "FAILED on run $i"
  fi
done
echo "passes=$passes/20"
```

Expected: `passes=20/20`.

- [x] **Step 6: Run the whole module test suite**

```bash
cd /d/open-wes/server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew :modules-plugin:script-plugin-core:test
```

Expected: BUILD SUCCESSFUL, no failures.

- [x] **Step 7: Commit only the test file**

```bash
cd /d/open-wes
git add server/modules-plugin/script-plugin-core/src/test/java/org/openwes/script/plugin/core/domain/service/ScriptPluginServiceTest.java
git commit -m "test(script-plugin): wait for both validation callers before reporting

The concurrent-validation test slept 100 ms after the first pending
request appeared, which did not guarantee the second caller had entered
the shared wait. Under load the report could arrive first and the second
caller started a fresh request, failing the publish-count assertion.
Wait for both caller threads to block on the shared future instead.

Co-Authored-By: Claude Code <noreply@anthropic.com>"
git status --short
```

Expected: one file committed; `git status` shows only the untracked plan docs and the pre-existing `constants/` warning.

- [x] **Step 8: Record evidence**

```bash
{
  echo "# Task 1 — flaky test"
  echo "- baseline: see $E2E/logs/flake-baseline-*.log"
  echo "- fixed: passes=20/20 (paste the Step 5 output)"
  echo "- module suite: BUILD SUCCESSFUL (paste the tail of the Step 6 output)"
  echo "- commit: $(git log -1 --oneline)"
} > "$E2E/results/task1.md"
cat "$E2E/results/task1.md"
```

---

## Task 2: Build all backend jars

**Files:**
- No source changes. Produces `server/server/{wes,station,gateway}-server/build/libs/*-server.jar`.

**Interfaces:**
- Consumes: the Task 1 test fix.
- Produces: three runnable Spring Boot fat jars for Tasks 3–9.

- [x] **Step 1: Full build without bootJar**

```bash
cd /d/open-wes/server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew build -x bootJar
```

Expected: BUILD SUCCESSFUL. Allow up to 20 minutes. The no-main-class aggregator `bootJar` tasks are excluded deliberately; plain `build` fails on them for pre-existing reasons.

- [x] **Step 2: Build the three server jars**

```bash
cd /d/open-wes/server
JAVA_HOME="$HOME/.jdks/ms-17.0.20.1" ./gradlew \
  :server:wes-server:bootJar :server:station-server:bootJar :server:gateway-server:bootJar
```

Expected: BUILD SUCCESSFUL.

- [x] **Step 3: Verify the jars are fresh**

```bash
cd /d/open-wes
ls -l --time-style=full-iso server/server/wes-server/build/libs/wes-server.jar \
  server/server/station-server/build/libs/station-server.jar \
  server/server/gateway-server/build/libs/gateway-server.jar
```

Expected: all three timestamps are from this run (today, within the last hour).

- [x] **Step 4: Record evidence**

```bash
{
  echo "# Task 2 — build"
  echo "- build -x bootJar: BUILD SUCCESSFUL"
  echo "- bootJar: BUILD SUCCESSFUL"
  echo "- jars:"
  ls -l --time-style=full-iso server/server/*/build/libs/*-server.jar
} > "$E2E/results/task2.md"
cat "$E2E/results/task2.md"
```

---

## Task 3: Create the temp tooling, start wes, and verify the Liquibase seed

**Files:**
- Create: `$E2E/tools/DbQuery.java`, `$E2E/tools/DbExec.java`, `$E2E/tools/jsonq.js`, `$E2E/tools/redis-cmd.js`
- Create: `$E2E/results/task3.md`

**Interfaces:**
- Consumes: the wes jar from Task 2; env credentials.
- Produces: `$E2E/tools/out/` compiled JDBC classes; a running wes on 9010; proofs that changesets `create_p_script_plugin_ai_setting`, `create_p_script_plugin_ai_generation`, `seed_p_script_plugin_ai_setting`, `create_p_script_plugin_operation_audit`, `seed_script_plugin_workbench_roles`, `seed_script_plugin_workbench_menus`, `seed_script_plugin_workbench_grants` ran.

- [x] **Step 1: Write `DbQuery.java`**

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

/** Read-only JDBC helper for the Open WES dev VM. Not part of the repository. */
public final class DbQuery {

    private static final String URL = "jdbc:mysql://"
            + System.getenv().getOrDefault("DB_HOST", "192.168.196.128")
            + ":" + System.getenv().getOrDefault("DB_PORT", "3308")
            + "/openwes?useSSL=false&serverTimezone=Asia/Shanghai"
            + "&allowPublicKeyRetrieval=true&characterEncoding=utf8";

    public static void main(String[] args) throws Exception {
        String user = System.getenv().getOrDefault("OPENWES_DB_USER", "root");
        String password = System.getenv("OPENWES_DB_PASSWORD");
        if (password == null) {
            System.err.println("OPENWES_DB_PASSWORD is required");
            System.exit(2);
        }
        if (args.length == 0) {
            System.err.println("usage: DbQuery \"SELECT ...\" [more SQL...]");
            System.exit(2);
        }
        try (Connection connection = DriverManager.getConnection(URL, user, password);
             Statement statement = connection.createStatement()) {
            for (String sql : args) {
                System.out.println("-- " + sql);
                try (ResultSet rs = statement.executeQuery(sql)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int columns = meta.getColumnCount();
                    while (rs.next()) {
                        StringBuilder line = new StringBuilder();
                        for (int i = 1; i <= columns; i++) {
                            if (i > 1) {
                                line.append(" | ");
                            }
                            line.append(meta.getColumnLabel(i)).append('=').append(rs.getString(i));
                        }
                        System.out.println(line);
                    }
                }
            }
        }
    }
}
```

- [x] **Step 2: Write `DbExec.java`**

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/** JDBC DML helper for fixtures. Not part of the repository. */
public final class DbExec {

    private static final String URL = "jdbc:mysql://"
            + System.getenv().getOrDefault("DB_HOST", "192.168.196.128")
            + ":" + System.getenv().getOrDefault("DB_PORT", "3308")
            + "/openwes?useSSL=false&serverTimezone=Asia/Shanghai"
            + "&allowPublicKeyRetrieval=true&characterEncoding=utf8";

    public static void main(String[] args) throws Exception {
        String user = System.getenv().getOrDefault("OPENWES_DB_USER", "root");
        String password = System.getenv("OPENWES_DB_PASSWORD");
        if (password == null) {
            System.err.println("OPENWES_DB_PASSWORD is required");
            System.exit(2);
        }
        if (args.length == 0) {
            System.err.println("usage: DbExec \"INSERT ...\" [more DML...]");
            System.exit(2);
        }
        try (Connection connection = DriverManager.getConnection(URL, user, password);
             Statement statement = connection.createStatement()) {
            for (String sql : args) {
                int rows = statement.executeUpdate(sql);
                System.out.println(rows + " row(s): " + sql);
            }
        }
    }
}
```

- [x] **Step 3: Write `jsonq.js`**

```js
"use strict";
let input = "";
process.stdin.on("data", (chunk) => { input += chunk; });
process.stdin.on("end", () => {
  const path = process.argv[2] || "";
  let value;
  try {
    value = JSON.parse(input);
  } catch (error) {
    console.error("invalid JSON: " + error.message);
    process.exit(2);
  }
  for (const key of path.split(".").filter(Boolean)) {
    if (value === null || value === undefined) break;
    value = value[key];
  }
  if (value === undefined || value === null) {
    console.log("");
    return;
  }
  console.log(typeof value === "object" ? JSON.stringify(value) : String(value));
});
```

- [x] **Step 4: Write `redis-cmd.js`**

```js
"use strict";
// Simple-command Redis client (SELECT + one command). Prints raw RESP lines.
const net = require("net");
const host = process.env.REDIS_HOST || "192.168.196.128";
const port = Number(process.env.REDIS_PORT || 6379);
const [dbArg, ...command] = process.argv.slice(2);
if (command.length === 0) {
  console.error("usage: node redis-cmd.js <db> <command> [args...]");
  process.exit(2);
}
const db = Number(dbArg);
const socket = net.createConnection({ host, port });
let output = "";
function encode(parts) {
  let out = "*" + parts.length + "\r\n";
  for (const part of parts) {
    const text = String(part);
    out += "$" + Buffer.byteLength(text) + "\r\n" + text + "\r\n";
  }
  return out;
}
socket.on("connect", () => socket.write(encode(["SELECT", db]) + encode(command)));
socket.on("data", (chunk) => { output += chunk.toString(); });
socket.setTimeout(3000);
socket.on("timeout", () => socket.destroy(new Error("redis timeout")));
setTimeout(() => socket.end(), 500);
socket.on("close", () => {
  const lines = output.split("\r\n").filter((line) => line.length > 0);
  console.log(lines.join("\n"));
  process.exit(output.startsWith("-") ? 1 : 0);
});
socket.on("error", (error) => {
  console.error("redis error: " + error.message);
  process.exit(1);
});
```

- [x] **Step 5: Compile the JDBC helpers**

```bash
cd "$E2E/tools"
mkdir -p "$E2E/tools/out"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
"$JAVA_HOME/bin/javac" -cp "$DRIVER" -d "$E2E_WIN/tools/out" DbQuery.java DbExec.java
OUT=$(cygpath -w "$E2E/tools/out")
"$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery "SELECT 1 AS ok"
```

Expected: `ok=1`.

- [x] **Step 6: Snapshot the pre-start database state**

```bash
cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
"$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
  "SELECT id FROM l_change_log WHERE id IN ('create_p_script_plugin_ai_setting','create_p_script_plugin_ai_generation','seed_p_script_plugin_ai_setting','create_p_script_plugin_operation_audit','seed_script_plugin_workbench_roles','seed_script_plugin_workbench_menus','seed_script_plugin_workbench_grants')" \
  "SELECT COUNT(*) AS ai_setting_tables FROM information_schema.tables WHERE table_schema='openwes' AND table_name IN ('p_script_plugin_ai_setting','p_script_plugin_ai_generation')" \
  "SELECT COUNT(*) AS audit_tables FROM information_schema.tables WHERE table_schema='openwes' AND table_name='p_script_plugin_operation_audit'"
```

Expected before the first start: no rows from `l_change_log`, `ai_setting_tables=0`, `audit_tables=0`. If rows already exist, record that and skip the "newly applied" claim for those changesets.

- [x] **Step 7: Start wes in the background**

Stop any stale server already holding the port (the previous session may have left one; the old jar lacks this branch's code):

```bash
pid=$(netstat -ano | grep ':9010 ' | grep LISTENING | awk '{print $5}' | head -1)
if [ -n "$pid" ]; then
  echo "stopping stale 9010 pid=$pid"
  taskkill //PID "$pid" //F || true
  sleep 2
fi
```

Then start wes:

```bash
cd /d/open-wes/server/server/wes-server/build/libs
nohup "$JAVA17" -Dnacos.openwes.com=192.168.196.128 -jar wes-server.jar \
  > "$E2E/logs/wes.log" 2>&1 &
echo $! > "$E2E/logs/wes.pid"
echo "wes pid=$(cat "$E2E/logs/wes.pid")"
```

- [x] **Step 8: Wait for wes health**

```bash
for i in $(seq 1 120); do
  code=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:9010/actuator/health || true)
  [ "$code" = "200" ] && break
  sleep 2
done
echo "health=$code"
curl -s http://localhost:9010/actuator/health
grep -c "Started WesServerApplication" "$E2E/logs/wes.log"
```

Expected: `health=200`, JSON `{"status":"UP"}`, and at least one `Started WesServerApplication` line. Allow up to 4 minutes.

- [x] **Step 9: Check the log for Liquibase and startup errors**

```bash
grep -i "liquibase" "$E2E/logs/wes.log" | tail -20
grep -iE "ERROR|Exception" "$E2E/logs/wes.log" | tail -40
```

Expected: Liquibase ran the two changesets without checksum errors. If startup failed, read the last 120 lines of the log and stop; do not edit applied changelogs.

- [x] **Step 10: Verify the changesets, seed rows, and audit table**

```bash
cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
"$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
  "SELECT id FROM l_change_log WHERE id IN ('create_p_script_plugin_ai_setting','create_p_script_plugin_ai_generation','seed_p_script_plugin_ai_setting','create_p_script_plugin_operation_audit','seed_script_plugin_workbench_roles','seed_script_plugin_workbench_menus','seed_script_plugin_workbench_grants') ORDER BY id" \
  "SELECT code, name, status FROM u_role WHERE code IN ('pluginDeveloper','pluginAdmin') ORDER BY id" \
  "SELECT COUNT(*) AS menu_rows FROM u_menu WHERE id BETWEEN 1900000000 AND 1900000004" \
  "SELECT COUNT(*) AS grant_rows FROM u_role_menu WHERE id BETWEEN 1900000301 AND 1900000315" \
  "SELECT singleton_key, daily_generation_limit, max_tokens_per_call, max_attempts FROM p_script_plugin_ai_setting" \
  "SHOW COLUMNS FROM p_script_plugin_operation_audit"
```

Expected: 7 changeset IDs; two role rows with `status=1`; `menu_rows=5`; `grant_rows=9`; `DEFAULT | 20 | 4096 | 3`; audit columns `id, plugin_key, action, revision_id, operator, success, detail, created_at`.

- [x] **Step 11: Record evidence**

```bash
{
  echo "# Task 3 — wes + Liquibase"
  echo "## health"
  curl -s http://localhost:9010/actuator/health
  echo
  echo "## liquibase log"
  grep -i "liquibase" "$E2E/logs/wes.log" | tail -20
  echo "## seeded state"
  cd "$E2E/tools"
  DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
  OUT=$(cygpath -w "$E2E/tools/out")
  "$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
    "SELECT id FROM l_change_log WHERE id LIKE '%script_plugin%' ORDER BY id" \
    "SELECT code FROM u_role WHERE code IN ('pluginDeveloper','pluginAdmin')" \
    "SELECT COUNT(*) AS menus FROM u_menu WHERE id BETWEEN 1900000000 AND 1900000004" \
    "SELECT COUNT(*) AS grants FROM u_role_menu WHERE id BETWEEN 1900000301 AND 1900000315"
} > "$E2E/results/task3.md" 2>&1
cat "$E2E/results/task3.md"
```

---

## Task 4: Update the stale gateway route in Nacos (approval gate), then start station and gateway

**Files:**
- Create: `$E2E/config/gateway-before.yml`, `$E2E/config/gateway-after.yml`, `$E2E/config/gateway-verify.yml`
- Create: `$E2E/tools/update-gateway-route.js`
- Create: `$E2E/results/task4.md`

**Interfaces:**
- Consumes: Nacos `gateway` dataId (namespace `test`, group `openwes`); the canonical seed at `initdb.d/nacos_config.sql` already has the desired routes, so no repository change is needed.
- Produces: gateway route `plugin-platform → lb://wes` with no rewrite; running station (9040) and gateway (8090); a `$TOKEN` for later tasks.

- [x] **Step 1: Fetch and inspect the live gateway config**

```bash
curl -s "http://$NACOS/nacos/v1/cs/configs?dataId=gateway&group=openwes&tenant=test" \
  -o "$E2E_WIN/config/gateway-before.yml"
grep -nE "id: plugin|uri: lb://plugin|id: plugin-platform|Path=/plugin-platform" "$E2E/config/gateway-before.yml"
```

Expected current stale state: `uri: lb://plugin`, no `plugin-platform` route. If the config already has `plugin-platform` and `plugin → lb://wes`, record "already canonical" and skip Steps 2–5.

- [x] **Step 2: Write the route transformer**

```js
"use strict";
const fs = require("node:fs");
const [input, output] = process.argv.slice(2);
let yaml = fs.readFileSync(input, "utf8");
if (yaml.includes("id: plugin-platform")) {
  console.log("plugin-platform route already present; nothing to do");
  fs.writeFileSync(output, yaml);
  process.exit(0);
}
if (!yaml.includes("uri: lb://plugin\n")) {
  console.error("expected the stale 'uri: lb://plugin' line; refusing to guess");
  process.exit(3);
}
yaml = yaml.replace("uri: lb://plugin\n", "uri: lb://wes\n");
const marker = "        - id: station\n";
if (!yaml.includes(marker)) {
  console.error("station route marker not found; refusing to guess");
  process.exit(4);
}
const block = [
  "        - id: plugin-platform\n",
  "          uri: lb://wes\n",
  "          predicates:\n",
  "            - Path=/plugin-platform/**\n",
].join("");
yaml = yaml.replace(marker, block + marker);
fs.writeFileSync(output, yaml);
console.log("wrote " + output);
```

- [x] **Step 3: Generate the after-config and show the diff**

```bash
node "$E2E_WIN/tools/update-gateway-route.js" \
  "$E2E_WIN/config/gateway-before.yml" "$E2E_WIN/config/gateway-after.yml"
diff -u "$E2E/config/gateway-before.yml" "$E2E/config/gateway-after.yml" || true
grep -nE "id: plugin|uri: lb://plugin|id: plugin-platform|Path=/plugin-platform|id: station" "$E2E/config/gateway-after.yml"
```

Expected diff: `uri: lb://plugin` → `uri: lb://wes`, plus the inserted `plugin-platform` route before `station`, with no `RewritePath` filter.

- [x] **Step 4: STOP — request explicit approval**

Print the diff to the user and ask: "May I update the live Nacos `gateway` dataId in namespace `test` with this diff?" Do not POST anything until the user answers yes. If the answer is no, do not start the gateway; mark every gateway-dependent check (workbench browser, gateway routes) BLOCKED and continue only with direct-service checks.

- [x] **Step 5: After approval, write the config and verify it**

```bash
curl -s -X POST "http://$NACOS/nacos/v1/cs/configs" \
  --data-urlencode "dataId=gateway" \
  --data-urlencode "group=openwes" \
  --data-urlencode "tenant=test" \
  --data-urlencode "type=yaml" \
  --data-urlencode "content@$E2E_WIN/config/gateway-after.yml"
echo
curl -s "http://$NACOS/nacos/v1/cs/configs?dataId=gateway&group=openwes&tenant=test" \
  -o "$E2E_WIN/config/gateway-verify.yml"
diff -u "$E2E/config/gateway-after.yml" "$E2E/config/gateway-verify.yml" && echo "NACOS_CONTENT_MATCH"
```

Expected: POST returns `true`; `NACOS_CONTENT_MATCH`. If not, stop and show both files.

- [x] **Step 6: Start station and gateway**

Stop stale servers on 9040/8090 first, so the health checks below cannot be satisfied by an old process:

```bash
for port in 9040 8090; do
  pid=$(netstat -ano | grep ":$port " | grep LISTENING | awk '{print $5}' | head -1)
  if [ -n "$pid" ]; then
    echo "stopping stale $port pid=$pid"
    taskkill //PID "$pid" //F || true
    sleep 2
  fi
done
```

Then start station and gateway:

```bash
cd /d/open-wes/server/server/station-server/build/libs
nohup "$JAVA17" -Dnacos.openwes.com=192.168.196.128 -jar station-server.jar \
  > "$E2E/logs/station.log" 2>&1 &
echo $! > "$E2E/logs/station.pid"

cd /d/open-wes/server/server/gateway-server/build/libs
nohup "$JAVA17" -Dnacos.openwes.com=192.168.196.128 -jar gateway-server.jar \
  > "$E2E/logs/gateway.log" 2>&1 &
echo $! > "$E2E/logs/gateway.pid"
```

- [x] **Step 7: Wait for station and gateway**

```bash
for i in $(seq 1 120); do
  scode=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:9040/actuator/health || true)
  gcode=$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8090/actuator/health || true)
  if [ "$scode" = "200" ] && [ "$gcode" = "200" ]; then break; fi
  sleep 2
done
echo "station=$scode gateway=$gcode"
grep -c "Started StationServerApplication" "$E2E/logs/station.log"
grep -c "Started GatewayServerApplication" "$E2E/logs/gateway.log"
```

Expected: both 200 and one "Started" line each. If the gateway has no health endpoint, use `curl -s -o /dev/null -w '%{http_code}' -X POST http://localhost:8090/user/api/auth/signin -H 'Content-Type: application/json' -d '{}'` (400/401 proves it is up) and record that.

- [x] **Step 8: Verify Nacos discovery and the new route**

```bash
for svc in wes station gateway; do
  echo "-- $svc"
  curl -s "http://$NACOS/nacos/v1/ns/instance/list?serviceName=$svc&namespaceId=test&groupName=openwes"
done
curl -s -o /dev/null -w 'unauth_plugin_platform=%{http_code}\n' \
  http://localhost:8090/plugin-platform/scriptPlugin/list
```

Expected: `wes` and `station` each contain `"hosts":[` with a host and `"healthy":true`; `gateway` is informational (the gateway may not register itself); `unauth_plugin_platform=401`. If `wes` or `station` is empty, try `groupName=DEFAULT_GROUP` and record; if still empty, stop and diagnose registration before proceeding.

- [x] **Step 9: Log in through the gateway and verify capabilities**

```bash
LOGIN=$(curl -s -X POST http://localhost:8090/user/api/auth/signin \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"admin\",\"password\":\"$OPENWES_ADMIN_PASSWORD\"}")
TOKEN=$(printf '%s' "$LOGIN" | node "$E2E_WIN/tools/jsonq.js" token)
test -n "$TOKEN" && echo "TOKEN_CAPTURED"
export TOKEN
umask 077
printf '%s' "$TOKEN" > "$E2E/.token"
curl -s -H "Authorization: $TOKEN" \
  http://localhost:8090/plugin-platform/scriptPlugin/capabilities
```

Expected: `TOKEN_CAPTURED`; capabilities `{"code":"0",...,"data":{"canDevelop":true,"canAdminister":true,"roles":["superAdmin"]}}` (roles may include more). Do not print `$LOGIN` or `$TOKEN`.

- [x] **Step 10: Record evidence**

```bash
{
  echo "# Task 4 — gateway + servers"
  echo "## approvals"
  echo "Nacos gateway update: approved by user (record the exact approval message)"
  echo "## route diff"
  diff -u "$E2E/config/gateway-before.yml" "$E2E/config/gateway-after.yml" || true
  echo "## nacos verify"
  diff -u "$E2E/config/gateway-after.yml" "$E2E/config/gateway-verify.yml" && echo NACOS_CONTENT_MATCH
  echo "## discovery"
  for svc in wes station gateway; do
    echo "-- $svc"
    curl -s "http://$NACOS/nacos/v1/ns/instance/list?serviceName=$svc&namespaceId=test&groupName=openwes"
  done
  echo "## capabilities"
  curl -s -H "Authorization: $TOKEN" http://localhost:8090/plugin-platform/scriptPlugin/capabilities
} > "$E2E/results/task4.md" 2>&1
cat "$E2E/results/task4.md"
```

---

## Task 5: WES plugin — HTTP, injection, cron, request-supply, wave

**Files:**
- Create: `$E2E/tools/build-payloads.js`
- Create: `$E2E/payloads/*.json`
- Create: `$E2E/results/task5.md`

**Interfaces:**
- Consumes: `$TOKEN`, gateway 8090, wes 9010, `DbExec`/`DbQuery`, `redis-cmd.js`.
- Produces: plugin `e2e-wes-plugin` ENABLED at revision 1; execution-log rows for `HTTP_ENDPOINT`, `CRON`, `REQUEST_SUPPLY`, `OUTBOUND_WAVE_WAVE_ACTION`; two fixture orders sharing one wave.

- [x] **Step 1: Write the payload builder**

```js
"use strict";
const fs = require("node:fs");
const out = process.argv[2];
if (!out) {
  console.error("usage: node build-payloads.js <output-dir>");
  process.exit(2);
}
fs.mkdirSync(out, { recursive: true });
const write = (name, value) =>
  fs.writeFileSync(require("node:path").join(out, name), JSON.stringify(value, null, 2));

const waveScript = `class E2eWave {
    def wave(orders, ctx) {
        ctx.log().info('e2e wave received {} orders for plugin {}', orders.size(), ctx.pluginKey())
        return [orders]
    }
}
`;

const httpScript = (revision) => `class E2eHttp {
    def handle(body, ctx) {
        ctx.log().info('e2e http /status called for plugin {}', ctx.pluginKey())
        return [status: 'ok', revision: '${revision}', plugin: ctx.pluginKey(),
                greeting: ctx.configString('greeting'), body: body]
    }
}
`;

const injectedScript = `import org.springframework.beans.factory.annotation.Autowired
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi

class E2eInjected {
    @Autowired
    IOutboundPlanOrderApi outboundPlanOrderApi

    def handle(body, ctx) {
        return [injected: outboundPlanOrderApi != null,
                injectedClass: outboundPlanOrderApi == null ? null : outboundPlanOrderApi.getClass().getName(),
                plugin: ctx.pluginKey()]
    }
}
`;

const cronScript = `class E2eCron {
    def run(ctx) {
        ctx.log().info('e2e cron tick for {}', ctx.pluginKey())
        return 'tick'
    }
}
`;

const supplyScript = `class E2eSupply {
    def supply(context, ctx) {
        ctx.log().info('e2e supply hook fired for apiType={}', context.apiType)
    }
}
`;

const extensions = [
  { type: "OUTBOUND_WAVE_WAVE_ACTION", priority: 100, script: "E2eWave.groovy", method: "wave" },
  { type: "HTTP_ENDPOINT", priority: 100, script: "E2eHttp.groovy", method: "handle", httpMethod: "GET", path: "/status" },
  { type: "HTTP_ENDPOINT", priority: 100, script: "E2eInjected.groovy", method: "handle", httpMethod: "GET", path: "/injected" },
  { type: "CRON", priority: 100, script: "E2eCron.groovy", method: "run", cron: "*/5 * * * * *" },
  { type: "REQUEST_SUPPLY", priority: 100, script: "E2eSupply.groovy", method: "supply", apiType: "ORDER_OUTBOUND_CREATE" }
];

const configSchema = [
  { field: "greeting", type: "STRING", required: true, defaultValue: "hello" },
  { field: "maxPerWave", type: "INT", required: true, defaultValue: "2" },
  { field: "failCron", type: "BOOLEAN", required: true, defaultValue: "false" }
];

const baseScripts = {
  "E2eWave.groovy": waveScript,
  "E2eHttp.groovy": httpScript("v1"),
  "E2eInjected.groovy": injectedScript,
  "E2eCron.groovy": cronScript,
  "E2eSupply.groovy": supplyScript
};

write("wes-create.json", {
  pluginKey: "e2e-wes-plugin",
  name: "E2E WES Plugin",
  description: "Phase 5 end-to-end verification",
  applySystem: "wes",
  applyModule: ""
});

write("wes-revision-1.json", {
  scripts: baseScripts,
  extensionPoints: extensions,
  configSchema,
  changeSummary: "revision 1: HTTP, injection, cron, supply, wave"
});

write("wes-revision-2.json", {
  scripts: Object.assign({}, baseScripts, { "E2eHttp.groovy": httpScript("v2") }),
  extensionPoints: extensions,
  configSchema,
  changeSummary: "revision 2: /status returns v2"
});

write("station-create.json", {
  pluginKey: "e2e-station-plugin",
  name: "E2E Station Plugin",
  description: "Phase 5 station custom API verification",
  applySystem: "station",
  applyModule: ""
});

write("station-revision.json", {
  scripts: {
    "E2eStationApi.groovy": `class E2eStationApi {
    def execute(parameter, ctx) {
        ctx.log().info('e2e station custom api workStationId={} event={}',
                parameter.workStationId, parameter.requestParameter)
        return null
    }
}
`
  },
  extensionPoints: [
    { type: "CUSTOM_API", priority: 100, script: "E2eStationApi.groovy", method: "execute", customApiCode: "E2E_STATION_API" }
  ],
  configSchema: [],
  changeSummary: "station custom api E2E_STATION_API"
});

write("ai-create.json", {
  pluginKey: "e2e-ai-plugin",
  name: "E2E AI Draft Plugin",
  description: "Phase 5 AI draft acceptance",
  applySystem: "wes",
  applyModule: ""
});

write("ai-stream-request.json", {
  pluginKey: "e2e-ai-plugin",
  requirement: "Create scripts for this plugin. applySystem is wes. Declare exactly one extension of type CRON with script file ai-cron.groovy, method run, and cron '0 0/1 * * * *'. The run method must call ctx.log().info('ai tick') and return the string 'ai-tick'. Do not declare HTTP, CUSTOM_API, wave, or request extensions. Use the standard bundle JSON format."
});

write("supply-request.json", {
  warehouseCode: "WH1",
  customerOrderNo: "E2E-SUPPLY-1",
  customerOrderType: "E2E",
  origPlatformCode: "E2E",
  priority: 0,
  details: [
    { ownerCode: "E2E", skuCode: "E2E-SKU-1", qtyRequired: 1 }
  ]
});

write("config-save-default.json", {
  values: { greeting: "hi", maxPerWave: 3, failCron: false },
  lockVersion: null
});
write("config-save-v0.json", {
  values: { greeting: "hi", maxPerWave: 3, failCron: false },
  lockVersion: 0
});
write("config-save-stale-v0.json", {
  values: { greeting: "stale", maxPerWave: 9, failCron: true },
  lockVersion: 0
});

write("test-run.json", {
  extensionType: "HTTP_ENDPOINT",
  script: "E2eHttp.groovy",
  method: "handle",
  arguments: { body: { probe: "test-run" } }
});
write("test-run-bad-method.json", {
  extensionType: "HTTP_ENDPOINT",
  script: "E2eHttp.groovy",
  method: "missingMethod",
  arguments: { body: {} }
});
write("station-invoke.json", {
  customApiCode: "E2E_STATION_API",
  event: { hello: "station", probe: 1 }
});
console.log("payloads written to " + out);
```

Run it:

```bash
node "$E2E_WIN/tools/build-payloads.js" "$E2E_WIN/payloads"
ls "$E2E/payloads"
```

Expected: 14 JSON files.

- [x] **Step 2: Create the WES draft**

```bash
CREATE=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/createDraft \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/wes-create.json")
printf '%s' "$CREATE" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$CREATE" | node "$E2E_WIN/tools/jsonq.js" data.pluginKey
printf '%s' "$CREATE" | node "$E2E_WIN/tools/jsonq.js" data.status
printf '%s' "$CREATE" | node "$E2E_WIN/tools/jsonq.js" data.sdkVersion
```

Expected: `0`, `e2e-wes-plugin`, `DRAFT`, and a non-empty contract version.

- [x] **Step 3: Save revision 1**

```bash
REV1=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/revision \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/wes-revision-1.json")
printf '%s' "$REV1" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$REV1" | node "$E2E_WIN/tools/jsonq.js" data.revisionNo
REV1_ID=$(printf '%s' "$REV1" | node "$E2E_WIN/tools/jsonq.js" data.id)
export REV1_ID
printf '%s' "$REV1_ID" > "$E2E/.rev1_id"
echo "REV1_ID=$REV1_ID"
printf '%s' "$REV1" | node "$E2E_WIN/tools/jsonq.js" data.contentHash
```

Expected: `0`, `1`, a numeric `REV1_ID`, and a 64-char hash.

- [x] **Step 4: Validate and enable**

```bash
VALID1=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/validate \
  -H "Authorization: $TOKEN")
printf '%s' "$VALID1" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$VALID1" | node "$E2E_WIN/tools/jsonq.js" data.passed
printf '%s' "$VALID1" | node "$E2E_WIN/tools/jsonq.js" data.failures

ENABLE1=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/enable \
  -H "Authorization: $TOKEN")
printf '%s' "$ENABLE1" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$ENABLE1" | node "$E2E_WIN/tools/jsonq.js" data.status
printf '%s' "$ENABLE1" | node "$E2E_WIN/tools/jsonq.js" data.currentRevisionId
```

Expected: `0`, `true`, empty failures; then `0`, `ENABLED`, `REV1_ID`.

- [x] **Step 5: Verify the HTTP endpoint (with materialization retry)**

```bash
STATUS=""
for i in $(seq 1 30); do
  STATUS=$(curl -s -w '\n%{http_code}' http://localhost:8090/plugin-platform/ext/e2e-wes-plugin/status \
    -H "Authorization: $TOKEN")
  code=$(printf '%s' "$STATUS" | tail -1)
  [ "$code" = "200" ] && break
  sleep 1
done
echo "http=$code"
printf '%s' "$STATUS" | sed '$d' | node "$E2E_WIN/tools/jsonq.js" data.status
printf '%s' "$STATUS" | sed '$d' | node "$E2E_WIN/tools/jsonq.js" data.revision
printf '%s' "$STATUS" | sed '$d' | node "$E2E_WIN/tools/jsonq.js" data.plugin
```

Expected: `http=200`, `ok`, `v1`, `e2e-wes-plugin`.

- [x] **Step 6: Verify Spring injection inside the script**

```bash
INJECTED=""
for i in $(seq 1 30); do
  INJECTED=$(curl -s -w '\n%{http_code}' http://localhost:8090/plugin-platform/ext/e2e-wes-plugin/injected \
    -H "Authorization: $TOKEN")
  code=$(printf '%s' "$INJECTED" | tail -1)
  [ "$code" = "200" ] && break
  sleep 1
done
echo "http=$code"
printf '%s' "$INJECTED" | sed '$d' | node "$E2E_WIN/tools/jsonq.js" data.injected
printf '%s' "$INJECTED" | sed '$d' | node "$E2E_WIN/tools/jsonq.js" data.injectedClass
```

Expected: `http=200`, `true`, and a non-empty implementation class name (not null).

- [x] **Step 7: Verify the cron execution**

```bash
cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
CRON=0
for i in $(seq 1 20); do
  CRON=$("$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
    "SELECT COUNT(*) AS cron_ok FROM p_script_plugin_execution_log WHERE plugin_key='e2e-wes-plugin' AND extension_type='CRON' AND success=1" \
    | sed -n 's/.*cron_ok=//p')
  [ "$CRON" -ge 1 ] && break
  sleep 1
done
echo "cron_ok=$CRON"
```

Expected: `cron_ok>=1` within 20 seconds.

- [x] **Step 8: Verify the request-supply hook**

```bash
SUPPLY=$(curl -s -w '\n%{http_code}' -X POST \
  "http://localhost:8090/api-platform/api/execute?apiType=ORDER_OUTBOUND_CREATE" \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/supply-request.json" || true)
echo "api_http=$(printf '%s' "$SUPPLY" | tail -1)"
printf '%s' "$SUPPLY" | sed '$d' | head -c 500
echo

cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
for i in $(seq 1 15); do
  N=$("$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
    "SELECT COUNT(*) AS supply_ok FROM p_script_plugin_execution_log WHERE plugin_key='e2e-wes-plugin' AND extension_type='REQUEST_SUPPLY' AND success=1" \
    | sed -n 's/.*supply_ok=//p')
  [ "$N" -ge 1 ] && break
  sleep 1
done
echo "supply_ok=$N"
```

Expected: `supply_ok>=1`. The API response may be a business error if the SKU does not exist; the supply hook runs before invocation, so the execution-log row is the acceptance signal. Record the API response verbatim.

- [x] **Step 9: Create the wave fixture and trigger the scheduler**

```bash
cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
"$JAVA17" -cp ".;$DRIVER;$OUT" DbExec \
  "DELETE FROM w_outbound_plan_order_detail WHERE id IN (9900000011,9900000012)" \
  "DELETE FROM w_outbound_plan_order WHERE id IN (9900000001,9900000002)" \
  "INSERT INTO w_outbound_plan_order (id, create_time, create_user, update_time, update_user, audit_time, audit_user, abnormal, abnormal_reason, carrier_code, customer_order_no, customer_order_type, customer_wave_no, expired_time, order_no, orig_platform_code, outbound_plan_order_status, priority, short_outbound, short_waiting, sku_kind_num, total_qty, version, warehouse_code, wave_no, waybill_no) VALUES (9900000001, 0, 'e2e', 0, 'e2e', 0, '', b'0', '', '', 'E2E-CO-1', 'E2E', '', 0, 'E2E-OP-1', 'E2E', 'ASSIGNED', 0, b'0', b'0', 1, 2, 0, 'WH1', '', '')" \
  "INSERT INTO w_outbound_plan_order (id, create_time, create_user, update_time, update_user, audit_time, audit_user, abnormal, abnormal_reason, carrier_code, customer_order_no, customer_order_type, customer_wave_no, expired_time, order_no, orig_platform_code, outbound_plan_order_status, priority, short_outbound, short_waiting, sku_kind_num, total_qty, version, warehouse_code, wave_no, waybill_no) VALUES (9900000002, 0, 'e2e', 0, 'e2e', 0, '', b'0', '', '', 'E2E-CO-2', 'E2E', '', 0, 'E2E-OP-2', 'E2E', 'ASSIGNED', 0, b'0', b'0', 1, 2, 0, 'WH1', '', '')" \
  "INSERT INTO w_outbound_plan_order_detail (id, create_time, create_user, update_time, update_user, outbound_plan_order_detail_status, outbound_plan_order_id, owner_code, qty_actual, qty_allocated, qty_required, sku_code, sku_id, sku_name, version) VALUES (9900000011, 0, 'e2e', 0, 'e2e', 'NEW', 9900000001, 'E2E', 0, 0, 2, 'E2E-SKU-1', 9900000101, 'E2E SKU 1', 0)" \
  "INSERT INTO w_outbound_plan_order_detail (id, create_time, create_user, update_time, update_user, outbound_plan_order_detail_status, outbound_plan_order_id, owner_code, qty_actual, qty_allocated, qty_required, sku_code, sku_id, sku_name, version) VALUES (9900000012, 0, 'e2e', 0, 'e2e', 'NEW', 9900000002, 'E2E', 0, 0, 2, 'E2E-SKU-1', 9900000101, 'E2E SKU 1', 0)"
node "$E2E_WIN/tools/redis-cmd.js" 3 DEL ":wms:outbound:plan:order:assigned:idsWH1"
node "$E2E_WIN/tools/redis-cmd.js" 3 RPUSH ":wms:outbound:plan:order:assigned:idsWH1" 9900000001 9900000002
```

Expected: the two DELETE lines report `0 row(s)` on a first run and `2 row(s)` on a rerun; each INSERT reports `1 row(s)`; then `+OK`/`:0`, then `+OK`/`:2`. The Redis key starts with a colon — keep it exactly as written.

`version` must be `0`, not `NULL`: Hibernate treats a versioned entity with a null version as transient, so the wave save would INSERT the existing orders and fail with `Duplicate entry ... PRIMARY`. Note also that a raw `RPUSH` stores plain `9900000001`, while Redisson's `JsonJacksonCodec` writes `["java.lang.Long",9900000001]`; the scheduler can read the raw value but `removeListByPureKey` cannot match it, so delete the key manually after verifying the wave (Step 10).

- [x] **Step 10: Wait for the wave and verify one wave for both orders**

```bash
cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
WAVE_OK=""
for i in $(seq 1 100); do
  WAVE_OK=$("$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
    "SELECT COUNT(*) AS ok FROM w_outbound_plan_order o JOIN w_outbound_wave w ON w.wave_no = o.wave_no WHERE o.id IN (9900000001,9900000002) AND w.wave_no <> '' AND JSON_LENGTH(w.outbound_plan_order_ids) = 2 GROUP BY w.wave_no HAVING COUNT(*) = 2" \
    | sed -n 's/.*ok=//p')
  [ "$WAVE_OK" = "2" ] && break
  sleep 2
done
echo "wave_ok=$WAVE_OK"
"$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
  "SELECT id, wave_no, outbound_plan_order_status FROM w_outbound_plan_order WHERE id IN (9900000001,9900000002)" \
  "SELECT COUNT(*) AS wave_log_ok FROM p_script_plugin_execution_log WHERE plugin_key='e2e-wes-plugin' AND extension_type='OUTBOUND_WAVE_WAVE_ACTION' AND success=1"

# the raw-pushed fixture values are not Redisson-encoded; remove the key so the scheduler does not retry
node "$E2E_WIN/tools/redis-cmd.js" 3 DEL ":wms:outbound:plan:order:assigned:idsWH1"
```

Expected: `wave_ok=2`, both orders share one non-empty `wave_no`, and `wave_log_ok>=1` within 150 seconds; the cleanup `DEL` reports `:1`. If the wave is not created, check the wes log for scheduler/plugin errors and stop.

- [x] **Step 11: Verify the executions page and stats (the never-exercised JPQL)**

```bash
EXEC=$(curl -s -H "Authorization: $TOKEN" \
  "http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/executions?page=1&pageSize=100")
printf '%s' "$EXEC" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$EXEC" | node "$E2E_WIN/tools/jsonq.js" data.total
printf '%s' "$EXEC" | node "$E2E_WIN/tools/jsonq.js" data.items.0.extensionType

STATS=$(curl -s -H "Authorization: $TOKEN" \
  "http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/executions/stats?hours=24")
printf '%s' "$STATS" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$STATS" | node "$E2E_WIN/tools/jsonq.js" data.total
printf '%s' "$STATS" | node "$E2E_WIN/tools/jsonq.js" data.failureRate
printf '%s' "$STATS" | node "$E2E_WIN/tools/jsonq.js" data.byType
```

Expected: `code=0` for both; `data.total>=4`; `failureRate` a number; `byType` a non-empty JSON array containing the extension types observed. This is the first live exercise of `statsByType`; if Hibernate throws, record the full error and stop.

- [x] **Step 12: Record evidence**

```bash
{
  REV1_ID=$(cat "$E2E/.rev1_id")
  echo "# Task 5 — WES plugin"
  echo "## create/revision/validate/enable outputs"
  echo "REV1_ID=$REV1_ID"
  echo "$VALID1"
  echo "$ENABLE1"
  echo "## http status"
  curl -s -H "Authorization: $TOKEN" http://localhost:8090/plugin-platform/ext/e2e-wes-plugin/status
  echo
  echo "## injected"
  curl -s -H "Authorization: $TOKEN" http://localhost:8090/plugin-platform/ext/e2e-wes-plugin/injected
  echo
  echo "## cron_ok=$CRON supply_ok=$N wave_ok=$WAVE_OK"
  cd "$E2E/tools"
  DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
  OUT=$(cygpath -w "$E2E/tools/out")
  "$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
    "SELECT id, wave_no FROM w_outbound_plan_order WHERE id IN (9900000001,9900000002)" \
    "SELECT extension_type, success, COUNT(*) AS n FROM p_script_plugin_execution_log WHERE plugin_key='e2e-wes-plugin' GROUP BY extension_type, success"
  echo "## executions"
  echo "$EXEC"
  echo "## stats"
  echo "$STATS"
} > "$E2E/results/task5.md" 2>&1
cat "$E2E/results/task5.md"
```

---

## Task 6: Station plugin — Dubbo validation handshake and CUSTOM_API

**Files:**
- Create: `$E2E/results/task6.md`

**Interfaces:**
- Consumes: station 9040 running; `$TOKEN`; `$E2E/payloads/station-*.json`.
- Produces: plugin `e2e-station-plugin` ENABLED on station; a successful `E2E_STATION_API` invocation and a `CUSTOM_API` execution-log row.

- [ ] **Step 1: Create the station draft**

```bash
SCREATE=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/createDraft \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/station-create.json")
printf '%s' "$SCREATE" | node "$E2E_WIN/tools/jsonq.js" data.pluginKey
printf '%s' "$SCREATE" | node "$E2E_WIN/tools/jsonq.js" data.status
```

Expected: `e2e-station-plugin`, `DRAFT`.

- [ ] **Step 2: Save the revision**

```bash
SREV=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-station-plugin/revision \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/station-revision.json")
printf '%s' "$SREV" | node "$E2E_WIN/tools/jsonq.js" data.revisionNo
SREV_ID=$(printf '%s' "$SREV" | node "$E2E_WIN/tools/jsonq.js" data.id)
export SREV_ID
printf '%s' "$SREV_ID" > "$E2E/.srev_id"
echo "SREV_ID=$SREV_ID"
```

Expected: `1`, numeric `SREV_ID`.

- [ ] **Step 3: Validate through the station handshake**

```bash
SVALID=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-station-plugin/validate \
  -H "Authorization: $TOKEN")
printf '%s' "$SVALID" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$SVALID" | node "$E2E_WIN/tools/jsonq.js" data.passed
printf '%s' "$SVALID" | node "$E2E_WIN/tools/jsonq.js" data.failures
```

Expected: `0`, `true`, empty failures. The call blocks until the station poller reports; allow up to 30 seconds. If it times out (`passed=false` with "did not report"), inspect `$E2E/logs/station.log` for Dubbo/poller errors and stop.

- [ ] **Step 4: Enable the station plugin**

```bash
SENABLE=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-station-plugin/enable \
  -H "Authorization: $TOKEN")
printf '%s' "$SENABLE" | node "$E2E_WIN/tools/jsonq.js" data.status
printf '%s' "$SENABLE" | node "$E2E_WIN/tools/jsonq.js" data.currentRevisionId
```

Expected: `ENABLED`, `SREV_ID`.

- [ ] **Step 5: Invoke the custom API and wait for the execution log**

```bash
cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
LOG_OK=0
for attempt in $(seq 1 35); do
  curl -s -o /dev/null -w 'station_put=%{http_code}\n' -X PUT \
    "http://localhost:8090/station/api?apiCode=CUSTOM_API" \
    -H "Authorization: $TOKEN" -H 'Content-Type: application/json' -H 'StationCode: 1' \
    --data-binary "@$E2E_WIN/payloads/station-invoke.json"
  LOG_OK=$("$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
    "SELECT COUNT(*) AS custom_ok FROM p_script_plugin_execution_log WHERE plugin_key='e2e-station-plugin' AND extension_type='CUSTOM_API' AND success=1" \
    | sed -n 's/.*custom_ok=//p')
  [ "$LOG_OK" -ge 1 ] && break
  sleep 2
done
echo "custom_ok=$LOG_OK"
grep -c "e2e station custom api" "$E2E/logs/station.log"
```

Expected: `station_put=200` and `custom_ok>=1` within 70 seconds, plus at least one script log line in the station log. HTTP 200 alone is not sufficient — `CustomApiHandler` returns 200 even when no plugin matches, so the DB row and the log line are the acceptance signals.

- [ ] **Step 6: Exercise the direct station port (record only)**

```bash
curl -s -o /dev/null -w 'direct_station=%{http_code}\n' -X PUT \
  "http://localhost:9040/api?apiCode=CUSTOM_API" \
  -H 'Content-Type: application/json' -H 'StationCode: 1' \
  --data-binary "@$E2E_WIN/payloads/station-invoke.json"
```

Expected: 200. If 401, record "direct port requires auth; gateway path passed" — this is informational, not gating.

- [x] **Step 7: Record evidence**

```bash
{
  echo "# Task 6 — station plugin"
  echo "SREV_ID=$SREV_ID"
  echo "$SVALID"
  echo "$SENABLE"
  echo "custom_ok=$LOG_OK"
  echo "## station log line"
  grep "e2e station custom api" "$E2E/logs/station.log" | tail -5
  echo "## execution row"
  cd "$E2E/tools"
  DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
  OUT=$(cygpath -w "$E2E/tools/out")
  "$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
    "SELECT plugin_key, revision_id, extension_type, success, duration_ms FROM p_script_plugin_execution_log WHERE plugin_key='e2e-station-plugin' ORDER BY created_at DESC LIMIT 5"
} > "$E2E/results/task6.md" 2>&1
cat "$E2E/results/task6.md"
```

---

## Task 7: Governance — denials, config locking, test-run, disable/revision/rollback, metadata

**Files:**
- Create: `$E2E/results/task7.md`

**Interfaces:**
- Consumes: `$TOKEN` (admin/superAdmin), `e2e-wes-plugin`, direct wes 9010, payloads from Task 5.
- Produces: PL002001/PL002002/PL002003 denial evidence; denial audit rows; config lockVersion 0→1→stale error; test-run success and method failure; disable → rev2 → enable → rollback to rev1; metadata update; audit action coverage.

- [x] **Step 1: Prove the three denial codes on the direct wes port**

```bash
DENY1=$(curl -s -X POST http://localhost:9010/plugin-platform/scriptPlugin/createDraft \
  -H 'username: phase5-norole' -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/wes-create.json")
printf '%s' "$DENY1" | node "$E2E_WIN/tools/jsonq.js" status
printf '%s' "$DENY1" | node "$E2E_WIN/tools/jsonq.js" description

DENY2=$(curl -s -X POST http://localhost:9010/plugin-platform/scriptPlugin/e2e-wes-plugin/enable \
  -H 'username: phase5-norole')
printf '%s' "$DENY2" | node "$E2E_WIN/tools/jsonq.js" status
printf '%s' "$DENY2" | node "$E2E_WIN/tools/jsonq.js" description

DENY3=$(curl -s http://localhost:9010/plugin-platform/scriptPlugin/list)
printf '%s' "$DENY3" | node "$E2E_WIN/tools/jsonq.js" status
printf '%s' "$DENY3" | node "$E2E_WIN/tools/jsonq.js" description
```

Expected: `PL002001` / `PL002002` / `PL002003`, each with a description naming the missing role or authentication. The error envelope field is `status` (not HTTP status); HTTP is 200.

- [x] **Step 2: Verify the denial audit rows**

```bash
cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
"$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
  "SELECT plugin_key, action, operator, success, detail FROM p_script_plugin_operation_audit WHERE operator='phase5-norole' ORDER BY id"
```

Expected: a `CREATE_DRAFT` row and an `ENABLE` row with `success=0` and `detail` starting `missing role `.

- [x] **Step 3: Read config defaults**

```bash
CONFIG=$(curl -s -H "Authorization: $TOKEN" \
  http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/config)
printf '%s' "$CONFIG" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$CONFIG" | node "$E2E_WIN/tools/jsonq.js" data.values
printf '%s' "$CONFIG" | node "$E2E_WIN/tools/jsonq.js" data.lockVersion
node -e 'const c=JSON.parse(process.argv[1]); const s=c.data.schema; console.log("schema_fields="+s.length+" greeting_default="+s.find(f=>f.field==="greeting").defaultValue); if(s.length!==3) process.exit(1);' "$CONFIG"
```

Expected: `0`, values with `greeting=hello`, `lockVersion` empty (null), `schema_fields=3`, `greeting_default=hello`.

- [x] **Step 4: Save, update, and prove stale-lock rejection**

```bash
SAVE1=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/config \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/config-save-default.json")
printf '%s' "$SAVE1" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$SAVE1" | node "$E2E_WIN/tools/jsonq.js" data.lockVersion

SAVE2=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/config \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/config-save-v0.json")
printf '%s' "$SAVE2" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$SAVE2" | node "$E2E_WIN/tools/jsonq.js" data.lockVersion

SAVE3=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/config \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/config-save-stale-v0.json")
printf '%s' "$SAVE3" | node "$E2E_WIN/tools/jsonq.js" status
printf '%s' "$SAVE3" | node "$E2E_WIN/tools/jsonq.js" description
```

Expected: first save `code=0`, `lockVersion=0`; second `code=0`, `lockVersion=1`; stale save has a non-zero `status` and a description containing `config was modified by another user; current lockVersion is 1`. The stale values must not be persisted:

```bash
cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
"$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
  "SELECT c.config_json, c.lock_version FROM p_script_plugin_config c JOIN p_script_plugin p ON p.id=c.plugin_id WHERE p.plugin_key='e2e-wes-plugin'"
```

Expected: `config_json` contains `"greeting": "hi"`, not `"stale"`; `lock_version=1`.

- [x] **Step 5: Test-run success and unknown-method failure**

```bash
TRUN=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/test-run \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/test-run.json")
printf '%s' "$TRUN" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$TRUN" | node "$E2E_WIN/tools/jsonq.js" data.success
printf '%s' "$TRUN" | node "$E2E_WIN/tools/jsonq.js" data.durationMs
printf '%s' "$TRUN" | node "$E2E_WIN/tools/jsonq.js" data.result
printf '%s' "$TRUN" | grep -q '"status":"ok"' && echo "TEST_RUN_STATUS_OK"

TBAD=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/test-run \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/test-run-bad-method.json")
printf '%s' "$TBAD" | node "$E2E_WIN/tools/jsonq.js" data.success
printf '%s' "$TBAD" | node "$E2E_WIN/tools/jsonq.js" data.error
```

Expected: `data.success=true`, numeric duration, `TEST_RUN_STATUS_OK`; the bad-method run is rejected before execution with a non-zero envelope `status` and `description=the current revision does not declare this extension` (test-run only executes a declared type+script+method, per the Phase 3A design; it does not return `data.success=false`).

- [x] **Step 6: Disable, save revision 2, enable, and prove v2**

```bash
DISABLE=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/disable \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  -d '{"reason":"phase5 disable"}')
printf '%s' "$DISABLE" | node "$E2E_WIN/tools/jsonq.js" data.status

for i in $(seq 1 15); do
  code=$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: $TOKEN" http://localhost:8090/plugin-platform/ext/e2e-wes-plugin/status)
  [ "$code" = "404" ] && break
  sleep 1
done
echo "disabled_http=$code"

REV2=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/revision \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/wes-revision-2.json")
printf '%s' "$REV2" | node "$E2E_WIN/tools/jsonq.js" data.revisionNo
REV2_ID=$(printf '%s' "$REV2" | node "$E2E_WIN/tools/jsonq.js" data.id)
export REV2_ID
printf '%s' "$REV2_ID" > "$E2E/.rev2_id"
echo "REV2_ID=$REV2_ID"

VALID2=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/validate \
  -H "Authorization: $TOKEN")
printf '%s' "$VALID2" | node "$E2E_WIN/tools/jsonq.js" data.passed

ENABLE2=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/enable \
  -H "Authorization: $TOKEN")
printf '%s' "$ENABLE2" | node "$E2E_WIN/tools/jsonq.js" data.status
printf '%s' "$ENABLE2" | node "$E2E_WIN/tools/jsonq.js" data.currentRevisionId

for i in $(seq 1 20); do
  R=$(curl -s -H "Authorization: $TOKEN" http://localhost:8090/plugin-platform/ext/e2e-wes-plugin/status \
    | node "$E2E_WIN/tools/jsonq.js" data.revision)
  [ "$R" = "v2" ] && break
  sleep 1
done
echo "revision=$R"
```

Expected: `DISABLED`, `disabled_http=404`, revisionNo `2`, `passed=true`, `ENABLED`, `REV2_ID`, `revision=v2`.

- [x] **Step 7: Roll back to revision 1 and prove v1**

```bash
REV1_ID=$(cat "$E2E/.rev1_id")
export REV1_ID
ROLLBACK=$(curl -s -X POST "http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/rollback/$REV1_ID" \
  -H "Authorization: $TOKEN")
printf '%s' "$ROLLBACK" | node "$E2E_WIN/tools/jsonq.js" data.status
printf '%s' "$ROLLBACK" | node "$E2E_WIN/tools/jsonq.js" data.currentRevisionId

for i in $(seq 1 20); do
  R=$(curl -s -H "Authorization: $TOKEN" http://localhost:8090/plugin-platform/ext/e2e-wes-plugin/status \
    | node "$E2E_WIN/tools/jsonq.js" data.revision)
  [ "$R" = "v1" ] && break
  sleep 1
done
echo "revision=$R"
```

Expected: `ENABLED`, `REV1_ID`, `revision=v1`.

- [x] **Step 8: Update metadata**

```bash
META=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/metadata \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  -d '{"name":"E2E WES Plugin Renamed","description":"Phase 5 metadata update","applyModule":""}')
printf '%s' "$META" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$META" | node "$E2E_WIN/tools/jsonq.js" data.name
```

Expected: `0`, `E2E WES Plugin Renamed`.

- [x] **Step 9: Verify the audit API and global audit**

```bash
AUDIT=$(curl -s -H "Authorization: $TOKEN" \
  "http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/audit?page=1&pageSize=100")
printf '%s' "$AUDIT" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$AUDIT" | node "$E2E_WIN/tools/jsonq.js" data.total
for action in CREATE_DRAFT SAVE_REVISION VALIDATE ENABLE DISABLE ROLLBACK CONFIG_SAVE TEST_RUN METADATA_UPDATE; do
  printf '%s' "$AUDIT" | grep -q "\"action\":\"$action\"" && echo "AUDIT_HAS_$action" || echo "AUDIT_MISSING_$action"
done

GLOBAL=$(curl -s -H "Authorization: $TOKEN" \
  "http://localhost:8090/plugin-platform/scriptPlugin/audit?page=1&pageSize=100")
printf '%s' "$GLOBAL" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$GLOBAL" | node "$E2E_WIN/tools/jsonq.js" data.total
```

Expected: `data.total>=9`; all nine `AUDIT_HAS_*` lines; global audit `code=0` with `data.total>=9`.

- [x] **Step 10: Record evidence**

```bash
{
  echo "# Task 7 — governance"
  echo "## denials"
  echo "$DENY1"
  echo "$DENY2"
  echo "$DENY3"
  echo "## config saves"
  echo "$SAVE1"
  echo "$SAVE2"
  echo "$SAVE3"
  echo "## test-run"
  echo "$TRUN"
  echo "$TBAD"
  echo "## disable/rev2/enable"
  echo "$DISABLE"
  echo "$REV2"
  echo "$ENABLE2"
  echo "revision=$R"
  echo "## rollback"
  echo "$ROLLBACK"
  echo "## metadata"
  echo "$META"
  echo "## audit"
  echo "$AUDIT"
  echo "$GLOBAL"
} > "$E2E/results/task7.md" 2>&1
cat "$E2E/results/task7.md"
```

---

## Task 8: AI draft — real model generation, then validate and deploy the output

**Files:**
- Create: `$E2E/results/task8.md`, `$E2E/logs/ai-stream.txt`

**Interfaces:**
- Consumes: `$TOKEN`, `e2e-ai-plugin` (DRAFT), DeepSeek key from the environment (never printed), `$E2E/payloads/ai-*.json`.
- Produces: an SSE terminal `result` or `error`; on success, an AI-origin revision and a generation row `SUCCEEDED`; validation/enable/disable of the AI output.

- [x] **Step 1: Create the AI draft plugin**

```bash
ACREATE=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/createDraft \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  --data-binary "@$E2E_WIN/payloads/ai-create.json")
printf '%s' "$ACREATE" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$ACREATE" | node "$E2E_WIN/tools/jsonq.js" data.pluginKey
printf '%s' "$ACREATE" | node "$E2E_WIN/tools/jsonq.js" data.status
```

Expected: `0`, `e2e-ai-plugin`, `DRAFT`.

- [x] **Step 2: Stream one generation**

```bash
curl -sN --max-time 220 -X POST \
  http://localhost:8090/plugin-platform/ai/draft/stream \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  -H 'Accept: text/event-stream' \
  --data-binary "@$E2E_WIN/payloads/ai-stream-request.json" \
  > "$E2E/logs/ai-stream.txt"
echo "stream_exit=$?"
grep -c '^event:stage' "$E2E/logs/ai-stream.txt" || true
grep -c '^event:chunk' "$E2E/logs/ai-stream.txt" || true
grep -c '^event:result' "$E2E/logs/ai-stream.txt" || true
grep -c '^event:error' "$E2E/logs/ai-stream.txt" || true
grep '^event:' "$E2E/logs/ai-stream.txt" | tail -5
```

Expected: at least one `stage`, many `chunk`s, and exactly one terminal `result` or `error`. Inspect the terminal payload:

```bash
grep -A1 '^event:result' "$E2E/logs/ai-stream.txt" | tail -1 | sed 's/^data://' \
  | node "$E2E_WIN/tools/jsonq.js" result.revisionId
grep -A1 '^event:result' "$E2E/logs/ai-stream.txt" | tail -1 | sed 's/^data://' \
  | node "$E2E_WIN/tools/jsonq.js" result.attempts
grep -A1 '^event:result' "$E2E/logs/ai-stream.txt" | tail -1 | sed 's/^data://' \
  | node "$E2E_WIN/tools/jsonq.js" result.model
grep -A1 '^event:result' "$E2E/logs/ai-stream.txt" | tail -1 | sed 's/^data://' \
  | node "$E2E_WIN/tools/jsonq.js" result.promptVersion
```

Expected on success: numeric `revisionId`, `attempts` 1–3, model `GLM-4-air`, prompt version `plugin-draft-v1`. If the terminal event is `error`, read `data.text` and `data.code`, record it, and retry once:

```bash
# Retry once with a shorter requirement.
node -e '
const fs=require("node:fs");
const p=process.argv[1];
const r=JSON.parse(fs.readFileSync(p,"utf8"));
r.requirement="wes script plugin e2e-ai-plugin. Declare exactly one CRON extension: script ai-cron.groovy, method run, cron 0 0/1 * * * *, returns ai-tick. No other extensions. Standard bundle JSON.";
fs.writeFileSync(p, JSON.stringify(r,null,2));
' "$E2E_WIN/payloads/ai-stream-request.json"
curl -sN --max-time 220 -X POST http://localhost:8090/plugin-platform/ai/draft/stream \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' -H 'Accept: text/event-stream' \
  --data-binary "@$E2E_WIN/payloads/ai-stream-request.json" > "$E2E/logs/ai-stream-retry.txt"
grep -c '^event:result' "$E2E/logs/ai-stream-retry.txt" || true
grep -c '^event:error' "$E2E/logs/ai-stream-retry.txt" || true
```

If the retry also ends in `error`, mark AI acceptance BLOCKED by model/provider output and do not claim it; continue with Task 9 (the rest of the platform is independent).

**Result (2026-10-28):** both attempts ended with `event:error`, `code=PL001006`, `401 Unauthorized from POST https://open.bigmodel.cn/api/paas/v4/chat/completions`, and zero chunks. AI acceptance is BLOCKED by the ZhipuAI credential in the live wes Nacos config; the pipeline itself emitted stages and exactly one terminal error, and persisted FAILED generation rows.

**DeepSeek rerun (2026-10-28):** after the provider was switched to DeepSeek, the same request produced `stage=3`, `chunk=177`, `result=1`, `error=0`; `result.revisionId=903397123045859328`, `attempts=1`, `model=deepseek-chat`, `promptVersion=plugin-draft-v1`. The two Zhipu FAILED rows remain as history; the newest generation row is SUCCEEDED.

- [x] **Step 3: Verify the AI revision and generation rows**

```bash
cd "$E2E/tools"
DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
OUT=$(cygpath -w "$E2E/tools/out")
"$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
  "SELECT p.plugin_key, p.status, r.revision_no, r.origin, r.ai_model, r.prompt_version FROM p_script_plugin p JOIN p_script_plugin_revision r ON r.plugin_id=p.id WHERE p.plugin_key='e2e-ai-plugin' ORDER BY r.revision_no" \
  "SELECT plugin_key, user_name, model, prompt_version, status, attempts, duration_ms, error_message FROM p_script_plugin_ai_generation WHERE plugin_key='e2e-ai-plugin' ORDER BY id"
```

Expected on success: one revision row with `origin=AI`, `ai_model=deepseek-chat`, `prompt_version=plugin-draft-v1`, and plugin `status=DRAFT` (AI never enables). One generation row `status=SUCCEEDED`; a retry may leave one `FAILED` row before it — record both. (The original expectation said `GLM-4-air`; the provider migration changed the model id.)

- [x] **Step 4: Validate and deploy the AI output (manual step)**

```bash
AVALID=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-ai-plugin/validate \
  -H "Authorization: $TOKEN")
printf '%s' "$AVALID" | node "$E2E_WIN/tools/jsonq.js" data.passed
printf '%s' "$AVALID" | node "$E2E_WIN/tools/jsonq.js" data.failures

AENABLE=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-ai-plugin/enable \
  -H "Authorization: $TOKEN")
printf '%s' "$AENABLE" | node "$E2E_WIN/tools/jsonq.js" data.status

ADISABLE=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-ai-plugin/disable \
  -H "Authorization: $TOKEN" -H 'Content-Type: application/json' \
  -d '{"reason":"phase5 ai acceptance complete"}')
printf '%s' "$ADISABLE" | node "$E2E_WIN/tools/jsonq.js" data.status
```

Expected: `passed=true`, empty failures, `ENABLED`, then `DISABLED`. If validation fails on the model output, record the failure text and mark AI deployment FAILED (the generation itself may still have succeeded).

**BLOCKED (2026-10-28, ZhipuAI attempt):** no revision exists because generation failed. Validate returned `status=CM010001`, `description=plugin has no revision to validate: e2e-ai-plugin`; enable/disable were not attempted.

**Result after DeepSeek rerun (2026-10-28):** `passed=true`, `failures=[]`, `validate_code=0`; `enable_status=ENABLED`; `disable_status=DISABLED`. The plugin is left DISABLED.

- [x] **Step 5: Check the generation history API**

```bash
AGEN=$(curl -s -H "Authorization: $TOKEN" \
  http://localhost:8090/plugin-platform/ai/generations/e2e-ai-plugin)
printf '%s' "$AGEN" | node "$E2E_WIN/tools/jsonq.js" code
printf '%s' "$AGEN" | node "$E2E_WIN/tools/jsonq.js" data.0.status
printf '%s' "$AGEN" | node "$E2E_WIN/tools/jsonq.js" data.0.model
printf '%s' "$AGEN" | node "$E2E_WIN/tools/jsonq.js" data.0.promptVersion
```

Expected: `0`, latest status `SUCCEEDED`, `GLM-4-air`, `plugin-draft-v1`.

**Observed (2026-10-28, ZhipuAI attempt):** `code=0`, `data.0.status=FAILED`, `GLM-4-air`, `plugin-draft-v1` (no SUCCEEDED generation exists because of the provider 401). The API itself works.

**Observed after DeepSeek rerun (2026-10-28):** `code=0`, `data.0.status=SUCCEEDED`, `model=deepseek-chat`, `promptVersion=plugin-draft-v1`, `attempts=1`.

- [x] **Step 6: Corpus refresh (record only, optional)**

```bash
CORPUS=$(curl -s -X POST http://localhost:8090/plugin-platform/ai/corpus/refresh \
  -H "Authorization: $TOKEN" -w '\n%{http_code}')
echo "$CORPUS" | tail -1
echo "$CORPUS" | sed '$d' | head -c 500
echo
```

Expected: either `code=0` with a count or a recorded error (Pinecone may be unconfigured). This is not gating; write the result verbatim.

**Observed (2026-10-28):** HTTP 200 with envelope `status=CM010001`, `description=401 - {"error":{"code":"1000","message":"身份验证失败。"}}` — Pinecone rejected the dummy `PINECONE_API_KEY=not-configured`. Not gating.

- [x] **Step 7: Record evidence**

```bash
{
  echo "# Task 8 — AI draft"
  echo "## terminal events"
  grep '^event:' "$E2E/logs/ai-stream.txt" | tail -5
  echo "## result payload"
  grep -A1 '^event:result' "$E2E/logs/ai-stream.txt" | tail -1 | head -c 1500
  echo
  echo "## retry (if used)"
  ls -l "$E2E/logs/ai-stream-retry.txt" 2>/dev/null || echo "no retry"
  echo "## DB rows"
  cd "$E2E/tools"
  DRIVER=$(cygpath -w "$(find ~/.gradle/caches -name 'mysql-connector-j-8.3.0.jar' | head -1)")
  OUT=$(cygpath -w "$E2E/tools/out")
  "$JAVA17" -cp ".;$DRIVER;$OUT" DbQuery \
    "SELECT p.plugin_key, p.status, r.revision_no, r.origin, r.ai_model, r.prompt_version FROM p_script_plugin p JOIN p_script_plugin_revision r ON r.plugin_id=p.id WHERE p.plugin_key='e2e-ai-plugin'" \
    "SELECT status, attempts, model, prompt_version, error_message FROM p_script_plugin_ai_generation WHERE plugin_key='e2e-ai-plugin'"
  echo "## validate/enable/disable"
  echo "$AVALID"
  echo "$AENABLE"
  echo "$ADISABLE"
  echo "## corpus"
  echo "$CORPUS"
} > "$E2E/results/task8.md" 2>&1
cat "$E2E/results/task8.md"
```

---

## Task 9: Browser acceptance — 12 screenshots and a contact sheet

**Files:**
- Create: `$E2E/tools/cdp-capture.js`
- Create: `$E2E/shots/shot01…shot12*.png`, `$E2E/contact-sheet.png`, `$E2E/results/task9.md`

**Interfaces:**
- Consumes: client dev server on 3000 with the `/gw` proxy (check `grep -n "\"/gw\"" client/build/webpack.config.dev.js`), gateway 8090, `$OPENWES_ADMIN_PASSWORD`, the plugins from Tasks 5/6/8.
- Produces: 12 PNGs, a 3×4 contact sheet read visually, and console/network error lists. The browser login invalidates earlier CLI tokens — do not reuse `$TOKEN` after this task.

- [x] **Step 1: Confirm the client dev server**

```bash
curl -s -o /dev/null -w 'client=%{http_code}\n' http://localhost:3000/
grep -n '"/gw"' client/build/webpack.config.dev.js
```

Expected: `client=200` and a `/gw` proxy match. If the server is down, start it:

```bash
cd /d/open-wes/client
nohup npm run dev > "$E2E/logs/client.log" 2>&1 &
echo $! > "$E2E/logs/client.pid"
```

Wait up to 5 minutes for `client=200`. If the proxy line is missing from the (gitignored) webpack config, stop — the workbench API cannot be reached; report it.

- [x] **Step 2: Write the CDP capture script**

```js
"use strict";
const { spawn } = require("node:child_process");
const fs = require("node:fs");
const path = require("node:path");

const CHROME = "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe";
const PROFILE = path.join(process.env.E2E_WIN, "chrome-profile");
const SHOTS = path.join(process.env.E2E_WIN, "shots");
const ORIGIN = "http://localhost:3000";
const PASSWORD = process.env.OPENWES_ADMIN_PASSWORD;
if (!PASSWORD) throw new Error("OPENWES_ADMIN_PASSWORD is required");
fs.mkdirSync(SHOTS, { recursive: true });
fs.rmSync(PROFILE, { recursive: true, force: true });
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));

const consoleErrors = [];
const networkFailures = [];

async function main() {
  const chrome = spawn(CHROME, [
    "--headless=new", "--disable-gpu", "--no-first-run", "--no-default-browser-check",
    "--remote-debugging-port=9222", `--user-data-dir=${PROFILE}`,
    "--window-size=1600,1000", "about:blank",
  ], { stdio: "ignore" });

  let version;
  for (let i = 0; i < 60; i++) {
    try { version = await (await fetch("http://127.0.0.1:9222/json/version")).json(); break; }
    catch { await sleep(500); }
  }
  if (!version) throw new Error("Chrome CDP did not start");

  const target = await (await fetch("http://127.0.0.1:9222/json/new?" + encodeURIComponent(ORIGIN + "/login"), { method: "PUT" })).json();
  const ws = new WebSocket(target.webSocketDebuggerUrl);
  await new Promise((resolve, reject) => { ws.onopen = resolve; ws.onerror = reject; });

  let nextId = 0;
  const pending = new Map();
  ws.onmessage = (event) => {
    const msg = JSON.parse(event.data);
    if (msg.id && pending.has(msg.id)) {
      pending.get(msg.id)(msg);
      pending.delete(msg.id);
      return;
    }
    if (msg.method === "Runtime.exceptionThrown") {
      consoleErrors.push("uncaught: " + JSON.stringify(msg.params.exceptionDetails.text));
    }
    if (msg.method === "Runtime.consoleAPICalled" && msg.params.type === "error") {
      consoleErrors.push("console.error: " + msg.params.args.map((a) => a.value || a.description).join(" ").slice(0, 300));
    }
    if (msg.method === "Network.loadingFailed" && !msg.params.canceled) {
      networkFailures.push(msg.params.errorText + " " + (msg.params.requestId || ""));
    }
    if (msg.method === "Network.responseReceived" && msg.params.response.url.includes("plugin-platform") && msg.params.response.status >= 400) {
      networkFailures.push("HTTP " + msg.params.response.status + " " + msg.params.response.url);
    }
  };
  const send = (method, params = {}) => new Promise((resolve) => {
    const id = ++nextId;
    pending.set(id, resolve);
    ws.send(JSON.stringify({ id, method, params }));
  });
  const evaluate = async (expression) => {
    const response = await send("Runtime.evaluate", { expression, awaitPromise: true, returnByValue: true });
    if (response.result && response.result.exceptionDetails) throw new Error(JSON.stringify(response.result.exceptionDetails));
    return response.result && response.result.result ? response.result.result.value : undefined;
  };
  const waitFor = async (expression, timeoutMs, label) => {
    const deadline = Date.now() + timeoutMs;
    while (Date.now() < deadline) {
      if (await evaluate(expression).catch(() => false)) return true;
      await sleep(500);
    }
    throw new Error("timeout waiting for " + label);
  };
  const navigate = async (url) => {
    await send("Page.navigate", { url });
    await sleep(2500);
  };
  const shot = async (name) => {
    await sleep(1500);
    const response = await send("Page.captureScreenshot", { format: "png" });
    fs.writeFileSync(path.join(SHOTS, name), Buffer.from(response.result.data, "base64"));
    const text = await evaluate("document.body ? document.body.innerText.slice(0, 200) : ''").catch(() => "");
    console.log("SHOT " + name + " :: " + text.replace(/\s+/g, " ").slice(0, 160));
  };
  const clickTab = async (key) => {
    const clicked = await evaluate(`(function(){
      const tab = document.querySelector('.ant-tabs-tab[data-node-key="${key}"]');
      if (!tab) return false;
      tab.dispatchEvent(new MouseEvent('click', {bubbles:true, cancelable:true, view:window}));
      return true;
    })()`);
    if (!clicked) throw new Error("tab not found: " + key);
    await sleep(1500);
  };

  await send("Page.enable");
  await send("Runtime.enable");
  await send("Network.enable");
  await send("Page.navigate", { url: ORIGIN + "/login" });
  await waitFor("!!document.querySelector('input[type=password]')", 240000, "login form");
  const filled = await evaluate(`(function(){
    const setValue = (el, value) => {
      const setter = Object.getOwnPropertyDescriptor(window.HTMLInputElement.prototype, 'value').set;
      setter.call(el, value);
      el.dispatchEvent(new Event('input', {bubbles:true}));
      el.dispatchEvent(new Event('change', {bubbles:true}));
    };
    const pwd = document.querySelector('input[type=password]');
    const form = pwd && pwd.closest('form');
    const user = form ? form.querySelector('input:not([type=password])') : null;
    if (!user || !pwd) return 'inputs-not-found';
    setValue(user, 'admin');
    setValue(pwd, ${JSON.stringify(PASSWORD)});
    const button = form.querySelector('button[type=submit]') ||
      Array.from(document.querySelectorAll('button')).find((b) => /登录|Sign in|Login/i.test(b.innerText));
    if (!button) return 'button-not-found';
    button.click();
    return 'clicked';
  })()`);
  console.log("login_fill=" + filled);
  await waitFor("!!localStorage.getItem('ws_token')", 60000, "ws_token");
  await waitFor("location.pathname !== '/login'", 60000, "post-login route");
  console.log("login_ok path=" + (await evaluate("location.pathname")));

  await navigate(ORIGIN + "/plugin-platform/plugins");
  await waitFor("document.body.innerText.includes('E2E WES Plugin') || document.body.innerText.includes('e2e-wes-plugin')", 60000, "plugins list rows");
  await shot("shot01-plugins-list.png");

  await navigate(ORIGIN + "/plugin-platform/plugin-workbench?pluginKey=e2e-wes-plugin");
  await waitFor("!!document.querySelector('.ant-tabs-tab[data-node-key=\"overview\"]')", 90000, "workbench tabs");
  await shot("shot02-wes-overview.png");
  for (const [key, file] of [
    ["scripts", "shot03-wes-scripts.png"],
    ["revisions", "shot04-wes-revisions.png"],
    ["config", "shot05-wes-config.png"],
    ["testRun", "shot06-wes-testrun.png"],
    ["executions", "shot07-wes-executions.png"],
    ["audit", "shot08-wes-audit.png"],
  ]) {
    await clickTab(key);
    await shot(file);
  }

  await navigate(ORIGIN + "/plugin-platform/audit");
  await sleep(3000);
  await shot("shot09-global-audit.png");

  await navigate(ORIGIN + "/plugin-platform/plugin-workbench?pluginKey=e2e-ai-plugin");
  await waitFor("!!document.querySelector('.ant-tabs-tab[data-node-key=\"aiHistory\"]')", 90000, "ai history tab");
  await clickTab("aiHistory");
  await shot("shot10-ai-history.png");

  await navigate(ORIGIN + "/plugin-platform/plugin-workbench?mode=new");
  await sleep(3000);
  await shot("shot11-new-wizard.png");

  await navigate(ORIGIN + "/plugin-platform/plugin-workbench?pluginKey=e2e-station-plugin");
  await waitFor("!!document.querySelector('.ant-tabs-tab[data-node-key=\"overview\"]')", 90000, "station overview tab");
  await shot("shot12-station-overview.png");

  const summary = {
    shots: fs.readdirSync(SHOTS).filter((f) => f.endsWith(".png")).sort(),
    consoleErrors,
    networkFailures,
    path: await evaluate("location.pathname"),
  };
  fs.writeFileSync(path.join(process.env.E2E_WIN, "results", "cdp-summary.json"), JSON.stringify(summary, null, 2));
  console.log("SUMMARY " + JSON.stringify(summary));
  ws.close();
  chrome.kill();
  if (summary.shots.length !== 12) throw new Error("expected 12 screenshots, got " + summary.shots.length);
}

main().catch((error) => { console.error("FAILED: " + error.message); process.exit(1); });
```

- [x] **Step 3: Run the capture**

```bash
cd /d/open-wes
node "$E2E_WIN/tools/cdp-capture.js" 2>&1 | tee "$E2E/logs/cdp-capture.log"
```

Expected: `login_ok`, 12 `SHOT` lines, and a `SUMMARY` with 12 files. The first page load can take up to ~3 minutes if the client is recompiling; the login wait allows 4 minutes.

- [x] **Step 4: Inspect the console and network summary**

```bash
node -e '
const s=require(process.argv[1]);
console.log("shots="+s.shots.length);
console.log("consoleErrors="+s.consoleErrors.length);
s.consoleErrors.slice(0,20).forEach((e)=>console.log("  CONSOLE "+e));
console.log("networkFailures="+s.networkFailures.length);
s.networkFailures.slice(0,20).forEach((e)=>console.log("  NETWORK "+e));
if(s.shots.length!==12) process.exit(1);
' "$E2E_WIN/results/cdp-summary.json"
```

Expected: 12 shots. Any console error or `plugin-platform` network failure must be triaged: fix a real UI defect (with a test) or record it as a known issue with the exact message. AMIS may emit benign warnings; classify them explicitly in the evidence.

- [x] **Step 5: Build and read the contact sheet**

```bash
cd "$E2E/shots"
ffmpeg -y -pattern_type glob -i "shot*.png" \
  -filter_complex "scale=800:-1,tile=3x4:padding=8:margin=8" \
  -frames:v 1 "$E2E_WIN/contact-sheet.png"
ls -l "$E2E/contact-sheet.png"
```

Then **read** `$E2E/contact-sheet.png` with the Read tool. Verify each of the 12 panels shows the expected surface: plugin list rows; WES overview with status ENABLED; scripts with 5 script names; revisions with 2 revisions; config fields; test-run form; executions rows; audit rows; global audit rows; AI history; new-plugin wizard step 1; station overview. Record any blank, error, or login panel as a failure with the panel position.

- [x] **Step 6: Record evidence**

```bash
{
  echo "# Task 9 — browser"
  echo "## capture log"
  cat "$E2E/logs/cdp-capture.log"
  echo "## summary"
  cat "$E2E/results/cdp-summary.json"
  echo "## contact sheet"
  ls -l "$E2E/contact-sheet.png"
  echo "## visual read"
  echo "Describe each of the 12 panels and any defect found."
} > "$E2E/results/task9.md"
cat "$E2E/results/task9.md"
```

**Result (2026-10-28):** 12/12 screenshots show the expected surfaces and 0 network failures. One real workbench defect was found and fixed: ScriptFileEditor did not show an existing revision's script files (commit 4ec5d21b), and the re-capture shows E2eCron.groovy in the editor. Two uncaught AMIS Table-teardown errors while leaving the plugin list are a pre-existing known issue; the dev-server overlay they raise was dismissed for the screenshots, and the raw overlay-covered sheet is kept as `contact-sheet-with-overlay.png`. Details in `task9.md`.

---

## Task 10: Archive, dematerialization, results, cleanup, merge handoff

**Files:**
- Create: `$E2E/results/RESULTS.md`
- Create: `$E2E/results/task10.md`

**Interfaces:**
- Consumes: everything above; a fresh gateway login (Task 9 invalidated the old token).
- Produces: archived WES plugin with a 404 endpoint; a per-check PASS/FAIL/BLOCKED table; stopped servers; merge options presented without merging.

- [x] **Step 1: Re-login and archive the WES plugin**

```bash
LOGIN=$(curl -s -X POST http://localhost:8090/user/api/auth/signin \
  -H 'Content-Type: application/json' \
  -d "{\"username\":\"admin\",\"password\":\"$OPENWES_ADMIN_PASSWORD\"}")
TOKEN=$(printf '%s' "$LOGIN" | node "$E2E_WIN/tools/jsonq.js" token)
export TOKEN
umask 077
printf '%s' "$TOKEN" > "$E2E/.token"

ARCHIVE=$(curl -s -X POST http://localhost:8090/plugin-platform/scriptPlugin/e2e-wes-plugin/archive \
  -H "Authorization: $TOKEN")
printf '%s' "$ARCHIVE" | node "$E2E_WIN/tools/jsonq.js" data.status
```

Expected: `ARCHIVED`.

- [x] **Step 2: Verify dematerialization**

```bash
for i in $(seq 1 20); do
  code=$(curl -s -o /dev/null -w '%{http_code}' -H "Authorization: $TOKEN" \
    http://localhost:8090/plugin-platform/ext/e2e-wes-plugin/status)
  [ "$code" = "404" ] && break
  sleep 1
done
echo "archived_http=$code"

curl -s -H "Authorization: $TOKEN" \
  http://localhost:8090/plugin-platform/scriptPlugin/detail/e2e-wes-plugin \
  | node "$E2E_WIN/tools/jsonq.js" data.status
```

Expected: `archived_http=404`, detail status `ARCHIVED`. The station and AI plugins remain unaffected:

```bash
curl -s -H "Authorization: $TOKEN" \
  http://localhost:8090/plugin-platform/scriptPlugin/detail/e2e-station-plugin \
  | node "$E2E_WIN/tools/jsonq.js" data.status
curl -s -H "Authorization: $TOKEN" \
  http://localhost:8090/plugin-platform/scriptPlugin/detail/e2e-ai-plugin \
  | node "$E2E_WIN/tools/jsonq.js" data.status
```

Expected: `ENABLED` and `DISABLED`.

**Result (2026-10-28):** `archived_http=404`, wes detail `ARCHIVED`, station `ENABLED`, AI `DRAFT` at the time of Task 10 (generation was still blocked by the ZhipuAI credential, so the AI plugin had never left DRAFT). After the DeepSeek rerun of Task 8 Steps 2–5, the AI plugin was ENABLED and then DISABLED, so it is now `DISABLED`, matching the plan's original expectation.

- [x] **Step 3: Write the results table**

Fill every row with PASS/FAIL/BLOCKED and the evidence path. Do not mark a row PASS without an observed output.

```markdown
# Phase 5 Acceptance Results — 2026-10-27

| # | Check | Result | Evidence |
|---|---|---|---|
| 1 | Flaky validation test fixed, 20/20 | | task1.md |
| 2 | Full build + three jars | | task2.md |
| 3 | Liquibase 20261022 + 20261023 applied | | task3.md |
| 4 | Nacos gateway route approved and applied | | task4.md |
| 5 | WES HTTP endpoint /status | | task5.md |
| 6 | Spring @Autowired inside script | | task5.md |
| 7 | Cron execution | | task5.md |
| 8 | REQUEST_SUPPLY hook on ORDER_OUTBOUND_CREATE | | task5.md |
| 9 | Wave action groups two orders into one wave | | task5.md |
| 10 | Executions page + stats JPQL | | task5.md |
| 11 | Station handshake validation | | task6.md |
| 12 | Station CUSTOM_API invocation + log | | task6.md |
| 13 | PL002001/PL002002/PL002003 denials + audit | | task7.md |
| 14 | Config defaults, lockVersion 0→1, stale rejection | | task7.md |
| 15 | Test-run success + unknown-method failure | | task7.md |
| 16 | Disable, revision 2, enable, rollback to v1 | | task7.md |
| 17 | Metadata update + audit actions | | task7.md |
| 18 | AI SSE generation + AI revision | | task8.md |
| 19 | AI output validates and deploys | | task8.md |
| 20 | Workbench 12-panel browser acceptance | | task9.md |
| 21 | Archive + dematerialization | | task10.md |
```

Below the table, list every item not exercised and why (for example "Pinecone corpus refresh: recorded, not gating"; "AI generation blocked by provider output"; "gateway update not approved"). Include the exact failing output for every FAIL.

- [x] **Step 4: Final repository state**

```bash
cd /d/open-wes
git status --short
git log --oneline -3
```

Expected: no modified tracked files; only the untracked plan docs and the pre-existing `constants/` warning; the top commit is the Task 1 test fix. If any other tracked file changed during acceptance, inspect it before proceeding; do not commit it silently.

- [x] **Step 5: Stop the servers started by this plan**

```bash
for name in wes station gateway; do
  pidfile="$E2E/logs/$name.pid"
  if [ -f "$pidfile" ]; then
    pid=$(cat "$pidfile")
    taskkill //PID "$pid" //F 2>/dev/null || true
    echo "stopped $name ($pid)"
  fi
done
for port in 9010 9040 8090; do
  netstat -ano | grep ":$port " | grep LISTENING || echo "port $port free"
done
rm -f "$E2E/.token" "$E2E/.rev1_id" "$E2E/.rev2_id" "$E2E/.srev_id"
```

Leave the pre-existing client dev server running only if it was already running before Task 9 (PID recorded in `$E2E/logs/client.pid` only when the plan started it; if started by the plan, stop it the same way).

- [x] **Step 6: Hand off the branch**

Announce: "I'm using the finishing-a-development-branch skill to complete this work." Invoke `superpowers:finishing-a-development-branch`, present the merge options with the results table, and **stop**. Do not merge, push, or open a PR.

- [x] **Step 7: Record evidence**

```bash
{
  echo "# Task 10 — archive + close-out"
  echo "$ARCHIVE"
  echo "archived_http=$code"
  echo "## git status"
  cd /d/open-wes
  git status --short
  git log --oneline -3
  echo "## results"
  cat "$E2E/results/RESULTS.md"
} > "$E2E/results/task10.md" 2>&1
cat "$E2E/results/task10.md"
```

---

## Self-Review Notes

- **Coverage:** Task 1 (flake), 2 (build), 3 (Liquibase + AI tables + roles/menus/grants + audit table), 4 (route approval + station/gateway), 5 (HTTP, injection, cron, supply, wave, executions/stats), 6 (station handshake + custom API), 7 (governance matrix), 8 (AI SSE + deployability + corpus), 9 (12-panel browser), 10 (archive, results, cleanup, handoff). Every open gate from `script-plugin-phase1b-prerequisites` is addressed or explicitly classified as non-gating.
- **Placeholders:** none. Credentials come from environment variables by security rule; the two env-required values are documented as environment prerequisites, not TODOs.
- **Type consistency:** API paths, DTO field names (`status`/`description` for errors, `code`/`data` for success), extension type strings, script method names, Redis key `:wms:outbound:plan:order:assigned:idsWH1`, and SQL columns were checked against the current source and live schema.
- **Deliberate limits:** the gateway route change is gated on explicit user approval; the AI step may end BLOCKED without a claim; the direct station port is informational; the corpus refresh is optional. Script timeout enforcement and non-blocking failure policy remain covered by `script-plugin-runtime` unit tests rather than a new live fixture; the test-run bad-method case exercises the failure-reporting path. The workbench AI chat drawer is rendered only as the AI-history screenshot; live generation is exercised through the API in Task 8.
