package org.openwes.wes.basic.warehouse.domain.entity;

import org.openwes.common.utils.base.UpdateUserDTO;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.BasicErrorDescEnum;
import org.openwes.wes.api.basic.constants.LocationStatusEnum;
import org.openwes.wes.api.basic.constants.LocationTypeEnum;
import org.openwes.wes.api.basic.dto.PositionDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class Location extends UpdateUserDTO {

    private Long id;

    private String locationCode;
    private String aisleCode;
    private String shelfCode;

    private String warehouseCode;
    private Long warehouseAreaId;
    private Long warehouseLogicId;

    private LocationTypeEnum locationType;

    private String containerCode;

    private String heat;
    private boolean occupied;

    private LocationStatusEnum locationStatus;

    private PositionDTO position;

    private long version;


    public void delete() {
        if (occupied) {
            throw WmsException.throwWmsException(BasicErrorDescEnum.LOCATION_CONTAINS_STOCK);
        }
    }

    public void enable() {
        this.setLocationStatus(LocationStatusEnum.PUT_AWAY_PUT_DOWN);
    }

    public void disable() {
        this.setLocationStatus(LocationStatusEnum.NONE);
    }

    public void changeContainer(String containerCode) {
        this.containerCode = containerCode;
        this.occupied = true;
    }
}
