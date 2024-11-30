package org.openwes.wes.inbound.domain.entity;

import lombok.Data;

import java.util.Map;

@Data
public class PutAwayTaskDetail {

    private Long id;
    private Long putAwayTaskId;

    private String ownerCode;

    private Long acceptOrderId;
    private Long acceptOrderDetailId;

    private Long containerId;
    private String containerCode;
    private String containerSlotCode;
    private String containerFace;

    private Long skuBatchAttributeId;
    private Map<String, Object> batchAttributes;

    private String boxNo;

    private Long skuId;
    private String skuCode;
    private String skuName;

    private Integer qtyPutAway;

    private Map<String, Object> extendFields;

}
