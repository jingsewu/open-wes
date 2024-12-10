package org.openwes.wes.inbound.domain.entity;

import org.openwes.common.utils.jpa.ModificationAware;
import lombok.Data;

import java.util.Map;
import java.util.TreeMap;

@Data
public class AcceptOrderDetail implements ModificationAware {
    private Long id;

    private Long acceptOrderId;

    private String ownerCode;

    private Long inboundPlanOrderId;
    private Long inboundPlanOrderDetailId;

    private String boxNo;

    private String packBoxNo;

    private Long targetContainerId;
    private String targetContainerCode;
    private String targetContainerSpecCode;
    private String targetContainerSlotCode;
    private String targetContainerFace;

    private Integer qtyAccepted;

    private String skuCode;
    private String skuName;
    private Long skuId;
    private Long skuBatchAttributeId;
    private String style;
    private String color;
    private String size;
    private String brand;

    private Map<String, Object> batchAttributes = new TreeMap<>();

    private Map<String, Object> extendFields;

    private Long workStationId;

    private boolean modified;

    public void cancel() {
        if (qtyAccepted > 0) {
            this.qtyAccepted = 0;
            this.modified = true;
        }
    }
}
