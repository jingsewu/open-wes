package org.openwes.wes.stocktake.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.stocktake.constants.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_stocktake_task",
        indexes = {
                @Index(unique = true, name = "idx_task_no", columnList = "taskNo"),
                @Index(name = "idx_stocktake_order_id", columnList = "stocktakeOrderId"),
                @Index(name = "idx_stocktake_task_status", columnList = "stocktakeTaskStatus"),
        }
)
@DynamicUpdate
public class StocktakeTaskPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false)
    @Comment("盘点单ID")
    private Long stocktakeOrderId;

    @Column(nullable = false, length = 64)
    @Comment("任务编号")
    private String taskNo;

    @Column(nullable = false, length = 64)
    @Comment("仓库")
    private String warehouseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("状态")
    private StocktakeTaskStatusEnum stocktakeTaskStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '盘点单类型'")
    private StocktakeTypeEnum stocktakeType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '创建方式'")
    private StocktakeCreateMethodEnum stocktakeCreateMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("盘点方式")
    private StocktakeMethodEnum stocktakeMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '创建类型'")
    private StocktakeUnitTypeEnum stocktakeUnitType;

    @Column
    @Comment("工作站")
    private Long workStationId;

    @Column
    @Comment("领单用户id")
    private Long receivedUserId;

    @Version
    private Long version;
}
