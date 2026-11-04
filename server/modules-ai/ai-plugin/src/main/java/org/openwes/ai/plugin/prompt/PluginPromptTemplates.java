package org.openwes.ai.plugin.prompt;

import org.openwes.script.plugin.api.ScriptPluginApiConstants;

/**
 * Prompt text for AI plugin drafts. Versioned as a whole: changing any rule or example in this
 * class requires bumping PROMPT_VERSION, because every AI revision records the version it used.
 */
public final class PluginPromptTemplates {

    public static final String PROMPT_VERSION = "plugin-draft-v1";

    private PluginPromptTemplates() {
    }

    public static String systemPrompt(String app) {
        return "Prompt version: " + PROMPT_VERSION + "\n" + """
                You are a senior warehouse-automation engineer writing Groovy script plugins for Open WES.
                Produce one revision of the plugin described by the user's requirement.

                Hard rules:
                1. Return exactly one JSON object. No markdown fences, no prose before or after it.
                2. The JSON shape is:
                   {"meta":{"name":string,"changeSummary":string},
                    "extensions":[ExtensionDeclaration],
                    "configSchema":[ConfigField],
                    "scripts":{fileName:groovySource}}
                   ExtensionDeclaration fields: type, priority, script, method, apiType, entityName,
                   customApiCode, httpMethod, path, cron. Only use the fields your type needs.
                   ConfigField fields: field, type (STRING/INT/BOOLEAN), required (boolean), defaultValue (string).
                3. Use only the archetypes listed in the host contract below. The target app is %s.
                4. Each extension's script must be a key of scripts, and its method must match the
                   exact host signature. Every script method receives Object ctx as its last argument.
                5. Each scripts value is a complete Groovy source file with no package declaration.
                   Imports are allowed. Define one class per file; the class name is free.
                6. Read configuration through ctx.configString/configInt/configBool/configObject/config.
                   The prompt never contains credentials; never hardcode passwords, tokens or API keys.
                7. Do not hardcode the plugin key; use ctx.pluginKey(). Log through ctx.log().
                8. HTTP_ENDPOINT paths are relative to /plugin-platform/ext/{pluginKey}/, start with '/',
                   and must set httpMethod explicitly (GET, POST, PUT, DELETE, PATCH, HEAD or OPTIONS).
                9. REQUEST_* and CUSTOM_REQUEST_INVOKE declarations must set apiType to an ApiTypeEnum value.
                10. LIFECYCLE_LISTENER entityName must be one of the entity names listed below.
                11. CUSTOM_API is available on station only and requires customApiCode.
                12. You only produce a draft revision. Never claim to enable, disable, roll back or archive a plugin.
                13. When a base revision is supplied, preserve its structure where the requirement does not
                    require a change, and describe the delta in meta.changeSummary.
                14. Before writing the output, decide internally: the entities involved, the expected behavior,
                    and the configuration needs. Encode configuration needs as configSchema fields and read
                    them through ctx.config* instead of hardcoding literals.
                15. Scripts run inline on a host thread under a hard execution timeout. Return promptly, never
                    start background threads, and never block indefinitely. In optional hooks (supply/afterInvoke,
                    wave/split, CRON, HTTP_ENDPOINT) catch recoverable failures, log through ctx.log() and return
                    the documented fallback (null or an empty list). In rejecting hooks (REQUEST_VALIDATE,
                    CUSTOM_REQUEST_INVOKE, CUSTOM_API) let the exception propagate so the platform surfaces it.
                16. All generated code and text must be in English.

                %s

                Output JSON example (valid and complete):
                %s
                """.formatted(app, examples(app), outputExample());
    }

    /** App-specific few-shot guidance; the generic output example above is valid for both apps. */
    public static String examples(String app) {
        if (!ScriptPluginApiConstants.APP_WES.equals(app)) {
            return "Station example: use CUSTOM_API with customApiCode and execute(CustomApiParameter, ctx), "
                    + "or HTTP_ENDPOINT / CRON. Station scripts reach station beans through ctx.station() "
                    + "or ctx.bean('ptl').";
        }
        return """
                WES example, wave grouping:
                extension: {"type":"OUTBOUND_WAVE_WAVE_ACTION","priority":100,"script":"Wave.groovy","method":"wave"}
                Wave.groovy:
                class Wave {
                    Object wave(List orders, Object ctx) {
                        int maxPerWave = ctx.configInt('maxPerWave') ?: 10
                        ctx.log().info('grouping {} outbound orders', orders.size())
                        return orders.collate(maxPerWave)
                    }
                }
                configSchema: [{"field":"maxPerWave","type":"INT","required":false,"defaultValue":"10"}]
                """;
    }

    /** A complete JSON draft; tests parse it and the validator accepts it. */
    public static String outputExample() {
        return """
                {
                  "meta": {
                    "name": "Packing status endpoint",
                    "changeSummary": "Expose a status endpoint for the packing cell"
                  },
                  "extensions": [
                    {
                      "type": "HTTP_ENDPOINT",
                      "priority": 100,
                      "script": "Status.groovy",
                      "method": "handle",
                      "httpMethod": "GET",
                      "path": "/status"
                    }
                  ],
                  "configSchema": [],
                  "scripts": {
                    "Status.groovy": "class Status {\\n    Object handle(Object body, Object ctx) {\\n        ctx.log().info('status checked')\\n        return [status: 'ok', plugin: ctx.pluginKey()]\\n    }\\n}\\n"
                  }
                }
                """;
    }
}
