package org.openwes.wes.stocktake.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.wes.api.stocktake.constants.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_stocktake_order",
        indexes = {
                @Index(name = "idx_customer_order_no", columnList = "customerOrderNo"),
                @Index(unique = true, name = "idx_order_no", columnList = "orderNo")
        }
)
@DynamicUpdate
public class StocktakeOrderPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '订单编号'")
    private String orderNo;

    @Column(columnDefinition = "varchar(64) comment '客户订单编号'")
    private String customerOrderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private StocktakeOrderStatusEnum stocktakeOrderStatus;

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

    @Column(nullable = false)
    @Comment("是否盘点零库存")
    private Boolean includeZeroStock;

    @Column(nullable = false)
    @Comment("是否盘点空格口")
    private Boolean includeEmptySlot;

    @Column(nullable = false, columnDefinition = "int comment '异常标识'")
    private boolean abnormal;

    @Column(nullable = false, columnDefinition = "bigint comment '库区ID'")
    private Long warehouseAreaId;

    @Column(columnDefinition = "bigint comment '逻辑区ID'")
    private Long warehouseLogicId;

    @Version
    private Long version;
}
