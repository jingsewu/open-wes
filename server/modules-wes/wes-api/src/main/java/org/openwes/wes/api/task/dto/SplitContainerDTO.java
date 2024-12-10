package org.openwes.wes.api.task.dto;

import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SplitContainerDTO implements Serializable {

    @NotNull
    private Long workStationId;
    @NotEmpty
    private String putWallSlotCode;

    private PutWallSlotStatusEnum putWallSlotStatusAfterSplit;
}
