package org.openwes.station.application.business.handler.event.outbound;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "拆箱事件")
public class SplitTasksEvent {
    @NotEmpty
    private String putWallSlotCode;
    @NotNull
    @Min(0)
    private Integer operatedQty;
}
