package org.openwes.wes.stocktake.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.stocktake.constants.StocktakeTaskDetailStatusEnum;
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
        name = "w_stocktake_task_detail",
        indexes = {
                @Index(name = "idx_stocktake_task_id", columnList = "stocktakeTaskId"),
                @Index(name = "idx_stocktake_order_id", columnList = "stocktakeOrderId"),
                @Index(name = "idx_container_code_face", columnList = "containerCode,containerFace"),
        }
)
@DynamicUpdate
public class StocktakeTaskDetailPO extends UpdateUserPO {
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

    @Column(nullable = false, length = 64)
    @Comment("仓库")
    private String warehouseCode;

    @Column(nullable = false, length = 64)
    @Comment("容器编码")
    private String containerCode;

    @Column(length = 64)
    @Comment("容器面")
    private String containerFace;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("盘点任务明细状态")
    private StocktakeTaskDetailStatusEnum stocktakeTaskDetailStatus;

    @Version
    private Long version;
}
