## Why

Currently, when configuring a `jsParamConverter` script for an API config, users have no way to verify whether the script is correct without actually triggering a real API call. A dedicated test endpoint and UI panel lets users interactively validate their converter scripts by inputting sample JSON and immediately seeing the converted output.

## What Changes

- **New backend endpoint**: `POST /api-config-management/test-converter` — accepts an API code (or inline JS script) and a sample JSON input, executes the `jsParamConverter` script via `ConverterHelper.convertParam`, and returns the converted output.
- **New frontend feature**: On the API config management page, add a "Test Converter" panel where users can:
  - Input sample JSON in a text area.
  - Click a "Test" button to invoke the new endpoint.
  - See the converted output JSON displayed below.

## Capabilities

### New Capabilities
- `api-config-test-converter`: A test-converter endpoint and corresponding UI panel that allows users to validate `jsParamConverter` scripts interactively by supplying sample input JSON and viewing the converted output.

### Modified Capabilities
<!-- No existing spec requirements are changing -->

## Impact

- **New backend**: `ApiConfigManagementController` — new `POST /api-config-management/test-converter` endpoint + new request param class `ApiConfigTestConverterParam`.
- **Reuses existing**: `ConverterHelper.convertParam`, `JavaScriptUtils.executeJs`.
- **Frontend**: API config management page — new test panel UI (input textarea + test button + output display).
- No database changes required.
