package org.openwes.wes.basic.work_station.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.StationErrorDescEnum;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.openwes.wes.api.basic.constants.PutWallStatusEnum;

import java.util.List;

/**
 * Definition: SomeWhere that operators put items.
 * <p>
 * one work station can only have a put wall. cause one put wall not specify one wall in physic. but presents a lot of walls
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class PutWall {

    private Long id;
    private Long workStationId;
    private String putWallCode;
    private String putWallName;
    private String containerSpecCode;
    private String location;
    private PutWallStatusEnum putWallStatus;
    private Long version;
    private List<PutWallSlot> putWallSlots;

    private boolean deleted;
    private Long deleteTime;

    private boolean enable;

    public void enable() {
        this.enable = true;
    }

    public void disable() {
        this.enable = false;
    }

    public void delete() {
        if (this.putWallStatus != PutWallStatusEnum.IDLE) {
            throw new IllegalStateException("put wall status is not IDLE, cannot delete it");
        }
        this.deleted = true;
        this.deleteTime = System.currentTimeMillis();
    }

    public void occupy() {
        log.info("work station: {} put wall: {} occupy", this.workStationId, this.putWallCode);

        if (!this.enable || this.deleted) {
            throw new IllegalStateException("put wall is disabled or deleted, cannot occupy");
        }

        if (this.putWallStatus == PutWallStatusEnum.WORKING) {
            throw new IllegalStateException("put wall status is working, cannot occupy");
        }

        this.putWallStatus = PutWallStatusEnum.WORKING;
    }

    public void release() {
        log.info("work station: {} put wall: {} release", this.workStationId, this.putWallCode);

        this.putWallSlots.forEach(slot -> {
            if (PutWallSlotStatusEnum.IDLE != slot.getPutWallSlotStatus()) {
                throw WmsException.throwWmsException(StationErrorDescEnum.STATION_HAS_TASK_ERROR);
            }
        });

        this.putWallStatus = PutWallStatusEnum.IDLE;
    }

    public void updateSlots(List<PutWallSlot> existSlots) {
        log.info("work station: {} put wall: {} update slots", this.workStationId, this.putWallCode);

        if (this.putWallStatus != PutWallStatusEnum.IDLE) {
            throw new IllegalStateException("put wall status is not IDLE,  can't update slots");
        }

        if (ObjectUtils.isEmpty(existSlots)) {
            return;
        }

        existSlots.forEach(existSlot -> {
            this.putWallSlots.forEach(putWallSlot -> {
                if (existSlot.getPutWallSlotCode().equals(putWallSlot.getPutWallSlotCode())) {
                    putWallSlot.update(existSlot);
                }
            });
        });

    }

    public void initial() {
        this.putWallStatus = PutWallStatusEnum.IDLE;
    }
}
