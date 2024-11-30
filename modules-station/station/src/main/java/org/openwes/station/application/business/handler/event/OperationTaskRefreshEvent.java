package org.openwes.station.application.business.handler.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Schema(description = "容器任务刷新事件")
public class OperationTaskRefreshEvent {

    @NotEmpty
    private String containerCode;

    @NotEmpty
    private String face;
}
