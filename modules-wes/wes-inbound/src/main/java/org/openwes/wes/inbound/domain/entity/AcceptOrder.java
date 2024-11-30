package org.openwes.wes.inbound.domain.entity;

import com.google.common.collect.Lists;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.id.OrderNoGenerator;
import org.openwes.wes.api.inbound.constants.AcceptMethodEnum;
import org.openwes.wes.api.inbound.constants.AcceptOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.AcceptTypeEnum;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static org.openwes.common.utils.exception.code_enum.InboundErrorDescEnum.ACCEPT_ORDER_HAD_COMPLETED;

@Data
@Slf4j
public class AcceptOrder {

    private Long id;

    private String orderNo;

    /**
     * <P>验收单创建维度标识：
     * 1. 比如在机器人仓，维度可以为容器编码。即如果当前容器的验收单未完成，则继续追加，否则创建新的验收单
     * 2. 在人工仓，维度可以为入库计划单ID, 即如果当前入库计划单的验收单未完成，则继续追加，否则创建新的验收单
     */
    private String identifyNo;

    private String warehouseCode;

    private AcceptMethodEnum acceptMethod;
    private AcceptTypeEnum acceptType;

    private boolean putAway;

    private Integer totalQty;
    private Integer totalBox;

    private String remark;

    private AcceptOrderStatusEnum acceptOrderStatus;

    private List<AcceptOrderDetail> details;

    private Long version;

    public void initialize() {
        this.orderNo = OrderNoGenerator.generationAcceptOrderNo();
        this.acceptOrderStatus = AcceptOrderStatusEnum.NEW;

        calculateTotal();
    }

    public void accept(AcceptOrderDetail acceptOrderDetail) {

        log.info("accept order: {} accept inbound order detail: {} skuId: {} qty: {}", this.orderNo,
                acceptOrderDetail.getInboundPlanOrderDetailId(), acceptOrderDetail.getSkuId(), acceptOrderDetail.getQtyAccepted());

        acceptOrderDetail.setModified(true);

        if (this.details == null) {
            this.details = Lists.newArrayList(acceptOrderDetail);
        } else {
            this.details.add(acceptOrderDetail);
        }
        calculateTotal();
    }

    public void complete() {

        log.info("accept order: {} complete", this.orderNo);

        if (this.acceptOrderStatus != AcceptOrderStatusEnum.NEW) {
            throw WmsException.throwWmsException(ACCEPT_ORDER_HAD_COMPLETED, this.orderNo);
        }
        this.acceptOrderStatus = AcceptOrderStatusEnum.COMPLETE;
    }

    public void cancel(Long acceptOrderDetailId) {

        log.info("accept order: {} cancel detail: {}", this.orderNo, acceptOrderDetailId == null ? "all" : acceptOrderDetailId);

        if (this.acceptOrderStatus != AcceptOrderStatusEnum.NEW) {
            throw WmsException.throwWmsException(ACCEPT_ORDER_HAD_COMPLETED, this.orderNo);
        }

        calculateTotal();

        this.details.stream().filter(v -> acceptOrderDetailId == null || v.getId().equals(acceptOrderDetailId))
                .forEach(AcceptOrderDetail::cancel);
    }

    private void calculateTotal() {

        this.totalBox = 0;
        this.totalQty = 0;
        if (this.details == null) {
            return;
        }

        for (AcceptOrderDetail acceptOrderDetail : this.details) {
            int box = StringUtils.isNotEmpty(acceptOrderDetail.getBoxNo()) ? 1 : 0;
            this.totalBox += box;
            this.totalQty += acceptOrderDetail.getQtyAccepted();
        }
    }
}
