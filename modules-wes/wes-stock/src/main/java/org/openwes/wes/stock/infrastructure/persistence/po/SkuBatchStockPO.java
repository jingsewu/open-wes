package org.openwes.wes.stock.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_sku_batch_stock",
        indexes = {
                @Index(unique = true, name = "idx_sku_batch_attribute_warehouse_area", columnList = "skuBatchAttributeId,warehouseAreaId"),
                @Index(name = "idx_sku_id_warehouse_area", columnList = "skuId,warehouseAreaId")
        }
)
@DynamicUpdate
public class SkuBatchStockPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'skuId'")
    private Long skuId;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '批次ID'")
    private Long skuBatchAttributeId;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '库区ID'")
    private Long warehouseAreaId;

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

    @Version
    private Long version;
}
