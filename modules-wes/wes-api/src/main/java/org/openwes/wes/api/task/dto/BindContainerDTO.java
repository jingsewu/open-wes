package org.openwes.wes.api.task.dto;

import org.openwes.common.utils.validate.IValidate;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class BindContainerDTO implements Serializable, IValidate {

    private Long workStationId;

    //exist put wall then will need put wall slot code
    private String putWallSlotCode;

    //without put wall then need picking order id
    private Long pickingOrderId;

    @NotEmpty
    private String containerCode;
    @NotEmpty
    private String warehouseCode;

    @Override
    public boolean validate() {
        return StringUtils.isNotEmpty(this.putWallSlotCode) || this.pickingOrderId != null;
    }

    public boolean isNeedHandlePutWallSlot() {
        return StringUtils.isNotEmpty(this.putWallSlotCode);
    }
}
