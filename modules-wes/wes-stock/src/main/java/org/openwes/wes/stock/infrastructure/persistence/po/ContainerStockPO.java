package org.openwes.wes.stock.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = false)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_container_stock",
        indexes = {
                @Index(unique = true, name = "uk_container_slot_sku_batch", columnList = "containerCode,containerSlotCode,skuBatchStockId"),
                @Index(name = "idx_sku_batch_stock_id", columnList = "skuBatchStockId"),
                @Index(name = "idx_sku_id", columnList = "skuId")
        }
)
public class ContainerStockPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'skuId'")
    private Long skuId;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'skuBatchAttributeId'")
    private Long skuBatchAttributeId;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '批次库存id'")
    private Long skuBatchStockId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    /**
     * container is not must be a physical container. e.g.
     * when sku received to a place but not a physical container,
     * then the container code will be received order no ,
     * and when sku put away on the rack, then the container code is the location code;
     */
    // when there is transfer container, container id is null
    @Column(columnDefinition = "bigint default 0 comment '容器id'")
    private Long containerId;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器编码'")
    private String containerCode;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器面'")
    private String containerFace = "";
    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器格口编码'")
    private String containerSlotCode;

    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '总数量'")
    private Integer totalQty = 0;
    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '可用数量'")
    private Integer availableQty = 0;
    // outbound locked qty
    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '出库锁定数量'")
    private Integer outboundLockedQty = 0;
    // other operation locked qty in the warehouse
    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '库内锁定数量'")
    private Integer noOutboundLockedQty = 0;

    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '冻结数量'")
    private Integer frozenQty = 0;
    /**
     * it means the container is or not a physical container
     */
    private boolean boxStock;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '箱号'")
    private String boxNo = "";

    @Version
    private Long version;
}
