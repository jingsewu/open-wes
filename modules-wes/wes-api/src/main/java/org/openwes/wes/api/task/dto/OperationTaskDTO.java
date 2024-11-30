package org.openwes.wes.api.task.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.Accessors;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;

import java.io.Serializable;
import java.util.Map;

/**
 * abstract of operation task contains all tasks. eg: inbound, outbound, relocation, etc.
 */
@Data
@AllArgsConstructor
@Accessors(chain = true)
public class OperationTaskDTO implements Serializable {

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @NotEmpty
    private String warehouseCode;

    @NotNull
    private OperationTaskTypeEnum taskType;

    private Long workStationId;

    @NotNull
    private Long skuId;

    private Integer priority;

    @NotNull
    private Long skuBatchStockId;
    @NotNull
    private Long skuBatchAttributeId;
    @NotNull
    private Long containerStockId;

    private String sourceContainerCode;
    private String sourceContainerFace;
    private String sourceContainerSlot;

    private String boxNo;

    @NotNull
    @Min(1)
    private Integer requiredQty;
    private Integer operatedQty;
    private Integer abnormalQty = 0;
    private Integer toBeOperatedQty;

    private String targetLocationCode;
    private String targetContainerCode;
    private Long transferContainerRecordId;
    private String targetContainerSlot;

    private Long orderId;
    private String orderNo;
    private Long detailId;
    private Long originalTaskId;

    private OperationTaskStatusEnum taskStatus;

    private Map<Long, String> assignedStationSlot;

    private boolean abnormal;

    private boolean shortComplete;

    private String updateUser;

    public OperationTaskDTO() {
        this.taskStatus = OperationTaskStatusEnum.NEW;
    }


    public Integer getToBeOperatedQty() {
        return this.requiredQty - this.operatedQty - this.abnormalQty;
    }


}
