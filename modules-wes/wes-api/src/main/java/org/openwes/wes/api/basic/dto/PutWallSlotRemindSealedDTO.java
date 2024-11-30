package org.openwes.wes.api.basic.dto;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PutWallSlotRemindSealedDTO {
    private Long workStationId;
    private String putWallSlotCode;
    private String ptlTag;
}
