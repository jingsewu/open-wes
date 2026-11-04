package org.openwes.script.plugin.core.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.common.utils.exception.CommonException;
import org.openwes.script.plugin.api.ScriptPluginApiConstants;
import org.openwes.script.plugin.api.contract.PluginContractCatalog;
import org.openwes.script.plugin.api.dto.PluginContractDTO;
import org.openwes.script.plugin.api.enums.ExtensionType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScriptPluginContractService {

    private final PluginWorkbenchAccessService accessService;

    public PluginContractDTO contract(String applySystem) {
        accessService.requireAuthenticated();
        List<ExtensionType> types = PluginContractCatalog.typesForApp(applySystem);
        if (types.isEmpty()) {
            throw new CommonException("unsupported applySystem: " + applySystem);
        }
        List<PluginContractDTO.Archetype> archetypes = PluginContractCatalog.ARCHETYPES.stream()
                .filter(archetype -> types.contains(archetype.type()))
                .map(archetype -> new PluginContractDTO.Archetype(archetype.type(),
                        archetype.scriptMethod(), archetype.hostSignature(), archetype.blocking()))
                .toList();
        return new PluginContractDTO(
                ScriptPluginApiConstants.CONTRACT_VERSION,
                applySystem,
                archetypes,
                PluginContractCatalog.LIFECYCLE_ENTITY_NAMES,
                PluginContractCatalog.toPromptFragment(applySystem, types));
    }
}
