package org.openwes.wes.stock.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.stock.constants.StockAbnormalReasonEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Table(name = "w_stock_adjustment_detail",
        indexes = {
                @Index(columnList = "stockAdjustmentOrderId", name = "idx_stock_adjustment_order_id"),
                @Index(columnList = "containerStockId", name = "idx_container_stock_id")
        }
)
public class StockAdjustmentDetailPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '调整单ID'")
    private Long stockAdjustmentOrderId;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '库存异常记录ID'")
    private Long stockAbnormalRecordId = 0L;

    @Column(columnDefinition = "varchar(64) default '' comment '货主'")
    private String ownerCode;

    @Column(nullable = false, columnDefinition = "bigint comment '库存id'")
    private Long containerStockId;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '库存批次id'")
    private Long skuBatchStockId;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '批次id'")
    private Long skuBatchAttributeId;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '商品id'")
    private Long skuId;

    @Column(columnDefinition = "varchar(64) default '' comment '商品编码'")
    private String skuCode;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '容器编号'")
    private String containerCode;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '容器格口(子容器编号)'")
    private String containerSlotCode;

    @Column(nullable = false, columnDefinition = "int comment '调整数量'")
    private Integer qtyAdjustment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '异常原因'")
    private StockAbnormalReasonEnum abnormalReason;
}
