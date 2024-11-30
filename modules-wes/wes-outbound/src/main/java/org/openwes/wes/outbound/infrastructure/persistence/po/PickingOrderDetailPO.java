package org.openwes.wes.outbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.outbound.constants.PickingOrderDetailStatusEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Collection;
import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "w_picking_order_detail",
    indexes = {
        @Index(name = "idx_picking_order_id", columnList = "pickingOrderId")
    }
)
@DynamicUpdate
public class PickingOrderDetailPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '货主'")
    private String ownerCode;

    @Column(nullable = false, columnDefinition = "bigint comment '拣货单ID'")
    private Long pickingOrderId;

    @Column(nullable = false, columnDefinition = "bigint comment '出库计划单ID'")
    private Long outboundOrderPlanId;

    @Column(nullable = false, columnDefinition = "bigint comment '出库计划单明细ID'")
    private Long outboundOrderPlanDetailId;

    @Column(nullable = false, columnDefinition = "bigint comment 'skuID'")
    private Long skuId;

    @Column(columnDefinition = "json comment '批次属性'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> batchAttributes;

    @Column(columnDefinition = "json comment '库存重定位库区IDS'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Collection<Long> retargetingWarehouseAreaIds;

    @Column(nullable = false, columnDefinition = "bigint comment 'sku batch stock ID'")
    private Long skuBatchStockId;

    @Column(nullable = false, columnDefinition = "int(11) comment '计划数量'")
    private Integer qtyRequired;
    @Column(nullable = false, columnDefinition = "int(11) comment '实际拣货数量'")
    private Integer qtyActual = 0;
    @Column(nullable = false, columnDefinition = "int(11) comment '异常登记数量'")
    private Integer qtyAbnormal = 0;
    @Column(nullable = false, columnDefinition = "int(11) comment '缺拣数量'")
    private Integer qtyShort = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private PickingOrderDetailStatusEnum pickingOrderDetailStatus = PickingOrderDetailStatusEnum.NEW;

    @Version
    private Long version;
}
