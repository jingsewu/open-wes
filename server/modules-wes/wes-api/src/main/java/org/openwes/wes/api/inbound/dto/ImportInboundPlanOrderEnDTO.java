package org.openwes.wes.api.inbound.dto;

import cn.afterturn.easypoi.excel.annotation.Excel;
import cn.afterturn.easypoi.excel.annotation.ExcelTarget;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.openwes.wes.api.inbound.constants.StorageTypeEnum;

import java.io.Serializable;

@Getter
@Setter
@ExcelTarget("importInboundPlanOrderEn")
public class ImportInboundPlanOrderEnDTO implements Serializable {

    @Excel(name = "Customer Order No", orderNum = "1", width = 20, isImportField = "true")
    @NotEmpty
    @Size(max = 64)
    @Schema(title = "Customer Order No", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderNo;

    @Excel(name = "LPN Code", orderNum = "2", width = 15, isImportField = "true")
    @Size(max = 64)
    @Schema(title = "LPN Code")
    private String lpnCode;

    @Excel(name = "Warehouse Code", orderNum = "3", width = 15, isImportField = "true")
    @NotEmpty
    @Size(max = 64)
    @Schema(title = "Warehouse Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Excel(name = "Order Type", orderNum = "4", width = 15, isImportField = "true")
    @Schema(title = "Inbound Plan Order Type", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderType;

    @Excel(name = "Storage Type", orderNum = "5", width = 15, isImportField = "true")
    @NotNull
    @Schema(title = "Storage Type", requiredMode = Schema.RequiredMode.REQUIRED)
    private StorageTypeEnum storageType;

    @Excel(name = "Owner", orderNum = "6", width = 15, isImportField = "true")
    @NotEmpty
    @Schema(title = "Owner")
    private String ownerCode;

    @Excel(name = "SKU Code", orderNum = "7", width = 15, isImportField = "true")
    @NotEmpty
    @Size(max = 64)
    @Schema(title = "SKU Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuCode;

    @Excel(name = "Quantity", orderNum = "8", width = 15, isImportField = "true")
    @NotNull
    @Min(1)
    @Schema(title = "Inbound Quantity", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qtyRestocked;

    @Excel(name = "Inbound Date", orderNum = "9", width = 15, isImportField = "true", format = "yyyy-MM-dd")
    private String inboundDate;

    @Excel(name = "Production Date", orderNum = "10", width = 15, isImportField = "true", format = "yyyy-MM-dd")
    private String productDate;

    @Excel(name = "Expiration Date", orderNum = "11", width = 15, isImportField = "true", format = "yyyy-MM-dd")
    private String expiredDate;

    @Excel(name = "Batch Attribute 1", orderNum = "12", width = 15, isImportField = "true")
    private String batchAttribute1;

    @Excel(name = "Batch Attribute 2", orderNum = "13", width = 15, isImportField = "true")
    private String batchAttribute2;

    @Excel(name = "Batch Attribute 3", orderNum = "14", width = 15, isImportField = "true")
    private String batchAttribute3;

    @Excel(name = "Batch Attribute 4", orderNum = "15", width = 15, isImportField = "true")
    private String batchAttribute4;

    @Excel(name = "Batch Attribute 5", orderNum = "16", width = 15, isImportField = "true")
    private String batchAttribute5;

    @Excel(name = "Batch Attribute 6", orderNum = "17", width = 15, isImportField = "true")
    private String batchAttribute6;

    @Excel(name = "Batch Attribute 7", orderNum = "18", width = 15, isImportField = "true")
    private String batchAttribute7;

    @Excel(name = "Batch Attribute 8", orderNum = "19", width = 15, isImportField = "true")
    private String batchAttribute8;

    @Excel(name = "Batch Attribute 9", orderNum = "20", width = 15, isImportField = "true")
    private String batchAttribute9;

    @Excel(name = "Batch Attribute 10", orderNum = "21", width = 15, isImportField = "true")
    private String batchAttribute10;
}
