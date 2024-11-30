package org.openwes.wes.api.task.dto;

import org.openwes.common.utils.validate.IValidate;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class SealContainerDTO implements Serializable, IValidate {

    @NotEmpty
    private String warehouseCode;

    private Long workStationId;
    private String putWallSlotCode;

    @NotNull
    private Long pickingOrderId;

    @NotEmpty
    private String transferContainerCode;

    @Override
    public boolean validate() {
        return StringUtils.isNotEmpty(this.putWallSlotCode) || this.pickingOrderId != null;
    }

    public boolean isNeedHandlePutWallSlot() {
        return StringUtils.isNotEmpty(this.putWallSlotCode);
    }
}
