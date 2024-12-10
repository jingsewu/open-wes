package org.openwes.wes.api.task.dto;

import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
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
public class HandleTaskDTO implements Serializable {

    private Long workStationId;

    @NotEmpty
    private List<HandleTask> handleTasks;
    @NotNull
    private HandleTaskTypeEnum handleTaskType;
    @NotNull
    private Long transferContainerRecordId;
    @NotEmpty
    private String transferContainerCode;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class HandleTask implements Serializable {


        @NotNull
        private Long taskId;
        @NotNull
        private Integer requiredQty;
        @NotNull
        private Integer operatedQty;
        private Integer abnormalQty;
        @NotNull
        private OperationTaskTypeEnum taskType;

    }

    public boolean isNeededAutoSealContainer() {
        return this.workStationId == null;
    }

    public enum HandleTaskTypeEnum {
        COMPLETE, SPLIT;
    }
}
