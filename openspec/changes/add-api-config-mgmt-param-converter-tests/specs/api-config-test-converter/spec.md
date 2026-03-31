## ADDED Requirements

### Requirement: Test converter endpoint
The system SHALL provide a `POST /api-config-management/test-converter` endpoint that accepts a JS converter script and a JSON input string, executes the script using the same runtime as production (`JavaScriptUtils.executeJs`), and returns the converted output as a JSON string.

#### Scenario: Successful param conversion
- **WHEN** a client POSTs `{ "jsScript": "<valid JS function>", "inputJson": "<valid JSON>" }` to `/api-config-management/test-converter`
- **THEN** the system executes the JS script against the parsed input and returns HTTP 200 with a body containing the serialized converted output

#### Scenario: Invalid JSON input
- **WHEN** a client POSTs with a malformed `inputJson` string
- **THEN** the system returns an error response with a descriptive message indicating the input JSON is invalid

#### Scenario: JS script execution error
- **WHEN** a client POSTs a `jsScript` that throws a runtime exception during execution
- **THEN** the system returns an error response with the exception message so the user can debug the script

#### Scenario: Empty or null script
- **WHEN** a client POSTs with a null or empty `jsScript`
- **THEN** the system returns an error response indicating the script is required

### Requirement: Test converter UI panel — param converter
The system SHALL display a collapsible test panel below the `jsParamConverter` editor in the API config dialog, visible only when `paramConverterType == 'JS'`. The panel SHALL contain a JSON input textarea, a "Test" button, and a read-only output display area.

#### Scenario: User tests a param converter script
- **WHEN** the user enters sample JSON in the input textarea and clicks the "Test" button
- **THEN** the frontend calls `POST /api-config-management/test-converter` with the current `jsParamConverter` script and the entered JSON, then displays the returned output in the output area

#### Scenario: Param converter test panel hidden for non-JS type
- **WHEN** `paramConverterType` is not `JS`
- **THEN** the param converter test panel is NOT visible

#### Scenario: Test returns error
- **WHEN** the test endpoint returns an error (invalid JSON or script error)
- **THEN** the output area displays the error message so the user can identify and fix the issue

### Requirement: Test converter UI panel — response converter
The system SHALL display a collapsible test panel below the `jsResponseConverter` editor in the API config dialog, visible only when `responseConverterType == 'JS'`. The panel SHALL contain a JSON input textarea, a "Test" button, and a read-only output display area.

#### Scenario: User tests a response converter script
- **WHEN** the user enters sample JSON in the input textarea and clicks the "Test" button
- **THEN** the frontend calls `POST /api-config-management/test-converter` with the current `jsResponseConverter` script and the entered JSON, then displays the returned output in the output area

#### Scenario: Response converter test panel hidden for non-JS type
- **WHEN** `responseConverterType` is not `JS`
- **THEN** the response converter test panel is NOT visible
