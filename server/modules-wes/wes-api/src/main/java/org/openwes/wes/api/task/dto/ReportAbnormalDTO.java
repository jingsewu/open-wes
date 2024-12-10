package org.openwes.wes.api.task.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Builder
@Data
public class ReportAbnormalDTO implements Serializable {

    @NotEmpty
    private List<ReportAbnormalDTO.HandleTask> handleTasks;

    private String abnormalReason;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HandleTask implements Serializable {

        @NotNull
        private Long taskId;
        @NotNull
        private Integer requiredQty;
        @Min(1)
        @NotNull
        private Integer abnormalQty;

    }
}
