package org.openwes.wes.api.task.dto;

import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.stock.dto.SkuBatchAttributeDTO;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class OperationTaskVO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3507465175245802228L;
    private OperationTaskDTO operationTaskDTO;
    private SkuMainDataDTO skuMainDataDTO;
    private SkuBatchAttributeDTO skuBatchAttributeDTO;
}
