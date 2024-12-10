package org.openwes.wes.api.inbound.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

@Data
public class AcceptOrderDetailDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -8457253817559784253L;

    private Long id;

    private Long acceptOrderId;

    private String ownerCode;

    @NotNull
    private Long inboundPlanOrderDetailId;

    private String boxNo;

    // if sku is loose , then they will be packed into a box
    private String packBoxNo;

    @NotEmpty
    private String targetContainerCode;
    @NotEmpty
    private String targetContainerSpecCode;
    @NotEmpty
    private String targetContainerSlotCode;

    @NotNull
    @Min(1)
    private Integer qtyAccepted;

    @NotEmpty
    private String skuCode;
    private String skuName;
    private String style;
    private String color;
    private String size;
    private String brand;

    private transient Map<String, Object> batchAttributes = new TreeMap<>();

    private Long workStationId;

}
