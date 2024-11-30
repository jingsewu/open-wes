package org.openwes.wes.stocktake.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.stocktake.constants.StocktakeAbnormalReasonEnum;
import org.openwes.wes.api.stocktake.constants.StocktakeRecordStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_stocktake_record",
        indexes = {
                @Index(name = "idx_stocktake_order_id", columnList = "stocktakeOrderId"),
                @Index(name = "idx_stocktake_task_id", columnList = "stocktakeTaskId"),
        }
)
@DynamicUpdate
public class StocktakeRecordPO extends UpdateUserPO {
    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false)
    @Comment("盘点单ID")
    private Long stocktakeOrderId;

    @Column(nullable = false)
    @Comment("盘点任务ID")
    private Long stocktakeTaskId;

    @Column(nullable = false)
    @Comment("盘点任务明细ID")
    private Long stocktakeTaskDetailId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("盘点记录状态")
    private StocktakeRecordStatusEnum stocktakeRecordStatus;

    @Column(nullable = false, length = 64)
    @Comment("仓库编码")
    private String warehouseCode;

    @Column(nullable = false)
    @Comment("容器ID")
    private Long containerId;

    @Column(nullable = false, length = 64)
    @Comment("容器编码")
    private String containerCode;

    @Column(nullable = false, length = 64)
    @Comment("容器面")
    private String containerFace;

    @Column(nullable = false, length = 64)
    @Comment("容器格口号")
    private String containerSlotCode;

    @Column(nullable = false)
    @Comment("SKU ID")
    private Long skuId;

    @Column(nullable = false)
    @Comment("库存ID")
    private Long stockId;

    @Column(nullable = false)
    @Comment("批次属性ID")
    private Long skuBatchAttributeId;

    @Column(nullable = false)
    @Comment("批次库存ID")
    private Long skuBatchStockId;

    @Column(nullable = false)
    @Comment("原库存数量")
    private Integer qtyOriginal;

    @Column(nullable = false)
    @Comment("盘点库存数量")
    private Integer qtyStocktake;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Comment("异常原因")
    private StocktakeAbnormalReasonEnum abnormalReason;

    @Column
    @Comment("盘点工作站ID")
    private Long workStationId;

    @Version
    private Long version;
}
