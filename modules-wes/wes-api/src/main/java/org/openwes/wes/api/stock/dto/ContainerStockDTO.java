package org.openwes.wes.api.stock.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class ContainerStockDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = -5856579493860691401L;

    private Long id;

    private Long skuId;

    private Long skuBatchAttributeId;

    private Long skuBatchStockId;

    private String warehouseCode;

    /**
     * container is not must be a physical container. e.g.
     * when sku received to a place but not a physical container,
     * then the container code will be received order no ,
     * and when sku put away on the rack, then the container code is the location code;
     */
    private Long containerId;
    private String containerCode;
    private String containerFace;
    private String containerSlotCode;

    private Integer totalQty;
    private Integer availableQty;
    // outbound locked qty
    private Integer outboundLockedQty;
    // other operation locked qty in the warehouse
    private Integer noOutboundLockedQty;
    private Integer frozenQty;

    /**
     * it means the container is or not a physical container
     */
    private boolean boxStock;
    private String boxNo;

    private Long version;
}
