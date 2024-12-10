package org.openwes.wes.api.task.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SplitManualContainerDTO implements Serializable {

    @NotEmpty
    private String warehouseCode;

    @NotNull
    private Long operationTaskId;

    @NotNull
    private Integer operatedQty;

    private String newTransferContainerCode;
}
