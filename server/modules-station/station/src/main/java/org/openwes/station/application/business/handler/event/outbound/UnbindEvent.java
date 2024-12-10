package org.openwes.station.application.business.handler.event.outbound;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
@Schema(description = "取消绑箱事件")
public class UnbindEvent {

    @NotEmpty
    private List<String> putWallSlotCodes;
}
