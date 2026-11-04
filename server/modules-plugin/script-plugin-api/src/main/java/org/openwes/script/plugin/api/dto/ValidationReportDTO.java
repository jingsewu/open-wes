package org.openwes.script.plugin.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ValidationReportDTO implements Serializable {

    private String pluginKey;
    private Long revisionId;
    private String targetApp;
    private boolean passed;
    private List<ValidationFailureDTO> failures = new ArrayList<>();
    private Long finishedAt;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationFailureDTO implements Serializable {
        private String stage;
        private String message;
    }

    public static ValidationReportDTO passed(String pluginKey, Long revisionId, String targetApp) {
        return new ValidationReportDTO(pluginKey, revisionId, targetApp, true, new ArrayList<>(),
                System.currentTimeMillis());
    }

    public static ValidationReportDTO failed(String pluginKey, Long revisionId, String targetApp, String stage, String message) {
        List<ValidationFailureDTO> failures = new ArrayList<>();
        failures.add(new ValidationFailureDTO(stage, message));
        return new ValidationReportDTO(pluginKey, revisionId, targetApp, false, failures, System.currentTimeMillis());
    }

    public String failureSummary() {
        StringBuilder summary = new StringBuilder();
        failures.forEach(f -> summary.append(f.getStage()).append(": ").append(f.getMessage()).append('\n'));
        return summary.toString();
    }
}
