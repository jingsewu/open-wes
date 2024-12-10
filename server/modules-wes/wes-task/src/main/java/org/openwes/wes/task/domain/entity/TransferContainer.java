package org.openwes.wes.task.domain.entity;

import com.google.common.collect.Lists;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;

import java.io.Serializable;
import java.util.List;

import static org.openwes.wes.api.task.constants.TransferContainerStatusEnum.*;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Slf4j
public class TransferContainer implements Serializable {

    private Long id;
    private String transferContainerCode;
    private String warehouseCode;
    private String containerSpecCode;
    private Long warehouseAreaId;
    private String locationCode;

    private boolean virtualContainer;
    private TransferContainerStatusEnum transferContainerStatus;
    private Long lockedTime;

    // 表示一个周期内关联的周转容器记录(TransferContainerRecord).
    // 一个周期表示周转箱状态从空闲 - 占用 - 锁定 - 空闲，即整个完整的生命周期
    // 如果同一个周期内，对周转容器进行多次操作，则这个周期内TransferContainerRecord都会被记录下来
    // 这样回传的时候就可以根据周转容器内的currentPeriodRelateRecordIds进行查询回传
    private List<Long> currentPeriodRelateRecordIds;
    private Long version;

    public void occupy(Long transferContainerRecordId) {
        log.info("occupy transfer container id: {}, transfer container code: {} with transfer container record id: {}",
                this.id, this.transferContainerCode, transferContainerRecordId);
        if (transferContainerStatus != IDLE) {
            throw new IllegalStateException("TransferContainer status is not IDLE");
        }
        currentPeriodRelateRecordIds = Lists.newArrayList(transferContainerRecordId);
        this.transferContainerStatus = OCCUPANCY;
    }

    /**
     * 强行占用周转容器： 应用场景，在接力拣选的情况下，需要对容器进行多次操作
     *
     * @param transferContainerRecordId 周转容器操作记录
     */
    public void forceOccupy(Long transferContainerRecordId) {
        log.info("force occupy transfer container id: {}, " +
                "transfer container code: {}. maybe relay picking?", this.id, this.transferContainerCode);
        if (this.transferContainerStatus == IDLE || ObjectUtils.isEmpty(currentPeriodRelateRecordIds)) {
            throw new IllegalStateException("currentPeriodRelateRecordIds is empty or status is IDLE, you need not to call forceOccupy");
        }
        this.transferContainerStatus = OCCUPANCY;
        currentPeriodRelateRecordIds.add(transferContainerRecordId);
    }

    public void unOccupy() {

        log.info("transfer container id: {}, transfer container code: {} unOccupy", this.id, this.transferContainerCode);

        if (transferContainerStatus != OCCUPANCY) {
            throw new IllegalStateException("TransferContainer status is not OCCUPANCY,can't unOccupy");
        }
        this.transferContainerStatus = IDLE;
    }

    public void lock() {

        log.info("transfer container id: {}, transfer container code: {} lock", this.id, this.transferContainerCode);

        if (transferContainerStatus != OCCUPANCY) {
            throw new IllegalStateException("TransferContainer status is not OCCUPANCY, can't seal");
        }
        this.transferContainerStatus = LOCKED;
        this.lockedTime = System.currentTimeMillis();
    }

    public void unlock() {

        log.info("transfer container id: {}, transfer container code: {} unlock", this.id, this.transferContainerCode);

        if (transferContainerStatus != LOCKED) {
            throw new IllegalStateException("TransferContainer status is not LOCKED ,can't unlock");
        }
        this.transferContainerStatus = IDLE;
        this.lockedTime = 0L;
        this.warehouseAreaId = null;
        this.currentPeriodRelateRecordIds = null;
        this.locationCode = "";
    }

    public void updateLocation(Long warehouseAreaId, String locationCode) {
        this.warehouseAreaId = warehouseAreaId;
        this.locationCode = locationCode;
    }
}
