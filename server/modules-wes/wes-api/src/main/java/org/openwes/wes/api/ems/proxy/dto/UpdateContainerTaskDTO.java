package org.openwes.wes.api.ems.proxy.dto;

import org.openwes.wes.api.ems.proxy.constants.ContainerTaskStatusEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
@Schema(description = "容器任务数据")
public class UpdateContainerTaskDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 3806352763513312807L;

    @NotEmpty
    @Schema(title = "任务编码")
    private String taskCode;

    @NotNull
    @Schema(title = "任务状态")
    private ContainerTaskStatusEnum taskStatus;

    @Schema(title = "机器人编号")
    private String robotCode;

    @Schema(title = "容器编号")
    private String containerCode;

    @Schema(title = "工作站编号")
    private Long workStationId;

    @Schema(title = "位置编码")
    private String locationCode;
}
