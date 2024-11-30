package org.openwes.wes.inbound.domain.entity;

import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import org.openwes.wes.api.inbound.constants.PutAwayTaskTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

@Data
@Slf4j
public class PutAwayTask {

    private Long id;

    private String taskNo;
    private PutAwayTaskTypeEnum taskType;

    private String warehouseCode;
    private String containerCode;
    private String containerSpecCode;

    private Long warehouseAreaId;
    private Long workStationId;

    private String locationCode;
    private List<PutAwayTaskDetail> putAwayTaskDetails;

    private Map<String, Object> extendFields;

    private PutAwayTaskStatusEnum taskStatus;

    private Long version;

    public void initialize() {
        taskNo = OrderNoGenerator.generationPutAwayTaskNo();
        this.taskStatus = PutAwayTaskStatusEnum.NEW;
    }

    public void complete(String locationCode) {

        log.info("put away task: {} complete and location: {}", taskNo, locationCode);

        if (this.taskStatus == PutAwayTaskStatusEnum.PUTTED_AWAY) {
            throw new IllegalStateException("put away task has been completed");
        }
        this.taskStatus = PutAwayTaskStatusEnum.PUTTED_AWAY;

        this.locationCode = locationCode;
    }
}
