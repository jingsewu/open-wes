package org.openwes.wes.outbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderDetailStatusEnum;
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
    name = "w_outbound_plan_order_detail",
    indexes = {
        @Index(name = "idx_outbound_plan_order_id", columnList = "outboundPlanOrderId"),
    }
)
@DynamicUpdate
public class OutboundPlanOrderDetailPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint comment '出库计划单ID'")
    private Long outboundPlanOrderId;

    @Column(nullable = false, columnDefinition = "bigint comment 'skuID'")
    private Long skuId;
    @Column(nullable = false, columnDefinition = "varchar(64) comment 'sku编码'")
    private String skuCode;
    @Column(nullable = false, columnDefinition = "varchar(512) comment 'sku名称'")
    private String skuName = "";
    @Column(nullable = false, columnDefinition = "varchar(64) comment '货主编码'")
    private String ownerCode;

    @Column(columnDefinition = "json comment '批次属性'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> batchAttributes;

    @Column(nullable = false, columnDefinition = "int(11) comment '计划数量'")
    private Integer qtyRequired;
    @Column(nullable = false, columnDefinition = "int(11) comment '预占的批次库存数量'")
    private Integer qtyAllocated = 0;
    @Column(nullable = false, columnDefinition = "int(11) comment '实际拣货数量'")
    private Integer qtyActual = 0;

    @Column(columnDefinition = "json comment '库区ID列表'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Collection<Long> warehouseAreaIds;

    @Column(columnDefinition = "json comment '扩展字段'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> reservedFields;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private OutboundPlanOrderDetailStatusEnum outboundPlanOrderDetailStatus = OutboundPlanOrderDetailStatusEnum.NEW;

    @Version
    private Long version;
}
