package org.openwes.wes.stock.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;

@Data
@EqualsAndHashCode(callSuper = false)
public class ContainerStockTransaction {

    private Long id;
    private Long containerStockId;
    private Long skuBatchStockId;

    private Long skuId;

    private OperationTaskTypeEnum operationTaskType;

    private String warehouseCode;

    private String sourceContainerCode;
    private String sourceContainerSlotCode;

    private String targetContainerCode;
    private String targetContainerSlotCode;

    private String containerCode;
    private String containerSlotCode;

    private String orderNo;

    private Long taskId;

    private Integer transferQty;

    private Long version;


}
