package org.openwes.wes.api.stock.dto;

import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ContainerStockTransactionDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3708668825417418182L;
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
