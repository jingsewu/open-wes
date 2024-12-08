package org.openwes.wes.stock.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.stock.constants.StockAbnormalReasonEnum;
import org.openwes.wes.api.stock.constants.StockAbnormalStatusEnum;
import org.openwes.wes.api.stock.constants.StockAbnormalTypeEnum;
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
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@EntityListeners(AuditingEntityListener.class)
@DynamicUpdate
@Table(name = "w_stock_abnormal_record",
        indexes = {
                @Index(columnList = "orderNo", name = "uk_order_no", unique = true),
                @Index(columnList = "containerCode", name = "idx_container_code"),
                @Index(columnList = "containerStockId", name = "idx_container_stock_id")
        }
)
public class StockAbnormalRecordPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' COMMENT '仓库'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '货主编码'")
    private String ownerCode;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '异常单号'")
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default '' comment '异常类型'")
    private StockAbnormalTypeEnum stockAbnormalType;

    @Column(columnDefinition = "varchar(64) default '' comment '复盘单号-差异复盘 二盘单号'")
    private String replayNo;

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

    @Column(columnDefinition = "varchar(64) default '' comment '库位编码'")
    private String locationCode;

    @Column(nullable = false, columnDefinition = "int comment '盈亏'")
    private Integer qtyAbnormal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20)  comment '差异原因'")
    private StockAbnormalReasonEnum abnormalReason = StockAbnormalReasonEnum.LESS_ENTITY;

    @Column(columnDefinition = "varchar(512) default '' comment '原因描述'")
    private String reasonDesc;

    @Column(columnDefinition = "varchar(64) default '' comment '原始单号'")
    private String abnormalOrderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32)  comment '状态'")
    private StockAbnormalStatusEnum stockAbnormalStatus = StockAbnormalStatusEnum.NEW;

    @Version
    private Long version;
}
