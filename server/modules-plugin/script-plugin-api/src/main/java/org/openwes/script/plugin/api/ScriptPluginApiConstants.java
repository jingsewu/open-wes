package org.openwes.script.plugin.api;

import java.util.List;

public final class ScriptPluginApiConstants {

    private ScriptPluginApiConstants() {
    }

    /** Platform contract version; a plugin's sdk_version must equal this or enabling is refused. */
    public static final String CONTRACT_VERSION = "1.0";

    public static final String APP_WES = "wes";
    public static final String APP_STATION = "station";

    /** Target apps that may host script plugins. A single plugin never spans both (spec: no cross-app plugins in v1). */
    public static final List<String> SUPPORTED_APPS = List.of(APP_WES, APP_STATION);
}
