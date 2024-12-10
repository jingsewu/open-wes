package org.openwes.wes.api.task.dto;

import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class TransferContainerDTO implements Serializable {

    private Long id;
    private String transferContainerCode;
    private String warehouseCode;
    private String containerSpecCode;
    private Long warehouseAreaId;
    private String locationCode;

    private boolean virtualContainer;
    private TransferContainerStatusEnum transferContainerStatus;
    private Long lockedTime;

    private List<Long> currentPeriodRelateRecordIds;
    private Long version;
}
