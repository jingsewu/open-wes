package org.openwes.wes.api.stocktake.dto;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
@Schema(description = "盘点单执行请求")
public class StocktakeOrderExecuteDTO implements Serializable {
    /**
     * 仓库编码
     */
    @NotEmpty
    @Size(max = 64)
    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    /**
     * 盘点单编号列表
     */
    @NotEmpty
    @Schema(title = "盘点单编号列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<String> orderNos;

    /**
     * 执行盘点单时计划将盘点单拆分成多少个盘点任务
     */
    @Min(1)
    @NotNull
    @Schema(title = "盘点任务数", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer taskCount = 1;

    /**
     * 执行盘点单时创建盘点任务直接执行盘点任务（无工作站）
     */
    @Hidden
    private Boolean executeTask;
}
