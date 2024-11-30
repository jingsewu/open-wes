package org.openwes.wes.api.inbound.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Data
public class PutAwayTaskDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -460989848595696347L;

    private Long id;
    private Long putAwayTaskId;

    private String ownerCode;

    private Long acceptOrderId;
    private Long acceptOrderDetailId;

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
