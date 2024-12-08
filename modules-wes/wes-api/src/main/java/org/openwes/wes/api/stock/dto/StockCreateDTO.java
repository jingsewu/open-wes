package org.openwes.wes.api.stock.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StockCreateDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 2195071631486150990L;

    @NotEmpty
    private String warehouseCode;

    @NotNull
    private Long skuBatchAttributeId;
    @NotNull
    private Long skuId;
    @NotNull
    @Min(1)
    private Integer transferQty;
    @NotEmpty
    private String orderNo;

    // if stock move from one warehouse area to another area in warehouse, this field is required
    @NotNull
    private Long warehouseAreaId;

    // upstream LPN code or upstream inbound order or put away task no
    @NotEmpty
    private String sourceContainerCode;
    private String sourceContainerSlotCode;

    @NotNull
    private Long targetContainerId;
    @NotEmpty
    private String targetContainerCode;
    private String targetContainerFace;
    @NotEmpty
    private String targetContainerSlotCode;
    private String boxNo;
    private boolean boxStock;

}
