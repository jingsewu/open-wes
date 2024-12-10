package org.openwes.wes.basic.work_station.domain.entity;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.task.dto.SplitContainerDTO;

@Slf4j
@Data
public class PutWallSlot {

    private Long id;
    private Long workStationId;
    private Long putWallId;
    private String putWallCode;
    private String putWallSlotCode;
    private String ptlTag;

    private String face;

    private String level;
    private String bay;
    private Integer locLevel;
    private Integer locBay;

    private boolean enable;

    private Long pickingOrderId = 0L;

    private PutWallSlotStatusEnum putWallSlotStatus;

    private String transferContainerCode;
    private Long transferContainerRecordId;
    private Long version;

    public void initPutWallSlot(Long putWallId, String putWallCode, Long workStationId) {
        this.workStationId = workStationId;
        this.putWallId = putWallId;
        this.putWallCode = putWallCode;
        this.putWallSlotStatus = PutWallSlotStatusEnum.IDLE;
        this.enable = true;
    }

    public void assignOrder(Long orderId) {
        log.info("workStation: {} putWall: {} slot: {} assign order: {}",
                this.workStationId, this.putWallCode, this.putWallSlotCode, orderId);

        if (this.pickingOrderId != null && this.pickingOrderId > 0) {
            throw new IllegalStateException("put wall slot has a picking order ,can't assign");
        }

        if (orderId == null) {
            throw new IllegalStateException("assigned orderId can not be empty");
        }

        if (this.putWallSlotStatus != PutWallSlotStatusEnum.IDLE) {
            throw new IllegalStateException("put wall slot status is not IDLE,  can't assign order");
        }

        this.pickingOrderId = orderId;
        this.putWallSlotStatus = PutWallSlotStatusEnum.WAITING_BINDING;
    }

    public void bindContainer(String containerCode, Long transferContainerRecordId) {
        log.info("work station: {} putWall: {} slot: {} bind container: {} transferContainerRecordId: {}",
                this.workStationId, this.putWallCode, putWallSlotCode, containerCode, transferContainerRecordId);

        if (this.putWallSlotStatus != PutWallSlotStatusEnum.WAITING_BINDING) {
            throw new IllegalStateException("PutWallSlot is not WAITING_BINDING, cannot bind container");
        }
        this.transferContainerCode = containerCode;
        this.transferContainerRecordId = transferContainerRecordId;
        this.putWallSlotStatus = PutWallSlotStatusEnum.BOUND;
    }

    public void unBindContainer() {
        log.info("work station: {} putWall: {} slot: {} unbind container", this.workStationId, this.putWallCode, putWallSlotCode);

        if (this.putWallSlotStatus != PutWallSlotStatusEnum.BOUND) {
            throw new IllegalStateException("PutWall Slot is not BOUND, cannot unbind container");
        }
        this.transferContainerCode = null;
        this.transferContainerRecordId = null;
        this.putWallSlotStatus = PutWallSlotStatusEnum.WAITING_BINDING;
    }

    public void sealContainer() {
        log.info("work station: {} putWall: {} slot: {} seal container", this.workStationId, this.putWallCode, putWallSlotCode);

        if (this.putWallSlotStatus != PutWallSlotStatusEnum.WAITING_SEAL) {
            throw new IllegalStateException("PutWallSlot is not WAITING_SEAL, cannot seal container");
        }
        this.pickingOrderId = null;
        this.transferContainerCode = null;
        this.transferContainerRecordId = null;
        this.putWallSlotStatus = PutWallSlotStatusEnum.IDLE;
    }

    public void remindToSealContainer(Long pickingOrderId) {
        log.info("work station: {} putWall: {} slot: {} reminded to seal picking order: {}", this.workStationId, this.putWallCode,
                this.putWallSlotCode, pickingOrderId);

        if (this.putWallSlotStatus != PutWallSlotStatusEnum.BOUND) {
            throw new IllegalStateException("put wall slot status is not BOUND,  can't remind to seal container");
        }

        this.putWallSlotStatus = PutWallSlotStatusEnum.WAITING_SEAL;
    }

    public void splitContainer(SplitContainerDTO splitContainerDTO) {
        log.info("work station: {} putWall: {} slot: {} split container, after split slot status {}",
                this.workStationId, this.putWallCode, this.putWallSlotCode, splitContainerDTO.getPutWallSlotStatusAfterSplit());

        if (PutWallSlotStatusEnum.BOUND != this.putWallSlotStatus) {
            throw new IllegalStateException("put wall slot status is not BOUND,  can't split container");
        }

        this.putWallSlotStatus = splitContainerDTO.getPutWallSlotStatusAfterSplit();

        // when full split and picking order is completed
        if (PutWallSlotStatusEnum.IDLE == this.putWallSlotStatus) {
            this.pickingOrderId = null;
        }

        this.transferContainerCode = null;
        this.transferContainerRecordId = null;
    }

    public void update(PutWallSlot existSlot) {
        this.id = existSlot.getId();
    }
}
