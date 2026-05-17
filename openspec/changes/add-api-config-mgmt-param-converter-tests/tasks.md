## 1. Backend — Request Param Class

- [x] 1.1 Create `ApiConfigTestConverterParam` in `org.openwes.api.platform.controller.param.apiconfig` with fields: `String jsScript`, `String inputJson`
- [x] 1.2 Add `@NotEmpty` validation on `jsScript` and `@NotEmpty` validation on `inputJson`

## 2. Backend — Test Converter Endpoint

- [x] 2.1 Add `POST /test-converter` method to `ApiConfigManagementController` accepting `@RequestBody @Valid ApiConfigTestConverterParam`
- [x] 2.2 Parse `inputJson` to an Object (using `JsonUtils.string2Object`); return error response if parsing fails
- [x] 2.3 Build a transient `ApiConfigPO` with `paramConverterType = JS` and `jsParamConverter = param.getJsScript()`
- [x] 2.4 Call `ConverterHelper.convertParam(apiConfigPO, parsedInput)` and serialize the result to JSON string
- [x] 2.5 Catch `Exception` from JS execution and return error response with the exception message

## 3. Frontend — API Constant

- [x] 3.1 Add `export const api_api_config_test_converter = "post:/api-platform/api-config-management/test-converter"` to `api_constant.tsx`

## 4. Frontend — Param Converter Test Panel

- [x] 4.1 Import `api_api_config_test_converter` in `api_management.tsx`
- [x] 4.2 Add a test panel section to `configForm` below the `jsParamConverter` editor field, with `visibleOn: "${paramConverterType == 'JS'}"`
- [x] 4.3 Add a `textarea` field named `testParamInput` with label "Test Input JSON" inside the test panel
- [x] 4.4 Add an AMIS `button` with `actionType: "ajax"`, calling `api_api_config_test_converter` with body `{ jsScript: "${jsParamConverter}", inputJson: "${testParamInput}" }`
- [x] 4.5 Display the response output using a `static` or `tpl` renderer below the button, bound to the ajax response data

## 5. Frontend — Response Converter Test Panel

- [x] 5.1 Add a test panel section to `configForm` below the `jsResponseConverter` editor field, with `visibleOn: "${responseConverterType == 'JS'}"`
- [x] 5.2 Add a `textarea` field named `testResponseInput` with label "Test Input JSON" inside the response test panel
- [x] 5.3 Add an AMIS `button` with `actionType: "ajax"`, calling `api_api_config_test_converter` with body `{ jsScript: "${jsResponseConverter}", inputJson: "${testResponseInput}" }`
- [x] 5.4 Display the response output using a `static` or `tpl` renderer below the button, bound to the ajax response data

## 6. Verification

- [ ] 6.1 Start the backend and confirm `POST /api-platform/api-config-management/test-converter` returns correctly converted output for a valid JS script and input JSON
- [ ] 6.2 Confirm the endpoint returns an error message for malformed input JSON
- [ ] 6.3 Confirm the endpoint returns a script error message when the JS throws
- [ ] 6.4 Open the API config dialog in the UI, verify the param converter test panel appears only when `paramConverterType == JS`
- [ ] 6.5 Open the API config dialog in the UI, verify the response converter test panel appears only when `responseConverterType == JS`
- [ ] 6.6 Enter sample JSON and click "Test" in the UI — confirm output is displayed correctly
