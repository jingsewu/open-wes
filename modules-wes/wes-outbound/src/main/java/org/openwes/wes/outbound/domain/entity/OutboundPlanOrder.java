package org.openwes.wes.outbound.domain.entity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderDetailStatusEnum;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.openwes.wes.api.outbound.event.OutboundPlanOrderAssignedEvent;
import org.openwes.wes.api.outbound.event.OutboundPlanOrderCompleteEvent;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Slf4j
public class OutboundPlanOrder {

    private Long id;

    private String warehouseCode;

    private String customerWaveNo;

    private String waveNo;
    private String customerOrderNo;
    private String customerOrderType;
    private String currierCode;
    private String waybillNo;
    private String origPlatformCode;

    private Long expiredTime;
    private Integer priority = 0;

    private String orderNo;

    private Integer skuKindNum;
    private Integer totalQty;

    private OutboundPlanOrderStatusEnum outboundPlanOrderStatus;

    private boolean shortOutbound;
    private boolean shortWaiting;

    private boolean abnormal;
    private String abnormalReason;

    private Map<String, String> reservedFields;

    private List<OutboundPlanOrderDetail> details;

    private Set<String> destinations;
    private Long version;

    public void initialize() {
        Set<String> skuSet = Sets.newHashSet();
        int total = 0;
        for (OutboundPlanOrderDetail orderDetail : this.details) {
            skuSet.add(orderDetail.getSkuCode());
            total += orderDetail.getQtyRequired();
        }
        this.totalQty = total;
        this.skuKindNum = skuSet.size();
        this.orderNo = OrderNoGenerator.generationOutboundPlanOrderNo();
    }

    public void initSkuInfo(Set<SkuMainDataDTO> skuMainDataDTOS) {
        Map<SkuMainDataDTO.Key, SkuMainDataDTO> skuMap = skuMainDataDTOS.stream()
                .collect(Collectors.toMap(
                        k -> new SkuMainDataDTO.Key(k.getSkuCode(), k.getOwnerCode(), k.getWarehouseCode()),
                        v -> v,
                        (existing, replacement) -> existing
                ));

        this.details.forEach(v -> {
            SkuMainDataDTO.Key key = new SkuMainDataDTO.Key(v.getSkuCode(), v.getOwnerCode(), this.warehouseCode);
            SkuMainDataDTO skuMainDataDTO = skuMap.get(key);
            if (skuMainDataDTO != null) {
                v.setSkuId(skuMainDataDTO.getId());
                v.setSkuName(skuMainDataDTO.getSkuName());
            }
        });
    }

    public boolean preAllocate(List<OutboundPreAllocatedRecord> planPreAllocatedRecords) {
        Integer totalPreAllocated = planPreAllocatedRecords
                .stream().map(OutboundPreAllocatedRecord::getQtyPreAllocated).reduce(Integer::sum).orElse(0);

        this.details.forEach(detail -> planPreAllocatedRecords.forEach(planPreAllocatedRecord -> {
            if (detail.getId().equals(planPreAllocatedRecord.getOutboundPlanOrderDetailId())) {
                detail.preAllocate(planPreAllocatedRecord);
            }
        }));

        Integer totalRequired = this.details.stream().map(OutboundPlanOrderDetail::getQtyRequired).reduce(Integer::sum).orElse(0);

        if (totalPreAllocated > totalRequired) {
            throw new IllegalArgumentException("pre allocated qty is bigger than total required");
        }

        if (totalRequired.equals(totalPreAllocated)) {
            log.info("outbound plan order id: {} orderNo: {} assigned", this.id, this.orderNo);
            this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.ASSIGNED;
            DomainEventPublisher.sendAsyncDomainEvent(new OutboundPlanOrderAssignedEvent().setWarehouseCode(this.warehouseCode).setOutboundPlanOrderId(this.id));
            return true;
        }

        if (shortWaiting) {
            log.info("outbound plan order id: {} orderNo: {} assigned but it is short waiting", this.id, this.orderNo);
            this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.SHORT_WAITING;
            return true;
        }

        if (shortOutbound && CollectionUtils.isNotEmpty(planPreAllocatedRecords)) {
            log.info("outbound plan order id: {} orderNo: {} assigned but is short pre allocated.", this.id, this.orderNo);
            this.abnormal = true;
            this.abnormalReason = "short preAllocate";
            this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.ASSIGNED;
            DomainEventPublisher.sendAsyncDomainEvent(new OutboundPlanOrderAssignedEvent().setWarehouseCode(this.warehouseCode).setOutboundPlanOrderId(this.id));
            return true;
        }

        shortComplete();
        return false;
    }

    public void cancel() {

        log.info("outbound plan order id: {}, orderNo: {} cancel", this.id, this.orderNo);

        if (OutboundPlanOrderStatusEnum.cancellability(this.outboundPlanOrderStatus)) {
            this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.CANCELED;
            this.details.forEach(OutboundPlanOrderDetail::cancel);
        }
    }

    public void dispatch() {

        log.info("outbound plan order id: {}, orderNo: {} dispatch", this.id, this.orderNo);

        if (this.outboundPlanOrderStatus != OutboundPlanOrderStatusEnum.ASSIGNED) {
            return;
        }
        this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.DISPATCHED;
    }

    public void picking(Integer operatedQty, Long outboundOrderDetailId) {

        log.info("outbound plan order id: {}, orderNo: {}, detail: {} picking with pickingQty: {}", this.id, this.orderNo, outboundOrderDetailId, operatedQty);

        details.stream().filter(v -> v.getId().equals(outboundOrderDetailId))
                .forEach(detail -> detail.picking(operatedQty));

        if (details.stream().allMatch(v -> v.getOutboundPlanOrderDetailStatus() == OutboundPlanOrderDetailStatusEnum.PICKED)) {
            this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.PICKED;
            DomainEventPublisher.sendAsyncDomainEvent(new OutboundPlanOrderCompleteEvent().setOutboundPlanOrderIds(Lists.newArrayList(this.id)));
        } else {
            this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.PICKING;
        }
    }

    public boolean shortComplete() {

        log.info("outbound plan order id: {}, orderNo: {} short complete", this.id, this.orderNo);

        if (this.outboundPlanOrderStatus == OutboundPlanOrderStatusEnum.PICKED) {
            log.error("outbound plan order id: {} short complete but it's status is PICKED", this.id);
            return false;
        }

        this.outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.PICKED;
        this.abnormal = true;
        this.abnormalReason = "short complete";

        this.details.stream().filter(v -> v.getOutboundPlanOrderDetailStatus() != OutboundPlanOrderDetailStatusEnum.PICKED)
                .forEach(OutboundPlanOrderDetail::shortComplete);

        DomainEventPublisher.sendAsyncDomainEvent(new OutboundPlanOrderCompleteEvent().setOutboundPlanOrderIds(Lists.newArrayList(this.id)));

        return true;
    }
}
