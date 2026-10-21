package org.openwes.script.plugin.runtime.registry;

import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.script.plugin.api.dto.ExtensionDeclarationDTO;
import org.openwes.script.plugin.api.dto.PluginManagementMessage;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.openwes.script.plugin.runtime.materialize.MaterializedExtension;
import org.openwes.script.plugin.runtime.materialize.MaterializedScriptPlugin;
import org.openwes.script.plugin.runtime.materialize.ScriptPluginMaterializer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Holds the materialized script plugins of this app. Refresh triggers: startup, the Redis
 * PLUGIN_LISTEN_PLUGIN_MANAGEMENT message (published by the management plane) and a 60 s version
 * poll as the safety net. A failed rematerialization keeps the previous revision running.
 */
@Slf4j
@Component
public class ScriptPluginRegistry implements DisposableBean {

    private final ScriptPluginApiAccessor accessor;
    private final ScriptPluginMaterializer materializer;
    private final RedisUtils redisUtils;
    private final String applySystem;

    /** keyed by pluginKey; a skip list keeps the iteration order deterministic */
    private final ConcurrentSkipListMap<String, MaterializedScriptPlugin> materialized = new ConcurrentSkipListMap<>();
    private final AtomicLong lastVersion = new AtomicLong(-1);

    public ScriptPluginRegistry(ScriptPluginApiAccessor accessor,
                                ScriptPluginMaterializer materializer,
                                RedisUtils redisUtils,
                                @Value("${spring.application.name:}") String applySystem) {
        this.accessor = accessor;
        this.materializer = materializer;
        this.redisUtils = redisUtils;
        this.applySystem = applySystem;
        redisUtils.listen(RedisConstants.PLUGIN_LISTEN_PLUGIN_MANAGEMENT, PluginManagementMessage.class,
                (channel, message) -> refreshAll());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void loadAtStartup() {
        refreshAll();
    }

    @Scheduled(fixedDelay = 60_000)
    public void pollForUpdates() {
        try {
            if (accessor.getRegistryVersion(applySystem) != lastVersion.get()) {
                refreshAll();
            }
        } catch (Exception e) {
            log.warn("script plugin registry poll failed: {}", e.getMessage());
        }
    }

    public synchronized void refreshAll() {
        try {
            List<ScriptPluginBundleDTO> bundles = accessor.getEnabledPlugins(applySystem);
            long version = accessor.getRegistryVersion(applySystem);

            Set<String> enabledKeys = bundles.stream().map(ScriptPluginBundleDTO::getPluginKey)
                    .collect(Collectors.toSet());
            materialized.keySet().stream()
                    .filter(pluginKey -> !enabledKeys.contains(pluginKey))
                    .toList()
                    .forEach(this::dematerialize);

            for (ScriptPluginBundleDTO bundle : bundles) {
                MaterializedScriptPlugin current = materialized.get(bundle.getPluginKey());
                if (current != null && Objects.equals(current.getRevisionId(), bundle.getRevisionId())) {
                    continue;
                }
                MaterializedScriptPlugin fresh;
                try {
                    fresh = materializer.materialize(bundle);
                } catch (Exception e) {
                    log.error("materialization of script plugin {} revision {} failed; keeping the previous revision: {}",
                            bundle.getPluginKey(), bundle.getRevisionId(), e.getMessage());
                    discardFailedRevision(bundle.getPluginKey(), current);
                    continue;
                }

                materialized.put(bundle.getPluginKey(), fresh);
                try {
                    materializer.closeRevisionsExcept(bundle.getPluginKey(), bundle.getRevisionId());
                } catch (Exception e) {
                    log.warn("closing superseded revisions of script plugin {} failed; revision {} stays registered: {}",
                            bundle.getPluginKey(), bundle.getRevisionId(), e.getMessage());
                }
                log.info("materialized script plugin {} revision {}", bundle.getPluginKey(), bundle.getRevisionId());
            }
            lastVersion.set(version);
        } catch (Exception e) {
            log.error("script plugin registry refresh failed: {}", e.getMessage());
        }
    }

    /**
     * A failed materialization may already have cached a child context for the new revision before
     * a later declaration failed. Drop it: keep the previous revision's context if the plugin was
     * materialized before, otherwise close every context of the plugin.
     */
    private void discardFailedRevision(String pluginKey, MaterializedScriptPlugin current) {
        if (current != null) {
            materializer.closeRevisionsExcept(pluginKey, current.getRevisionId());
        } else {
            materializer.closePlugin(pluginKey);
        }
    }

    public <T> List<T> getExtensions(Class<T> extensionType) {
        List<T> extensions = new ArrayList<>();
        for (MaterializedScriptPlugin plugin : materialized.values()) {
            for (MaterializedExtension extension : plugin.getExtensions()) {
                if (extension.getExtensionPointClass() == extensionType && extension.getProxy() != null) {
                    extensions.add(extensionType.cast(extension.getProxy()));
                }
            }
        }
        return extensions;
    }

    public Collection<MaterializedScriptPlugin> all() {
        return materialized.values();
    }

    public MaterializedScriptPlugin getMaterialized(String pluginKey) {
        return materialized.get(pluginKey);
    }

    public HttpEndpointMatch findHttpEndpoint(String pluginKey, String httpMethod, String path) {
        MaterializedScriptPlugin plugin = materialized.get(pluginKey);
        if (plugin == null) {
            return null;
        }
        return plugin.getExtensions().stream()
                .filter(extension -> extension.getDeclaration().getType() == ExtensionType.HTTP_ENDPOINT)
                .filter(extension -> matchesHttpMethod(extension.getDeclaration(), httpMethod))
                .filter(extension -> path.equals(extension.getDeclaration().getPath()))
                .findFirst()
                .map(extension -> new HttpEndpointMatch(plugin, extension))
                .orElse(null);
    }

    private boolean matchesHttpMethod(ExtensionDeclarationDTO declaration, String httpMethod) {
        return declaration.getHttpMethod() == null || declaration.getHttpMethod().isBlank()
                || declaration.getHttpMethod().equalsIgnoreCase(httpMethod);
    }

    private void dematerialize(String pluginKey) {
        materialized.remove(pluginKey);
        materializer.closePlugin(pluginKey);
        log.info("script plugin {} is no longer enabled; materialization dropped", pluginKey);
    }

    @Override
    public void destroy() {
        materializer.closeAll();
    }

    public record HttpEndpointMatch(MaterializedScriptPlugin plugin, MaterializedExtension extension) {
    }
}
