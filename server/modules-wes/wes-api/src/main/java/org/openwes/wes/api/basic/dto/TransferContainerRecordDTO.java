package org.openwes.wes.api.basic.dto;

import lombok.Data;
import org.openwes.wes.api.task.constants.TransferContainerRecordStatusEnum;

@Data
public class TransferContainerRecordDTO {

    private Long id;
    private Long pickingOrderId;
    private String transferContainerCode;
    private Long workStationId;
    private String putWallSlotCode;
    private TransferContainerRecordStatusEnum transferContainerStatus;
    private String warehouseCode;

    private Long sealTime;
    private String destination;
    private Long version;
}
