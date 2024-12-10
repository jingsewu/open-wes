package org.openwes.wes.inbound.domain.entity;

import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.api.inbound.constants.EmptyContainerInboundWayEnum;
import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.List;

@Data
public class EmptyContainerInboundOrder {

    private Long id;

    private String orderNo;

    private String warehouseCode;

    private EmptyContainerInboundWayEnum inboundWay;

    private int planCount;

    private PutAwayTaskStatusEnum inboundStatus;

    private List<EmptyContainerInboundOrderDetail> details;

    public EmptyContainerInboundOrder(String warehouseCode, EmptyContainerInboundWayEnum inboundWay, List<EmptyContainerInboundOrderDetail> details) {
        this.warehouseCode = warehouseCode;
        this.inboundWay = inboundWay;
        this.details = details;
    }

    public void initial() {
        orderNo = OrderNoGenerator.generationEmptyContainerInboundOrderNo();
        if (CollectionUtils.isEmpty(details)) {
            planCount = 0;
        } else {
            planCount = details.size();
        }
        inboundStatus = PutAwayTaskStatusEnum.NEW;

        // 初始化明细
        if (CollectionUtils.isNotEmpty(details)) {
            for (EmptyContainerInboundOrderDetail detail : details) {
                detail.initial();
            }
        }
    }

    public void complete(Collection<Long> detailIds) {

        details.stream().filter(v -> detailIds.contains(v.getId())).forEach(EmptyContainerInboundOrderDetail::complete);

        if (details.stream().allMatch(po -> PutAwayTaskStatusEnum.PUTTED_AWAY == po.getInboundStatus())) {
            this.inboundStatus = PutAwayTaskStatusEnum.PUTTED_AWAY;
        } else {
            this.inboundStatus = PutAwayTaskStatusEnum.PUTTING_AWAY;
        }
    }

    public void removeUnCompleteDetails() {
        this.details.removeIf(v -> v.getInboundStatus() != PutAwayTaskStatusEnum.PUTTED_AWAY);
    }
}
