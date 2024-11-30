package org.openwes.wes.inbound.domain.entity;


import com.google.common.collect.Sets;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.domain.event.DomainEventPublisher;
import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.StorageTypeEnum;
import org.openwes.wes.api.inbound.dto.AcceptRecordDTO;
import org.openwes.wes.api.inbound.event.InboundOrderCompletionEvent;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.openwes.common.utils.exception.code_enum.InboundErrorDescEnum.INBOUND_STATUS_ERROR;

@Data
@Slf4j
public class InboundPlanOrder {

    private Long id;

    private String orderNo;
    private String customerOrderNo;
    private String lpnCode;

    private String warehouseCode;
    private String customerOrderType;

    private StorageTypeEnum storageType;
    private boolean abnormal;

    private String sender;
    private String carrier;
    private String shippingMethod;
    private String trackingNumber;
    private Long estimatedArrivalDate;
    private String remark;

    private Integer skuKindNum;
    private Integer totalQty;
    private Integer totalBox;

    private InboundPlanOrderStatusEnum inboundPlanOrderStatus;

    private Map<String, Object> extendFields;

    private List<InboundPlanOrderDetail> details;

    private Long version;

    public void initialize() {
        this.orderNo = OrderNoGenerator.generationInboundPlanOrderNo();
        this.inboundPlanOrderStatus = InboundPlanOrderStatusEnum.NEW;
        this.totalBox = (this.totalBox == null ? 0 : this.totalBox);
        this.totalQty = (this.totalQty == null ? 0 : this.totalQty);

        Set<String> skuSet = Sets.newHashSet();
        for (InboundPlanOrderDetail inboundPlanOrderDetail : details) {
            skuSet.add(inboundPlanOrderDetail.getSkuCode());
            int box = StringUtils.isNotEmpty(inboundPlanOrderDetail.getBoxNo()) ? 1 : 0;
            this.totalBox += box;
            this.totalQty += inboundPlanOrderDetail.getQtyRestocked();
        }
        this.skuKindNum = skuSet.size();
    }

    public void initSkuInfo(Set<SkuMainDataDTO> skuMainDataDTOS) {
        Map<String, SkuMainDataDTO> skuMap = skuMainDataDTOS.stream()
                .collect(Collectors.toMap(k -> k.getSkuCode() + k.getOwnerCode(), v -> v));
        this.details.forEach(v -> {
            if (skuMap.containsKey(v.getSkuCode() + v.getOwnerCode())) {
                SkuMainDataDTO skuMainDataDTO = skuMap.get(v.getSkuCode() + v.getOwnerCode());
                v.initSkuInfo(skuMainDataDTO);
            }
        });
    }

    public void accept(AcceptRecordDTO acceptRecord) {

        log.info("inbound order: {} detail: {} accept qty: {} abnormal qty: {}",
                this.orderNo, acceptRecord.getInboundPlanOrderDetailId(), acceptRecord.getQtyAccepted(), acceptRecord.getQtyAbnormal());
        this.details.stream()
                .filter(v -> Objects.equals(v.getId(), acceptRecord.getInboundPlanOrderDetailId()))
                .forEach(v -> v.accept(acceptRecord.getQtyAccepted(), acceptRecord.getQtyAbnormal()));

        if (this.inboundPlanOrderStatus == InboundPlanOrderStatusEnum.NEW) {
            this.inboundPlanOrderStatus = InboundPlanOrderStatusEnum.ACCEPTING;
        }
    }

    public void close() {

        log.info("inbound order: {} close", this.orderNo);
        if (this.inboundPlanOrderStatus != InboundPlanOrderStatusEnum.NEW && this.inboundPlanOrderStatus != InboundPlanOrderStatusEnum.ACCEPTING) {
            throw WmsException.throwWmsException(INBOUND_STATUS_ERROR, this.inboundPlanOrderStatus);
        }

        this.inboundPlanOrderStatus = InboundPlanOrderStatusEnum.CLOSED;
        this.details.forEach(InboundPlanOrderDetail::close);
        DomainEventPublisher.sendAsyncDomainEvent(new InboundOrderCompletionEvent().setInboundOrderId(this.id));

    }

    public void cancel() {

        log.info("inbound order: {} cancel", this.orderNo);
        if (inboundPlanOrderStatus != InboundPlanOrderStatusEnum.NEW) {
            throw WmsException.throwWmsException(INBOUND_STATUS_ERROR, this.inboundPlanOrderStatus);
        }
        this.inboundPlanOrderStatus = InboundPlanOrderStatusEnum.CANCEL;
    }

    public void cancelAccept(Long inboundPlanOrderDetailId, Integer qtyAccepted) {
        log.info("inbound order: {} detail: {} cancel accept qty: {}", this.orderNo,
                inboundPlanOrderDetailId, qtyAccepted);

        this.details.stream().filter(v -> Objects.equals(v.getId(), inboundPlanOrderDetailId)).forEach(v -> v.cancelAccept(qtyAccepted));
    }

    public boolean isFullAccepted() {
        return this.details.stream().allMatch(InboundPlanOrderDetail::isCompleted);
    }

    public void completeAccepted() {

        log.info("inbound order: {} complete accepted.", this.orderNo);
        if (!this.isFullAccepted()) {
            log.error("inbound order: {} does not accept completed.", this.orderNo);
            return;
        }
        this.inboundPlanOrderStatus = InboundPlanOrderStatusEnum.ACCEPTED;

        DomainEventPublisher.sendAsyncDomainEvent(new InboundOrderCompletionEvent().setInboundOrderId(this.id));
    }

    public void forceCompleteAccepted() {

        log.info("inbound order: {} force complete accepted.", this.orderNo);
        this.details.forEach(InboundPlanOrderDetail::forceCompleteAccepted);
        this.inboundPlanOrderStatus = InboundPlanOrderStatusEnum.ACCEPTED;

        DomainEventPublisher.sendAsyncDomainEvent(new InboundOrderCompletionEvent().setInboundOrderId(this.id));
    }

    public InboundPlanOrderDetail getDetail(Long inboundPlanOrderDetailId) {
        return this.details.stream().filter(v -> Objects.equals(v.getId(), inboundPlanOrderDetailId)).findFirst().orElseThrow();
    }
}
