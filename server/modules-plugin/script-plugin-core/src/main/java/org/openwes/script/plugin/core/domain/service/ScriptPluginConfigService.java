package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigDTO;
import org.openwes.script.plugin.api.dto.ScriptPluginConfigSaveDTO;
import org.springframework.stereotype.Service;

import java.util.TreeSet;

import static org.openwes.script.plugin.api.enums.ScriptPluginAuditActionEnum.CONFIG_SAVE;

@Service
@RequiredArgsConstructor
public class ScriptPluginConfigService {

    private final ScriptPluginService pluginService;
    private final PluginWorkbenchAccessService accessService;
    private final ScriptPluginOperationAuditService auditService;

    public ScriptPluginConfigDTO getConfig(String pluginKey) {
        accessService.requireAdmin();
        return pluginService.getConfig(pluginKey);
    }

    public ScriptPluginConfigDTO saveConfig(String pluginKey, ScriptPluginConfigSaveDTO dto) {
        String actor = accessService.requireAdmin(pluginKey, CONFIG_SAVE);
        ScriptPluginConfigDTO saved = pluginService.saveConfig(pluginKey, dto.getValues(),
                dto.getLockVersion());
        String fields = dto.getValues() == null ? "" : String.join(", ",
                new TreeSet<>(dto.getValues().keySet()));
        auditService.recordAs(pluginKey, CONFIG_SAVE, null, true,
                fields.isEmpty() ? "saved config with no fields" : "saved config fields: " + fields,
                actor);
        return saved;
    }
}
