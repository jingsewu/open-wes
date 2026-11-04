package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScriptPluginConfigDTO {
    private Map<String, Object> values;
    private List<ConfigFieldDTO> schema;
    private Long lockVersion;
}
