package org.openwes.wes.stock.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.stock.constants.StockAdjustmentOrderStatusEnum;
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
@Table(name = "w_stock_adjustment_order",
        indexes = {
                @Index(columnList = "orderNo", name = "uk_order_no", unique = true)
        }
)
public class StockAdjustmentOrderPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' COMMENT '仓库'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '调整单号'")
    private String orderNo;

    @Column(nullable = false, columnDefinition = "varchar(512) default '' comment '描述'")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)  comment '状态'")
    private StockAdjustmentOrderStatusEnum status;

    @Version
    private Long version;

}
