package org.openwes.wes.api.inbound.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openwes.wes.api.inbound.constants.StorageTypeEnum;

import java.io.Serializable;

@Data
public class ImportInboundPlanOrderBaseDTO implements Serializable {

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "Customer Order No", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderNo;

    @Size(max = 64)
    @Schema(title = "LPN Code")
    private String lpnCode;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "Warehouse Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Schema(title = "Inbound Plan Order Type", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderType;

    @NotNull
    @Schema(title = "Storage Type", requiredMode = Schema.RequiredMode.REQUIRED)
    private StorageTypeEnum storageType;

    @NotEmpty
    @Schema(title = "Owner")
    private String ownerCode;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "SKU Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuCode;

    @NotNull
    @Min(1)
    @Schema(title = "Inbound Quantity", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qtyRestocked;

    private String inboundDate;
    private String productDate;
    private String expiredDate;

    private String batchAttribute1;
    private String batchAttribute2;
    private String batchAttribute3;
    private String batchAttribute4;
    private String batchAttribute5;
    private String batchAttribute6;
    private String batchAttribute7;
    private String batchAttribute8;
    private String batchAttribute9;
    private String batchAttribute10;
}
