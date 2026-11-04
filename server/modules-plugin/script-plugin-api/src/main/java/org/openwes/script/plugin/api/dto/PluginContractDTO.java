package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.script.plugin.api.enums.ExtensionType;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginContractDTO {

    private String contractVersion;
    private String applySystem;
    private List<Archetype> archetypes;
    private List<String> lifecycleEntityNames;
    private String promptFragment;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Archetype {
        private ExtensionType type;
        private String scriptMethod;
        private String hostSignature;
        private boolean blocking;
    }
}
