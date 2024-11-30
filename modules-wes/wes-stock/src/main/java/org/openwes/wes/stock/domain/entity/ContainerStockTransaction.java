package org.openwes.wes.stock.domain.entity;

import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ContainerStockTransaction {

    private Long id;
    private Long containerStockId;
    private Long skuBatchStockId;

    private OperationTaskTypeEnum operationTaskType;

    private String warehouseCode;

    private String sourceContainerCode;
    private String sourceContainerSlotCode;

    private String targetContainerCode;
    private String targetContainerSlotCode;

    private String orderNo;

    private Long taskId;

    private Integer transferQty;

    private Long version;


}
