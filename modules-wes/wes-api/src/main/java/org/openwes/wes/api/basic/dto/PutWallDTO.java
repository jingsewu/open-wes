package org.openwes.wes.api.basic.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.openwes.common.utils.validate.IValidate;
import org.openwes.wes.api.basic.constants.PutWallDisplayOrderEnum;
import org.openwes.wes.api.basic.constants.PutWallStatusEnum;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PutWallDTO implements IValidate, Serializable {

    private Long id;

    @NotNull
    private Long workStationId;
    @NotEmpty
    private String putWallCode;
    @NotEmpty
    private String putWallName;
    @NotEmpty
    private String containerSpecCode;
    @NotEmpty
    private List<PutWallSlotDTO> putWallSlots;

    private String location;

    private boolean enable;

    private PutWallDisplayOrderEnum displayOrder;

    private Long version;

    private PutWallStatusEnum putWallStatus;

    private boolean active;

    @Override
    public boolean validate() {
        return putWallSlots.stream().map(PutWallSlotDTO::getPutWallSlotCode).distinct().toList().size() == putWallSlots.size()
                && putWallSlots.stream().map(PutWallSlotDTO::getPtlTag).distinct().toList().size() == putWallSlots.size();
    }
}
