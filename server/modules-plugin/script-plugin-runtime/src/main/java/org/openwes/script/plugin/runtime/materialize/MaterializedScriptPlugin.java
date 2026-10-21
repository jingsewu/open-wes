package org.openwes.script.plugin.runtime.materialize;

import lombok.Getter;

import java.util.List;

@Getter
public class MaterializedScriptPlugin {

    private final String pluginKey;
    private final Long revisionId;
    private final List<MaterializedExtension> extensions;

    public MaterializedScriptPlugin(String pluginKey, Long revisionId, List<MaterializedExtension> extensions) {
        this.pluginKey = pluginKey;
        this.revisionId = revisionId;
        this.extensions = extensions;
    }
}
