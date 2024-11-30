package org.openwes.wes.basic.main.data.domain.entity;

import org.openwes.wes.api.main.data.constants.*;
import org.openwes.wes.api.main.data.dto.AddressDTO;
import org.openwes.wes.api.main.data.dto.ContactorDTO;
import lombok.Data;

@Data
public class WarehouseMainData {

    private Long id;
    // unique identifier
    private String warehouseCode;
    private String warehouseName;

    private WarehouseTypeEnum warehouseType;
    private WarehouseAttrTypeEnum warehouseAttrType;
    private WarehouseLevelEnum warehouseLevel;

    private String warehouseLabel;

    private WarehouseBusinessTypeEnum businessType;

    private WarehouseStructureTypeEnum structureType;
    private Integer area;
    private Integer height;
    private boolean virtualWarehouse;

    private ContactorDTO contactorDTO;

    private AddressDTO addressDTO;

    private Long version;
}

