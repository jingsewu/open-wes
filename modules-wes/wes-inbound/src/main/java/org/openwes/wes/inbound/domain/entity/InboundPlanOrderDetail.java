package org.openwes.wes.inbound.domain.entity;

import com.google.common.base.Preconditions;
import org.openwes.wes.api.main.data.dto.SkuMainDataDTO;
import lombok.Data;

import java.util.Map;


@Data
public class InboundPlanOrderDetail {

    private Long id;
    private Long inboundPlanOrderId;

    private String ownerCode;
    private String boxNo;

    private Integer qtyRestocked;
    private Integer qtyAccepted;
    private Integer qtyAbnormal;

    private String abnormalReason;
    private String responsibleParty;

    private String skuCode;
    private Long skuId;
    private String skuName;
    private String style;
    private String color;
    private String size;
    private String brand;

    private Map<String, Object> batchAttributes;
    private Map<String, Object> extendFields;

    private void validateQty() {
        Preconditions.checkState(this.qtyRestocked >= this.qtyAccepted + this.qtyAbnormal,
                "restocked qty should be greater than accepted qty");
    }

    public void accept(Integer qtyAccepted, Integer qtyAbnormal) {
        this.qtyAccepted += qtyAccepted;
        this.qtyAbnormal += (qtyAbnormal == null ? 0 : qtyAbnormal);
        validateQty();
    }

    public boolean isCompleted() {
        return this.qtyRestocked == this.qtyAccepted + this.qtyAbnormal;
    }

    public void close() {
        this.qtyAbnormal = this.qtyRestocked - this.qtyAccepted;
        this.abnormalReason = "order closed";
        validateQty();
    }

    public void initSkuInfo(SkuMainDataDTO skuMainDataDTO) {
        this.skuCode = skuMainDataDTO.getSkuCode();
        this.skuId = skuMainDataDTO.getId();
        this.skuName = skuMainDataDTO.getSkuName();
        this.style = skuMainDataDTO.getStyle();
        this.color = skuMainDataDTO.getColor();
        this.size = skuMainDataDTO.getSize();
        this.brand = skuMainDataDTO.getBrand();
    }

    public void cancelAccept(int qtyAccepted) {
        this.qtyAccepted -= qtyAccepted;
        validateQty();
    }

    public void forceCompleteAccepted() {
        this.qtyAbnormal = this.qtyRestocked - this.qtyAccepted;
        validateQty();
    }
}
