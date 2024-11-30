package org.openwes.station.application.business.handler.event.outbound;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Accessors(chain = true)
@Data
@Schema(description = "点击播种墙格口事件")
public class TapPutWallSlotEvent {

    @NotEmpty
    @Schema(description = "播种墙格口", requiredMode = Schema.RequiredMode.REQUIRED)
    private String putWallSlotCode;
}
