package org.openwes.wes.task.domain.entity;

import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.task.constants.TransferContainerRecordStatusEnum;
import org.openwes.wes.api.task.dto.BindContainerDTO;
import org.openwes.wes.api.task.event.TransferContainerSealedEvent;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;

@Data
@NoArgsConstructor
@Slf4j
public class TransferContainerRecord implements Serializable {

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

    public TransferContainerRecord(BindContainerDTO bindContainerDTO, Long pickingOrderId) {
        this.transferContainerCode = bindContainerDTO.getContainerCode();
        this.putWallSlotCode = bindContainerDTO.getPutWallSlotCode();
        this.warehouseCode = bindContainerDTO.getWarehouseCode();
        this.workStationId = bindContainerDTO.getWorkStationId();
        this.pickingOrderId = pickingOrderId;
        this.sealTime = 0L;
        this.transferContainerStatus = TransferContainerRecordStatusEnum.BOUNDED;
    }

    public TransferContainerRecord(String transferContainerCode, Long workStationId, String putWallSlotCode, String warehouseCode,
                                   Long pickingOrderId, String destination) {

        this.transferContainerCode = transferContainerCode;
        this.putWallSlotCode = putWallSlotCode;
        this.warehouseCode = warehouseCode;
        this.workStationId = workStationId;
        this.destination = destination;
        this.pickingOrderId = pickingOrderId;
        this.sealTime = 0L;
        this.transferContainerStatus = TransferContainerRecordStatusEnum.BOUNDED;
    }

    public void seal() {

        log.info("seal transfer container record id: {}, transfer container code: {}", this.id, this.transferContainerCode);

        if (this.transferContainerStatus != TransferContainerRecordStatusEnum.BOUNDED) {
            throw new IllegalStateException("transfer container record status is not bounded");
        }

        this.sealTime = System.currentTimeMillis();
        this.transferContainerStatus = TransferContainerRecordStatusEnum.SEALED;
        DomainEventPublisher.sendAsyncDomainEvent(new TransferContainerSealedEvent()
                .setTransferContainerRecordId(this.id).setWarehouseCode(this.warehouseCode));
    }
}
