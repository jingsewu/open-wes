package org.openwes.wes.api.basic.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PutWallSlotAssignedDTO implements Serializable {

    private Long workStationId;
    private String putWallSlotCode;
    private String ptlTag;
}
