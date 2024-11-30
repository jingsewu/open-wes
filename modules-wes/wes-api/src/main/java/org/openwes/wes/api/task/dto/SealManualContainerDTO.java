package org.openwes.wes.api.task.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class SealManualContainerDTO {

    @NotNull
    private Long pickingOrderId;
}
