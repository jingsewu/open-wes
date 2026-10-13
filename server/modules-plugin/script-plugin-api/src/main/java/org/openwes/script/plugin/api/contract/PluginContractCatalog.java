package org.openwes.script.plugin.api.contract;

import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
     * @param blocking               whether an exception blocks the host operation
     */
    public record ArchetypeDescriptor(ExtensionType type, String extensionPointClassName, String scriptMethod,
                                      boolean blocking) {
    }

    /**
     * @param name catalog label (also accepted by ctx.bean(String)); resolution is type-first
     * @param type Spring bean type present in that app
     */
    public record BeanDescriptor(String name, Class<?> type) {
    }

    public static final List<ArchetypeDescriptor> ARCHETYPES = List.of(
            new ArchetypeDescriptor(ExtensionType.OUTBOUND_WAVE_WAVE_ACTION,
                    "org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveWaveAction", "wave", true),
            new ArchetypeDescriptor(ExtensionType.OUTBOUND_WAVE_SPLIT_ACTION,
                    "org.openwes.plugin.extension.business.wes.outbound.action.IOutboundWaveSplitAction", "split", true),
            new ArchetypeDescriptor(ExtensionType.REQUEST_VALIDATE,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin", "validate", true),
            new ArchetypeDescriptor(ExtensionType.REQUEST_SUPPLY,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin", "supply", false),
            new ArchetypeDescriptor(ExtensionType.REQUEST_AFTER_INVOKE,
                    "org.openwes.plugin.extension.business.api.platform.request.IRequestPlugin", "afterInvoke", false),
            new ArchetypeDescriptor(ExtensionType.CUSTOM_REQUEST_INVOKE,
                    "org.openwes.plugin.extension.business.api.platform.request.ICustomRequestPlugin", "invoke", true),
            new ArchetypeDescriptor(ExtensionType.CUSTOM_API,
                    "org.openwes.plugin.extension.business.station.ICustomApiPlugin", "execute", true),
            new ArchetypeDescriptor(ExtensionType.LIFECYCLE_LISTENER,
                    "org.openwes.plugin.extension.business.IEntityLifecycleListener", "afterStatusChange", false),
            new ArchetypeDescriptor(ExtensionType.HTTP_ENDPOINT, null, "handle", true),
            new ArchetypeDescriptor(ExtensionType.CRON, null, "run", false));

    private static final Map<String, List<BeanDescriptor>> BEAN_CATALOG = buildBeanCatalog();

    public static List<ArchetypeDescriptor> archetypes() {
        return ARCHETYPES;
    }

    public static Optional<ArchetypeDescriptor> archetype(ExtensionType type) {
        return ARCHETYPES.stream().filter(a -> a.type() == type).findFirst();
    }

    public static Map<String, List<BeanDescriptor>> beanCatalog() {
        return BEAN_CATALOG;
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
                    .append(": implement '")
                    .append(archetype.extensionPointClassName() == null ? "dispatcher" : archetype.extensionPointClassName())
                    .append("', script method '")
                    .append(archetype.scriptMethod()).append("(.., ctx)'")
                    .append(archetype.blocking() ? ", blocking (exception blocks the host operation)"
                            : ", non-blocking (exception is logged and execution continues)")
                    .append('\n');
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
