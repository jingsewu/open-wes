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
@ExcelTarget("importInboundPlanOrder")
public class ImportInboundPlanOrderZhDTO implements Serializable {

    @Excel(name = "客户订单号", orderNum = "1", width = 20, isImportField = "true")
    @NotEmpty
    @Size(max = 64)
    @Schema(title = "客户订单号", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderNo;

    @Excel(name = "LPN编码", orderNum = "2", width = 15, isImportField = "true")
    @Size(max = 64)
    @Schema(title = "LPN 编码")
    private String lpnCode;

    @Excel(name = "仓库编码", orderNum = "3", width = 15, isImportField = "true")
    @NotEmpty
    @Size(max = 64)
    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @Excel(name = "入库计划单类型", orderNum = "4", width = 15, isImportField = "true")
    @Schema(title = "入库计划单类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private String customerOrderType;

    @Excel(name = "存储类型", orderNum = "5", width = 15, isImportField = "true")
    @NotNull
    @Schema(title = "存储类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private StorageTypeEnum storageType;

    @Excel(name = "货主", orderNum = "6", width = 15, isImportField = "true")
    @NotEmpty
    @Schema(title = "货主")
    private String ownerCode;

    @Excel(name = "商品编码", orderNum = "7", width = 15, isImportField = "true")
    @NotEmpty
    @Size(max = 64)
    @Schema(title = "商品编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skuCode;

    @Excel(name = "入库数量", orderNum = "8", width = 15, isImportField = "true")
    @NotNull
    @Min(1)
    @Schema(title = "入库数量", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer qtyRestocked;

    @Excel(name = "入库日期", orderNum = "9", width = 15, isImportField = "true")
    private String inboundDate;

    @Excel(name = "生产日期", orderNum = "10", width = 15, isImportField = "true")
    private String productDate;

    @Excel(name = "过期日期", orderNum = "11", width = 15, isImportField = "true")
    private String expiredDate;

    @Excel(name = "批次属性1", orderNum = "12", width = 15, isImportField = "true")
    private String batchAttribute1;

    @Excel(name = "批次属性2", orderNum = "13", width = 15, isImportField = "true")
    private String batchAttribute2;

    @Excel(name = "批次属性3", orderNum = "14", width = 15, isImportField = "true")
    private String batchAttribute3;

    @Excel(name = "批次属性4", orderNum = "15", width = 15, isImportField = "true")
    private String batchAttribute4;

    @Excel(name = "批次属性5", orderNum = "16", width = 15, isImportField = "true")
    private String batchAttribute5;

    @Excel(name = "批次属性6", orderNum = "17", width = 15, isImportField = "true")
    private String batchAttribute6;

    @Excel(name = "批次属性7", orderNum = "18", width = 15, isImportField = "true")
    private String batchAttribute7;

    @Excel(name = "批次属性8", orderNum = "19", width = 15, isImportField = "true")
    private String batchAttribute8;

    @Excel(name = "批次属性9", orderNum = "20", width = 15, isImportField = "true")
    private String batchAttribute9;

    @Excel(name = "批次属性10", orderNum = "21", width = 15, isImportField = "true")
    private String batchAttribute10;
}
