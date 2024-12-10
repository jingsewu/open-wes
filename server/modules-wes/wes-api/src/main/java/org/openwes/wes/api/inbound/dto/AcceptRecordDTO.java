package org.openwes.wes.api.inbound.dto;

import org.openwes.common.utils.validate.IValidate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class AcceptRecordDTO implements IValidate, Serializable {


    @Serial
    private static final long serialVersionUID = -3661795226757176125L;

    private Long inboundPlanOrderId;
    private Long inboundPlanOrderDetailId;

    @NotEmpty
    @Size(max = 64)
    private String warehouseCode;

    private Map<String, Object> batchAttributes;

    @NotNull
    @Min(1)
    private Integer qtyAccepted;

    @NotNull
    private Long skuId;

    @Min(1)
    private Integer qtyAbnormal;

    @NotNull
    private Long targetContainerId;
    @NotEmpty
    @Size(max = 64)
    private String targetContainerCode;
    @NotEmpty
    @Size(max = 64)
    private String targetContainerSpecCode;
    @NotEmpty
    @Size(max = 64)
    private String targetContainerSlotCode;
    private String targetContainerFace;

    @NotNull
    private Long workStationId;

    @Override
    public boolean validate() {
        return true;
    }
}
