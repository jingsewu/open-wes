package org.openwes.script.plugin.runtime.validation;

import org.junit.jupiter.api.Test;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StationValidationPollerTest {

    private final ScriptPluginApiAccessor accessor = mock(ScriptPluginApiAccessor.class);
    private final ScriptPluginMaterializationValidator validator = mock(ScriptPluginMaterializationValidator.class);
    private final StationValidationPoller poller = new StationValidationPoller(accessor, validator);

    @Test
    void poll_withoutPendingRequest_doesNothing() {
        when(validator.targetApp()).thenReturn("station");
        when(accessor.getPendingValidation("station")).thenReturn(null);

        poller.poll();

        verify(accessor, never()).reportValidation(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void poll_withPendingRequest_runsTheValidatorAndReportsBack() {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("station-api");
        bundle.setRevisionId(11L);
        ValidationReportDTO report = ValidationReportDTO.passed("station-api", 11L, "station");
        when(accessor.getPendingValidation("station")).thenReturn(bundle);
        when(validator.targetApp()).thenReturn("station");
        when(validator.validate(bundle)).thenReturn(report);

        poller.poll();

        verify(accessor).reportValidation(report);
    }

    @Test
    void poll_whenValidationThrows_reportsAFailedReport() {
        ScriptPluginBundleDTO bundle = new ScriptPluginBundleDTO();
        bundle.setPluginKey("station-api");
        bundle.setRevisionId(11L);
        when(accessor.getPendingValidation("station")).thenReturn(bundle);
        when(validator.targetApp()).thenReturn("station");
        when(validator.validate(bundle)).thenThrow(new IllegalStateException("boom"));

        poller.poll();

        verify(accessor).reportValidation(org.mockito.ArgumentMatchers.argThat(
                report -> !report.isPassed() && report.getPluginKey().equals("station-api")));
    }
}
