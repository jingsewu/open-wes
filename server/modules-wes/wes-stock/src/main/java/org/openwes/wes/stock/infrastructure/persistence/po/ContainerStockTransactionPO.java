package org.openwes.wes.stock.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_container_stock_transaction",
        indexes = {
                @Index(name = "idx_container_code", columnList = "containerCode"),
                @Index(name = "idx_order_no", columnList = "orderNo"),
                @Index(name = "idx_sku_id", columnList = "skuId")
        }
)
@DynamicUpdate
public class ContainerStockTransactionPO extends UpdateUserPO {
    @Id
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '明细库存id'")
    private Long containerStockId = 0L;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '批次库存id'")
    private Long skuBatchStockId;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'skuID'")
    private Long skuId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OperationTaskTypeEnum operationTaskType;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '原容器编码'")
    private String sourceContainerCode;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '原容器格口编码'")
    private String sourceContainerSlotCode = "";

    @Column(nullable = false, columnDefinition = "varchar(64) comment '目标容器编码'")
    private String targetContainerCode;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '目标容器格口编码'")
    private String targetContainerSlotCode;

    /**
     * 不管上架，还是出库，存在对应容器的编码&格口
     */
    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器编码'")
    private String containerCode;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器格口编码'")
    private String containerSlotCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '订单号'")
    private String orderNo;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '任务id'")
    private Long taskId = 0L;

    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '数量'")
    private Integer transferQty;

    @Version
    private Long version;
}
