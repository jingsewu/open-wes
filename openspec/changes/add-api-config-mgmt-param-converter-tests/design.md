## Context

The API config management page (`api_management.tsx`) lets users configure JavaScript converter scripts (`jsParamConverter`, `jsResponseConverter`) for each API. Scripts are edited in a Monaco code editor inside a dialog. Currently there is no way to validate a script without triggering a real API call, making it hard to debug and verify converter logic. The frontend uses the **AMIS** low-code framework (JSON schema rendered via `schema2component`). The backend uses **GraalVM polyglot JS** via `JavaScriptUtils.executeJs` and `ConverterHelper.convertParam`.

## Goals / Non-Goals

**Goals:**
- New backend endpoint `POST /api-config-management/test-converter` that executes a given JS script against a provided JSON input and returns the output.
- New "Test" panel in the config dialog: a JSON input textarea + "Test" button + output display area, visible when `paramConverterType == 'JS'` or `responseConverterType == 'JS'`.
- Both param converter and response converter can be tested independently.

**Non-Goals:**
- Testing `TEMPLATE` (FreeMarker) converters — requires file download from FastDFS, excluded for now.
- Persisting test inputs/outputs.
- Running scripts in a sandboxed environment different from production.

## Decisions

### 1. Test endpoint accepts inline script + input JSON (not a saved code)

**Decision**: `POST /api-config-management/test-converter` takes `{ converterType, jsScript, inputJson }` — the raw script and input directly — rather than looking up a saved config by `code`.

**Rationale**: The user may be editing a script that hasn't been saved yet. Testing the unsaved current value is the primary use case. Using a saved code would force save-before-test.

**Alternative considered**: Accept `code` and load from DB. Rejected because it prevents testing before saving.

### 2. Frontend uses AMIS `service` component + `button` with `actionType: ajax`

**Decision**: Add a new section below the `jsParamConverter` editor in `configForm` using AMIS `combo` or a group of fields: a `textarea` for input JSON + a button that posts to the new endpoint + a `static` field to display the output. Use `visibleOn: "${paramConverterType == 'JS'}"`.

**Rationale**: AMIS's built-in `actionType: ajax` with `feedback` or a `service` renderer can call an endpoint and render the response into the page without custom React. Keeps implementation within the existing AMIS schema pattern.

### 3. Two test panels — one for param converter, one for response converter

**Decision**: Add separate test sections for param and response converters, each with its own input/output, gated by their respective `visibleOn` conditions.

**Rationale**: They are independent scripts; a user may want to test one without the other.

### 4. Backend reuses `ConverterHelper.convertParam` / `JavaScriptUtils.executeJs` directly

**Decision**: The new endpoint calls `JavaScriptUtils.executeJs(Context.create(), jsScript, parsedInput)` directly (same as production path) and returns the serialized output.

**Rationale**: Maximum fidelity — same execution path as production. No separate "test mode".

## Risks / Trade-offs

- **Arbitrary JS execution**: The endpoint executes user-supplied JavaScript. This is already accepted risk in the existing production converter flow; the test endpoint adds no new attack surface beyond what's already present. The endpoint should be protected by the same auth as other management endpoints.
- **GraalVM context creation overhead**: Each test call creates a new `Context.create()`. Acceptable for an interactive test action (not a hot path).
- **Input JSON validation**: If `inputJson` is malformed, the endpoint should return a clear error message rather than a 500.

## Migration Plan

1. Add `ApiConfigTestConverterParam` request param class.
2. Add `POST /test-converter` endpoint to `ApiConfigManagementController`.
3. Add `api_api_config_test_converter` constant to `api_constant.tsx`.
4. Add test panel sections to `configForm` in `api_management.tsx`.
5. No DB migration needed.
6. Rollback: remove endpoint and UI sections independently.

## Open Questions

- Should the test endpoint also support `responseConverter` script type, or only `paramConverter`? (Proposed: both, via a `converterTarget` field: `"param"` or `"response"`.)
- Should the output be pretty-printed JSON or raw string? (Proposed: pretty-print JSON if parseable, else raw string.)
