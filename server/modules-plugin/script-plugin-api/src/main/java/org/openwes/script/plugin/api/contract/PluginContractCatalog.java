package org.openwes.script.plugin.api.contract;

import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Single source of contract truth. The AI prompt, the generated documentation and the enable-time
 * static validation all read from here, so they cannot drift from the code.
 */
public final class PluginContractCatalog {

    private PluginContractCatalog() {
    }

    /**
     * @param type                   the archetype
     * @param extensionPointClassName host interface the proxy implements, or null for dispatcher-backed archetypes
     * @param scriptMethod           the method name a script must declare
     * @param hostSignature          the exact script method signature; the runtime appends ctx as the last argument
     * @param blocking               whether an exception blocks the host operation
     */
    public record ArchetypeDescriptor(ExtensionType type, String extensionPointClassName, String scriptMethod,
                                      String hostSignature, boolean blocking) {
    }

    /**
     * @param name catalog label (also accepted by ctx.bean(String)); resolution is type-first
     * @param type Spring bean type present in that app
     */
    public record BeanDescriptor(String name, Class<?> type) {
    }

    public static final List<ArchetypeDescriptor> ARCHETYPES = List.of(
            new ArchetypeDescriptor(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION,
                    "org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction",
                    "wave", "List<List<OutboundPlanOrderDTO>> wave(List<OutboundPlanOrderDTO> orders, Object ctx)",
                    true),
            new ArchetypeDescriptor(ExtensionType.OUTBOUND_WAVE_SPLIT_ACTION,
                    "org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveSplitAction",
                    "split", "List<PickingOrderDTO> split(OutboundWaveDTO wave, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.REQUEST_VALIDATE,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin",
                    "validate", "void validate(RequestHandleContext context, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.REQUEST_SUPPLY,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin",
                    "supply", "void supply(RequestHandleContext context, Object ctx)", false),
            new ArchetypeDescriptor(ExtensionType.REQUEST_AFTER_INVOKE,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin",
                    "afterInvoke", "void afterInvoke(RequestHandleContext context, Object ctx)", false),
            new ArchetypeDescriptor(ExtensionType.CUSTOM_REQUEST_INVOKE,
                    "org.openwes.plugin.extension.business.api.platform.request.ICustomRequestPlugin",
                    "invoke", "void invoke(RequestHandleContext context, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.CUSTOM_API,
                    "org.openwes.plugin.extension.business.station.ICustomApiPlugin",
                    "execute", "void execute(CustomApiParameter parameter, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.LIFECYCLE_LISTENER,
                    "org.openwes.plugin.extension.business.IEntityLifecycleListener",
                    "afterStatusChange",
                    "void afterStatusChange(Long entityId, String entityOrderNo, String newStatus, Object ctx)", false),
            new ArchetypeDescriptor(ExtensionType.HTTP_ENDPOINT, null,
                    "handle", "Object handle(Object body, Object ctx)", true),
            new ArchetypeDescriptor(ExtensionType.CRON, null,
                    "run", "Object run(Object ctx)", false));

    /** Entity names that actually receive lifecycle events; declared by the host extension interfaces. */
    public static final List<String> LIFECYCLE_ENTITY_NAMES =
            List.of("OutboundPlanOrder", "OutboundWave", "ContainerTask");

    private static final Map<String, List<BeanDescriptor>> BEAN_CATALOG = buildBeanCatalog();

    public static List<ArchetypeDescriptor> archetypes() {
        return ARCHETYPES;
    }

    /** Renders the archetype contract as the Markdown table embedded in docs/standards/backend.md. */
    public static String toMarkdownTable() {
        StringBuilder table = new StringBuilder();
        table.append("| Archetype | Host extension point | Script method | Blocking |\n");
        table.append("|-----------|----------------------|---------------|----------|\n");
        for (ArchetypeDescriptor archetype : ARCHETYPES) {
            table.append("| `").append(archetype.type()).append("` | ")
                    .append(hostExtensionPoint(archetype)).append(" | `")
                    .append(archetype.scriptMethod()).append("` | ")
                    .append(archetype.blocking() ? "yes" : "no").append(" |\n");
        }
        return table.toString();
    }

    private static String hostExtensionPoint(ArchetypeDescriptor archetype) {
        if (archetype.extensionPointClassName() != null) {
            return "`" + archetype.extensionPointClassName() + "`";
        }
        return archetype.type() == ExtensionType.HTTP_ENDPOINT
                ? "dispatcher (`/plugin-platform/ext/{pluginKey}/**`)"
                : "dispatcher (scheduler)";
    }

    public static Optional<ArchetypeDescriptor> archetype(ExtensionType type) {
        return ARCHETYPES.stream().filter(a -> a.type() == type).findFirst();
    }

    public static Map<String, List<BeanDescriptor>> beanCatalog() {
        return BEAN_CATALOG;
    }

    /**
     * Archetypes a draft may declare for an app. CUSTOM_API is station-only; wes hosts everything
     * else. Returns empty for an unknown app so callers fail closed.
     */
    public static List<ExtensionType> typesForApp(String app) {
        if (ScriptPluginApiConstants.APP_WES.equals(app)) {
            return List.of(ExtensionType.values()).stream()
                    .filter(type -> type != ExtensionType.CUSTOM_API).toList();
        }
        if (ScriptPluginApiConstants.APP_STATION.equals(app)) {
            return List.of(ExtensionType.CUSTOM_API, ExtensionType.HTTP_ENDPOINT, ExtensionType.CRON);
        }
        return List.of();
    }

    /**
     * Human/AI-readable contract subset for one app and a set of archetypes — the AI prompt and the
     * docs rewrite embed exactly this text.
     */
    public static String toPromptFragment(String app, List<ExtensionType> types) {
        StringBuilder fragment = new StringBuilder();
        fragment.append("Contract version: ").append(ScriptPluginApiConstants.CONTRACT_VERSION).append('\n');
        fragment.append("Target app: ").append(app).append('\n');
        fragment.append("Archetypes:\n");
        for (ArchetypeDescriptor archetype : ARCHETYPES) {
            if (!types.contains(archetype.type())) {
                continue;
            }
            fragment.append("- ").append(archetype.type())
                    .append(": script method ").append(archetype.hostSignature())
                    .append(archetype.blocking() ? ", blocking (exception blocks the host operation)"
                            : ", non-blocking (exception is logged and execution continues)")
                    .append('\n');
        }
        if (types.contains(ExtensionType.LIFECYCLE_LISTENER)) {
            fragment.append("Lifecycle entityName values: ")
                    .append(String.join(", ", LIFECYCLE_ENTITY_NAMES)).append('\n');
        }
        fragment.append("Available beans (ctx.bean / facade methods):\n");
        for (BeanDescriptor bean : BEAN_CATALOG.getOrDefault(app, List.of())) {
            fragment.append("- ").append(bean.name()).append(" -> ").append(bean.type().getName()).append('\n');
        }
        fragment.append("Config access: ctx.configString/configInt/configBool(field); secrets only via ctx.config.\n");
        fragment.append("Logging: ctx.log(). No hardcoded pluginKey; use ctx.pluginKey().");
        return fragment.toString();
    }

    private static Map<String, List<BeanDescriptor>> buildBeanCatalog() {
        Map<String, List<BeanDescriptor>> catalog = new LinkedHashMap<>();
        catalog.put(ScriptPluginApiConstants.APP_WES, List.of(
                new BeanDescriptor("outbound", org.openwes.wes.api.outbound.IOutboundPlanOrderApi.class),
                new BeanDescriptor("pickingOrder", org.openwes.wes.api.outbound.IPickingOrderApi.class),
                new BeanDescriptor("callback", org.openwes.api.platform.api.ICallbackApi.class)));
        catalog.put(ScriptPluginApiConstants.APP_STATION, List.of(
                new BeanDescriptor("station", org.openwes.station.api.IStationApi.class),
                new BeanDescriptor("ptl", org.openwes.station.api.IPtlApi.class)));
        return catalog;
    }
}
