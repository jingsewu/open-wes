package org.openwes.station.application.business.handler.event.outbound;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "异常登记事件")
public class ReportAbnormalEvent {

    @NotEmpty
    private String abnormalReason;

    @NotEmpty
    private List<ReportAbnormalTask> reportAbnormalTasks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReportAbnormalTask implements Serializable {

        @NotNull
        private Long taskId;

        @NotNull
        private Integer toBeOperatedQty;
    }
}
