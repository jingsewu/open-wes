package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PluginCapabilitiesDTO {
    private boolean canDevelop;
    private boolean canAdminister;
    private Set<String> roles;
}
