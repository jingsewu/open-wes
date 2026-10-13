package org.openwes.script.plugin.api.host;

import org.openwes.api.platform.api.ICallbackApi;
import org.openwes.script.plugin.api.dto.ConfigFieldDTO;
import org.openwes.station.api.IStationApi;
import org.openwes.wes.api.outbound.IOutboundPlanOrderApi;
import org.openwes.wes.api.outbound.IPickingOrderApi;
import org.slf4j.Logger;

import java.util.List;

/**
 * Host context passed to every script method. Facade methods are sugar over bean lookup against
 * the target app's catalog (see PluginContractCatalog.beanCatalog); availability differs per app.
 * This is a usability contract, not a security boundary — scripts run with full JVM access.
 */
public interface ScriptContext {

    String pluginKey();

    /** Child (revision) context first, then the main application context. */
    <T> T bean(Class<T> type);

    Object bean(String name);

    Object configObject(String field);

    String configString(String field);

    Integer configInt(String field);

    Boolean configBool(String field);

    List<ConfigFieldDTO> configSchema();

    ScriptHttpClient http();

    Logger log();

    default IOutboundPlanOrderApi outbound() {
        return bean(IOutboundPlanOrderApi.class);
    }

    default IPickingOrderApi pickingOrder() {
        return bean(IPickingOrderApi.class);
    }

    default ICallbackApi callback() {
        return bean(ICallbackApi.class);
    }

    @SuppressWarnings("rawtypes")
    default IStationApi station() {
        return bean(IStationApi.class);
    }
}
