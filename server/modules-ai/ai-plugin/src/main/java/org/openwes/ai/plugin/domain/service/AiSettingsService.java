package org.openwes.ai.plugin.domain.service;

import lombok.RequiredArgsConstructor;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiSetting;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.domain.repository.ScriptPluginAiSettingRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiSettingsService {

    private final ScriptPluginAiSettingRepository repository;

    /**
     * The Liquibase seed guarantees the row exists. If an operator deleted it, the safe product
     * defaults apply; the service deliberately does not recreate rows on the request path.
     */
    public AiSettings current() {
        return repository.findBySingletonKey(ScriptPluginAiSetting.DEFAULT_KEY)
                .map(row -> new AiSettings(row.getDailyGenerationLimit(), row.getMaxTokensPerCall(),
                        row.getMaxAttempts()))
                .orElseGet(AiSettings::defaults);
    }
}
