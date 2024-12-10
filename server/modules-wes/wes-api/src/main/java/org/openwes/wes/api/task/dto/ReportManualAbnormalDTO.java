package org.openwes.wes.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class ReportManualAbnormalDTO implements Serializable {

    @NotNull
    private Long operationTaskId;

    @NotNull
    private Integer tobeOperatedQty;

    @NotEmpty
    private String abnormalReason;
}
