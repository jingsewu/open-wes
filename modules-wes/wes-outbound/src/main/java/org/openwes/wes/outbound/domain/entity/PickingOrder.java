package org.openwes.wes.outbound.domain.entity;

import com.google.common.collect.Lists;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.outbound.constants.PickingOrderDetailStatusEnum;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.api.outbound.event.OutboundPlanOrderDispatchedEvent;
import org.openwes.wes.api.outbound.event.OutboundPlanOrderPickingEvent;
import org.openwes.wes.api.outbound.event.PickingOrderCompleteEvent;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.List;
import java.util.Map;

@Data
@Accessors(chain = true)
@Slf4j
public class PickingOrder {

    private Long id;

    private String warehouseCode;

    private Long warehouseAreaId;

    private String waveNo;

    private String pickingOrderNo;

    private int priority;

    private boolean shortOutbound;

    private PickingOrderStatusEnum pickingOrderStatus;

    // true if this picking order is reallocated from another picking order that short picked and hasn't enough stocks in the area
    private boolean isReallocatedOrder;

    private List<PickingOrderDetail> details;

    /**
     * one picking order can be assigned to multiple station slot
     * <p>
     * Key is the station id
     * Value is the put wall slot code
     */
    private Map<Long, String> assignedStationSlot;

    private Long version;

    private String receivedUserAccount;
    private boolean allowReceive;

    public void dispatch(Map<Long, String> assignedStationSlot) {

        log.info("picking order id: {}, orderNo: {} dispatch to {}", this.id, this.pickingOrderNo, assignedStationSlot);
        if (this.pickingOrderStatus != PickingOrderStatusEnum.NEW) {
            throw new IllegalStateException("picking order status is not NEW, can't be dispatched");
        }
        this.assignedStationSlot = assignedStationSlot;
        this.pickingOrderStatus = PickingOrderStatusEnum.DISPATCHED;

        List<Long> outboundPlanOrderIds = this.details.stream()
                .map(PickingOrderDetail::getOutboundOrderPlanId)
                .distinct().toList();
        DomainEventPublisher.sendAsyncDomainEvent(new OutboundPlanOrderDispatchedEvent().setOutboundPlanOrderIds(outboundPlanOrderIds));
    }

    public void cancel() {

        log.info("picking order id: {}, orderNo: {} canceled", this.id, this.pickingOrderNo);
        if (this.pickingOrderStatus != PickingOrderStatusEnum.NEW) {
            throw new IllegalStateException("picking order status is not NEW, can't be canceled");
        }
        this.details.forEach(PickingOrderDetail::cancel);
        this.pickingOrderStatus = PickingOrderStatusEnum.CANCELED;
    }

    public void picking(Integer operatedQty, Long detailId) {

        log.info("picking order id: {}, orderNo: {} ,detailId: {} picking with pickingQty: {}",
                this.id, this.pickingOrderNo, detailId, operatedQty);

        PickingOrderDetail pickingOrderDetail = this.details.stream().filter(v -> v.getId().equals(detailId)).findFirst().orElseThrow();
        pickingOrderDetail.picking(operatedQty);

        OutboundPlanOrderPickingEvent.PickingDetail pickingDetail = new OutboundPlanOrderPickingEvent.PickingDetail().setOperatedQty(operatedQty)
                .setOutboundOrderDetailId(pickingOrderDetail.getOutboundOrderPlanDetailId())
                .setOutboundOrderId(pickingOrderDetail.getOutboundOrderPlanId());
        DomainEventPublisher.sendSyncDomainEvent(new OutboundPlanOrderPickingEvent().setPickingDetails(Lists.newArrayList(pickingDetail)));

        if (this.details.stream().allMatch(v -> v.getPickingOrderDetailStatus() == PickingOrderDetailStatusEnum.PICKED)) {
            this.pickingOrderStatus = PickingOrderStatusEnum.PICKED;
            DomainEventPublisher.sendAsyncDomainEvent(new PickingOrderCompleteEvent().setPickingOrderIds(Lists.newArrayList(this.id)));
        } else {
            this.pickingOrderStatus = PickingOrderStatusEnum.PICKING;
        }
    }

    public void reportAbnormal(Integer abnormalQty, Long detailId) {

        log.info("picking order id: {}, orderNo: {}, detailId: {} report abnormal with abnormal qty: {}",
                this.id, this.pickingOrderNo, detailId, abnormalQty);

        this.details.stream().filter(v -> v.getId().equals(detailId)).forEach(detail -> detail.reportAbnormal(abnormalQty));
    }

    public void reallocateAbnormal(Integer allocatedQty, Long detailId) {

        log.info("picking order id: {}, orderNo: {}, detailId: {} reallocate abnormal with allocated qty: {}",
                this.id, this.pickingOrderNo, detailId, allocatedQty);

        this.details.stream().filter(v -> v.getId().equals(detailId)).forEach(detail -> detail.reallocateAbnormal(allocatedQty));
    }

    public void shortPicking(Integer shortQty, Long detailId) {

        log.info("picking order id: {}, orderNo: {} ,detailId: {} short picking with pickingQty: {}",
                this.id, this.pickingOrderNo, detailId, shortQty);

        if (!this.shortOutbound) {
            return;
        }
        this.details.stream().filter(v -> v.getId().equals(detailId)).forEach(detail -> detail.shortPicking(shortQty));
        if (this.details.stream().allMatch(v -> v.getPickingOrderDetailStatus() == PickingOrderDetailStatusEnum.PICKED)) {
            this.pickingOrderStatus = PickingOrderStatusEnum.PICKED;
            DomainEventPublisher.sendAsyncDomainEvent(new PickingOrderCompleteEvent().setPickingOrderIds(Lists.newArrayList(this.id)));
        } else {
            this.pickingOrderStatus = PickingOrderStatusEnum.PICKING;
        }

    }

    public void allowReceive() {

        log.info("picking order id: {}, orderNo: {} allow receive", this.id, this.pickingOrderNo);

        if (this.allowReceive) {
            throw new IllegalStateException("picking order already allow receive");
        }
        this.allowReceive = true;
    }

    public void receive(String receivedUserAccount) {

        log.info("picking order id: {}, orderNo: {} receive with receivedUserAccount: {}", this.id, this.pickingOrderNo, receivedUserAccount);

        this.receivedUserAccount = receivedUserAccount;
        this.pickingOrderStatus = PickingOrderStatusEnum.PICKING;
    }

    public PickingOrder copyAndNew(Long warehouseAreaId) {
        PickingOrder newPickingOrder = new PickingOrder();
        BeanUtils.copyProperties(this, newPickingOrder, "id", "version", "assignedStationSlot", "receivedUserAccount");

        newPickingOrder.setPickingOrderNo(OrderNoGenerator.generationPickingOrderNo());
        newPickingOrder.setPickingOrderStatus(PickingOrderStatusEnum.NEW);
        newPickingOrder.setWarehouseAreaId(warehouseAreaId);
        newPickingOrder.setDetails(Lists.newArrayList());
        newPickingOrder.setReallocatedOrder(true);
        return newPickingOrder;
    }
}
