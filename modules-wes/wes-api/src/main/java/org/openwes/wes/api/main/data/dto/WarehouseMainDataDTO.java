package org.openwes.wes.api.main.data.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.openwes.wes.api.main.data.constants.*;

import java.io.Serializable;

@Data
public class WarehouseMainDataDTO implements Serializable {

    private Long id;

    @NotEmpty(message = "仓库编码不能为空!")
    @Size(max = 64, message = "仓库编码长度不能超过 64 位!")
    private String warehouseCode;

    @Size(max = 128, message = "仓库名称长度不能超过 128 位!")
    @NotEmpty(message = "仓库名称不能为空!")
    private String warehouseName;

    @NotNull(message = "仓库类型不能为空!")
    private WarehouseTypeEnum warehouseType;

    @NotNull(message = "仓库属性不能为空!")
    private WarehouseAttrTypeEnum warehouseAttrType;

    @NotNull(message = "仓库等级不能为空!")
    private WarehouseLevelEnum warehouseLevel;

    @Size(max = 64, message = "仓库标签长度不能超过 64 位!")
    private String warehouseLabel;

    @NotNull(message = "主营业务不能为空!")
    private WarehouseBusinessTypeEnum businessType;

    @NotNull(message = "仓库结构不能为空!")
    private WarehouseStructureTypeEnum structureType;

    @Min(0)
    private Integer area;
    @Min(0)
    private Integer height;
    private boolean virtualWarehouse;

    private ContactorDTO contactorDTO;

    private AddressDTO addressDTO;

    private Long version;
}
