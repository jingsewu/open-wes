package org.openwes.wes.outbound.domain.entity;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.common.utils.id.SnowflakeUtils;
import org.openwes.domain.event.AggregatorRoot;
import org.openwes.plugin.api.dto.event.LifeCycleStatusChangeEvent;
import org.openwes.wes.api.outbound.constants.PickingOrderDetailStatusEnum;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import org.openwes.wes.api.outbound.event.*;

import java.util.List;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@Slf4j
public class PickingOrder extends AggregatorRoot {

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

    public static PickingOrder create(
            Integer priority,
            boolean shortOutbound,
            String warehouseCode,
            Long warehouseAreaId,
            String waveNo,
            List<PickingOrderDetail> details,
            boolean allowReceive) {

        PickingOrder order = new PickingOrder();
        order.id = SnowflakeUtils.generateId();
        order.pickingOrderNo = OrderNoGenerator.generationPickingOrderNo();
        order.priority = priority;
        order.shortOutbound = shortOutbound;
        order.warehouseCode = warehouseCode;
        order.warehouseAreaId = warehouseAreaId;
        order.waveNo = waveNo;
        order.details = details;
        order.allowReceive = allowReceive;

        if (details != null) {
            details.forEach(detail -> detail.setPickingOrderId(order.id));
        }

        order.addLifecycleEvent(new LifeCycleStatusChangeEvent().setEntityId(order.getId()).setNewStatus(PickingOrderStatusEnum.NEW.name()));

        return order;
    }

    public void dispatch(Map<Long, String> assignedStationSlot) {

        log.info("picking order id: {}, orderNo: {} dispatch to {}", this.id, this.pickingOrderNo, assignedStationSlot);
        if (this.pickingOrderStatus != PickingOrderStatusEnum.NEW) {
            throw new IllegalStateException("picking order status is not NEW, can't be dispatched");
        }
        this.assignedStationSlot = assignedStationSlot;
        this.pickingOrderStatus = PickingOrderStatusEnum.DISPATCHED;

        this.addAsynchronousDomainEvents(new PickingOrderDispatchedEvent(this.id));
        this.addLifecycleEvent(new LifeCycleStatusChangeEvent().setEntityId(this.id).setNewStatus(PickingOrderStatusEnum.DISPATCHED.name()));
    }

    public void cancel() {

        log.info("picking order id: {}, orderNo: {} canceled", this.id, this.pickingOrderNo);
        if (this.pickingOrderStatus != PickingOrderStatusEnum.NEW) {
            throw new IllegalStateException("picking order status is not NEW, can't be canceled");
        }
        this.details.forEach(PickingOrderDetail::cancel);
        this.pickingOrderStatus = PickingOrderStatusEnum.CANCELED;
        addLifecycleEvent(new LifeCycleStatusChangeEvent().setEntityId(this.id).setNewStatus(PickingOrderStatusEnum.CANCELED.name()));
    }

    public void picking(Integer operatedQty, Long detailId) {

        log.info("picking order id: {}, orderNo: {} ,detailId: {} picking with pickingQty: {}",
                this.id, this.pickingOrderNo, detailId, operatedQty);

        PickingOrderDetail pickingOrderDetail = this.details.stream().filter(v -> v.getId().equals(detailId)).findFirst().orElseThrow();
        pickingOrderDetail.picking(operatedQty);

        PickingOrderPickedEvent.PickingDetail pickingDetail = new PickingOrderPickedEvent.PickingDetail()
                .setOperatedQty(operatedQty)
                .setOutboundOrderDetailId(pickingOrderDetail.getOutboundOrderPlanDetailId())
                .setOutboundOrderId(pickingOrderDetail.getOutboundOrderPlanId());

        this.addSynchronizationEvents(new PickingOrderPickedEvent(this.id, pickingDetail));

        if (this.details.stream().allMatch(v -> v.getPickingOrderDetailStatus() == PickingOrderDetailStatusEnum.PICKED)) {
            this.pickingOrderStatus = PickingOrderStatusEnum.PICKED;
            this.addAsynchronousDomainEvents(new PickingOrderCompletionEvent(this.id));
            this.addAsynchronousDomainEvents(new PickingOrderRemindSealContainerEvent(this.id,this.warehouseAreaId,this.assignedStationSlot));
        } else {
            this.pickingOrderStatus = PickingOrderStatusEnum.PICKING;
        }
    }

    public void reportAbnormal(Integer abnormalQty, Long detailId) {

        log.info("picking order id: {}, orderNo: {}, detailId: {} report abnormal with abnormal qty: {}",
                this.id, this.pickingOrderNo, detailId, abnormalQty);

        this.details.stream().filter(v -> v.getId().equals(detailId))
                .forEach(detail -> detail.reportAbnormal(abnormalQty));
    }

    public void reallocateAbnormal(Integer allocatedQty, Long detailId) {

        log.info("picking order id: {}, orderNo: {}, detailId: {} reallocate abnormal with allocated qty: {}",
                this.id, this.pickingOrderNo, detailId, allocatedQty);

        this.details.stream().filter(v -> v.getId().equals(detailId))
                .forEach(detail -> detail.reallocateAbnormal(allocatedQty));
    }

    public void shortPicking(Integer shortQty, Long detailId) {

        log.info("picking order id: {}, orderNo: {} ,detailId: {} short picking with pickingQty: {}",
                this.id, this.pickingOrderNo, detailId, shortQty);

        if (!this.shortOutbound) {
            return;
        }
        this.details.stream().filter(v -> v.getId().equals(detailId))
                .forEach(detail -> detail.shortPicking(shortQty));
        if (this.details.stream().allMatch(v -> v.getPickingOrderDetailStatus() == PickingOrderDetailStatusEnum.PICKED)) {
            this.pickingOrderStatus = PickingOrderStatusEnum.PICKED;
            this.addAsynchronousDomainEvents(new PickingOrderCompletionEvent(this.id));
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

    public static PickingOrder copyAndNew(PickingOrder source, Long warehouseAreaId,
                                          @NotEmpty List<PickingOrderDetail> pickingOrderDetails) {
        PickingOrder newOrder = new PickingOrder();

        newOrder.warehouseCode = source.warehouseCode;
        newOrder.waveNo = source.waveNo;
        newOrder.priority = source.priority;
        newOrder.shortOutbound = source.shortOutbound;
        newOrder.isReallocatedOrder = source.isReallocatedOrder;
        newOrder.allowReceive = source.allowReceive;

        newOrder.id = SnowflakeUtils.generateId();
        newOrder.pickingOrderNo = OrderNoGenerator.generationPickingOrderNo();
        newOrder.pickingOrderStatus = PickingOrderStatusEnum.NEW;
        newOrder.warehouseAreaId = warehouseAreaId;
        newOrder.details = pickingOrderDetails;
        newOrder.isReallocatedOrder = true;
        newOrder.version = null;
        newOrder.assignedStationSlot = null;
        newOrder.receivedUserAccount = null;
        newOrder.details.forEach(detail -> detail.setPickingOrderId(newOrder.id));

        return newOrder;
    }

    public void improvePriority(Integer priority) {

        log.info("picking order id: {} , orderNo: {} improve priority: {}", this.id, this.pickingOrderNo, priority);

        if (this.priority >= priority) {
            return;
        }
        this.priority = priority;

        this.addAsynchronousDomainEvents(new PickingOrderImprovedPriorityEvent(this.id, this.priority));
    }
}
