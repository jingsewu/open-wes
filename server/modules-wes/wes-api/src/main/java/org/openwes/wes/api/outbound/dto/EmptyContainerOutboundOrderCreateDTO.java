package org.openwes.wes.api.outbound.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmptyContainerOutboundOrderCreateDTO {

    @NotEmpty
    private String warehouseCode;

    @NotEmpty
    private String containerSpecCode;

    @NotNull
    private Long warehouseAreaId;

    private Integer emptySlotNum;

    private String warehouseLogicCode;

    @Min(1)
    private Integer planCount;

    @NotNull
    private Long workStationId;

}
