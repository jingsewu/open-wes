package org.openwes.wes.basic.main.data.domain.entity;

import org.openwes.wes.api.main.data.constants.OwnerTypeEnum;
import org.openwes.wes.api.main.data.dto.AddressDTO;
import org.openwes.wes.api.main.data.dto.ContactorDTO;
import lombok.Data;

@Data
public class OwnerMainData {

    private Long id;

    //ownerCode + warehouseCode = unique identifier
    private String ownerCode;
    private String warehouseCode;

    private String ownerName;

    private OwnerTypeEnum ownerType;

    private ContactorDTO contactorDTO;

    private AddressDTO addressDTO;

    private Long version;
}
