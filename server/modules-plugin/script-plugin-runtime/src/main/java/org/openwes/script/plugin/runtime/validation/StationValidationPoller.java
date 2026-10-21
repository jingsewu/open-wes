package org.openwes.script.plugin.runtime.validation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.script.plugin.api.ScriptPluginMaterializationValidator;
import org.openwes.script.plugin.api.dto.ScriptPluginBundleDTO;
import org.openwes.script.plugin.api.dto.ValidationReportDTO;
import org.openwes.script.plugin.runtime.registry.ScriptPluginApiAccessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Completes the enable-time validation handshake on remote target apps: polls the management plane
 * for a validation request addressed to this app, runs the local materialization dry-run and
 * reports the verdict back.
 */
@Component
@ConditionalOnProperty(name = "script-plugin.registry.mode", havingValue = "remote")
@RequiredArgsConstructor
@Slf4j
public class StationValidationPoller {

    private final ScriptPluginApiAccessor accessor;
    private final ScriptPluginMaterializationValidator validator;

    @Scheduled(fixedDelayString = "${script-plugin.validation.poll-interval-ms:5000}")
    public void poll() {
        try {
            ScriptPluginBundleDTO pending = accessor.getPendingValidation(validator.targetApp());
            if (pending == null) {
                return;
            }
            ValidationReportDTO report;
            try {
                report = validator.validate(pending);
            } catch (Exception e) {
                report = ValidationReportDTO.failed(pending.getPluginKey(), pending.getRevisionId(),
                        validator.targetApp(), "remote", e.getMessage());
            }
            accessor.reportValidation(report);
            log.info("reported validation of plugin {} revision {}: passed={}", pending.getPluginKey(),
                    pending.getRevisionId(), report.isPassed());
        } catch (Exception e) {
            log.warn("script plugin validation poll failed: {}", e.getMessage());
        }
    }
}
