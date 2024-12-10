package org.openwes.wes.api.task.dto;

import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class OperationTaskPickingDTO implements Serializable {
    private Long orderId;
    private Long detailId;
    private int operatedQty;
    private OperationTaskTypeEnum taskType;
}
