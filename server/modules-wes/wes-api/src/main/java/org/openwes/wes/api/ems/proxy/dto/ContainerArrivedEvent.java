package org.openwes.wes.api.ems.proxy.dto;

import org.openwes.wes.api.ems.proxy.constants.WorkLocationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "容器到达事件数据")
public class ContainerArrivedEvent implements Serializable {

    @NotEmpty
    @Schema(description = "容器明细信息列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ContainerDetail> containerDetails;

    @NotEmpty
    private String workLocationCode;

    @Schema(title = "工作位类型")
    private WorkLocationTypeEnum workLocationType;

    private Long workStationId;

    private Long warehouseAreaId;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ContainerDetail implements Serializable {

        @NotEmpty
        @Schema(title = "容器编号")
        private String containerCode;

        // container arrived with which face
        @Schema(title = "容器面: 用于KIVA料架的面")
        private String face;

        @Schema(title = "机器人编号")
        private String robotCode;
        @Schema(title = "机器人类型")
        private String robotType;

        @Schema(title = "层")
        private Integer level;

        @Schema(title = "列")
        private Integer bay;

        @NotEmpty
        @Schema(title = "位置编码")
        private String locationCode;

        // container forward face
        @Schema(title = "容器前面: 用于描述料箱到达的朝向")
        private String forwardFace;

        @NotEmpty
        @Schema(title = "组号")
        private String groupCode;

        @Schema(title = "料箱扩展属性")
        private Map<String, Object> containerAttributes;

        @Schema(title = "任务编号")
        private List<String> taskCodes;
    }
}
