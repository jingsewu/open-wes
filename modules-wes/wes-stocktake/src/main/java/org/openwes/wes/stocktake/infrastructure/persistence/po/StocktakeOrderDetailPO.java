package org.openwes.wes.stocktake.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.stocktake.constants.StocktakeUnitTypeEnum;
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
        name = "w_stocktake_order_detail",
        indexes = {
                @Index(name = "idx_stocktake_order_id", columnList = "stocktakeOrderId")
        }
)
@DynamicUpdate
public class StocktakeOrderDetailPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint comment '盘点单ID'")
    private Long stocktakeOrderId;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Comment("盘点基本单位类型")
    private StocktakeUnitTypeEnum stocktakeUnitType;

    @Column(length = 64)
    @Comment("盘点基本单位编码")
    private String unitCode;

    @Column
    @Comment("盘点基本单位Id：按商品盘点时为SkuId")
    private Long unitId;

    @Version
    private Long version;
}
