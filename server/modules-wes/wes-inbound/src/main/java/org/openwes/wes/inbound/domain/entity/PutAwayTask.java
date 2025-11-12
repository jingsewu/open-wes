package org.openwes.wes.inbound.domain.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.domain.event.AggregatorRoot;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.basic.event.ContainerLocationUpdateEvent;
import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import org.openwes.wes.api.inbound.constants.PutAwayTaskTypeEnum;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class PutAwayTask extends AggregatorRoot {

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
        this.id = SnowflakeUtils.generateId();
        this.taskNo = OrderNoGenerator.generationPutAwayTaskNo();
        this.taskStatus = PutAwayTaskStatusEnum.NEW;

        if (ObjectUtils.isEmpty(this.putAwayTaskDetails)) {
            throw new IllegalArgumentException("putAwayTaskDetails is empty");
        }
        log.info("put away task id: {} taskNo: {} initialize", this.id, this.taskNo);
        this.putAwayTaskDetails.forEach(detail -> detail.setPutAwayTaskId(this.id));
    }

    public void complete(String locationCode) {

        log.info("put away task id: {} taskNo: {} complete and location: {}", this.id, this.taskNo, locationCode);

        if (this.taskStatus == PutAwayTaskStatusEnum.PUTTED_AWAY) {
            throw new IllegalStateException("put away task has been completed");
        }
        this.taskStatus = PutAwayTaskStatusEnum.PUTTED_AWAY;

        this.locationCode = locationCode;

        this.addAsynchronousDomainEvents(new ContainerLocationUpdateEvent()
                .setLocationCode(this.locationCode).setWarehouseAreaId(this.warehouseAreaId)
                .setWarehouseCode(this.warehouseCode).setContainerCode(this.containerCode));
    }
}
