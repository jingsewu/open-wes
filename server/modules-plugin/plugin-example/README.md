# Lazy Waving — Script Plugin Sample

This sample is the script-plugin replacement for the legacy lazy-waving jar plugin. It declares one
`OUTBOUND_WAVE_WAVE_ACTION`: outbound plan orders are grouped into waves, but orders that share a
`customerWaveNo` with an already completed order are withheld until the packing station reports
online.

## Files

| File | Purpose |
|---|---|
| `plugin.json` | Bundle metadata: one declaration and two string config fields |
| `lazy-waving.groovy` | The script invoked by the host wave action |

## Declaration

| Field | Value |
|---|---|
| type | `OUTBOUND_WAVE_WAVE_ACTION` |
| script | `lazy-waving.groovy` |
| method | `wave` |
| priority | `100` |

## Configuration

| Field | Type | Required | Default |
|---|---|---|---|
| `packingStatusApiCode` | STRING | yes | `GET_PACKING_STATUS` |
| `packingStatusOnlineValue` | STRING | yes | `1` |

## Import and enable

The sample is not seeded automatically. Create it through the workbench:

1. Open `/plugin-platform/plugin-workbench?mode=new` as a user with the `pluginDeveloper` role.
2. Create a draft with the metadata from `plugin.json` and paste `lazy-waving.groovy` into the script editor.
3. Add the two config fields, save a revision and validate it.
4. Enable the plugin from `/plugin-platform/plugins` as a user with the `pluginAdmin` role.

## Runtime notes

- `ctx.outbound()` and `ctx.callback()` are available only on `wes`; the sample declares `applySystem: "wes"`.
- Read secrets through `ctx.config`, never hardcode them in the script.
- With no enabled wave plugin, wave creation keeps its built-in behavior.

## Validation

Java 17 is required.

```bash
cd server
./gradlew :modules-plugin:script-plugin-api:test --tests '*ScriptPluginExampleValidationTest'
```

The test compiles the Groovy script and runs the same static validation stages as enable time
(`sdkVersion`, `declarations`, `compile`). It does not start an application context or touch a
database.

See [Script Plugin Rules](../../../docs/standards/backend.md#script-plugin-rules) for the full contract.
