package org.openwes.ai.plugin.domain.service;

import org.junit.jupiter.api.Test;
import org.openwes.ai.plugin.domain.entity.ScriptPluginAiSetting;
import org.openwes.ai.plugin.domain.model.AiSettings;
import org.openwes.ai.plugin.domain.repository.ScriptPluginAiSettingRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AiSettingsServiceTest {

    private final ScriptPluginAiSettingRepository repository = mock(ScriptPluginAiSettingRepository.class);
    private final AiSettingsService service = new AiSettingsService(repository);

    @Test
    void missingRow_returnsSafeDefaults() {
        when(repository.findBySingletonKey(ScriptPluginAiSetting.DEFAULT_KEY)).thenReturn(Optional.empty());

        assertEquals(AiSettings.defaults(), service.current());
    }

    @Test
    void storedValues_areClampedToThePolicy() {
        ScriptPluginAiSetting row = new ScriptPluginAiSetting();
        row.setDailyGenerationLimit(-3);
        row.setMaxTokensPerCall(0);
        row.setMaxAttempts(99);
        when(repository.findBySingletonKey(ScriptPluginAiSetting.DEFAULT_KEY)).thenReturn(Optional.of(row));

        assertEquals(new AiSettings(0, 4096, 3), service.current());
    }

    @Test
    void storedValues_withinPolicy_areReturnedVerbatim() {
        ScriptPluginAiSetting row = new ScriptPluginAiSetting();
        row.setDailyGenerationLimit(7);
        row.setMaxTokensPerCall(2048);
        row.setMaxAttempts(2);
        when(repository.findBySingletonKey(ScriptPluginAiSetting.DEFAULT_KEY)).thenReturn(Optional.of(row));

        assertEquals(new AiSettings(7, 2048, 2), service.current());
    }
}
