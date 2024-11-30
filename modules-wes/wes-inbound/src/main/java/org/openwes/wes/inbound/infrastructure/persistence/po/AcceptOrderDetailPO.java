package org.openwes.wes.inbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;
import java.util.TreeMap;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_accept_order_detail",
        indexes = {
                @Index(name = "idx_inbound_plan_order_detail_id", columnList = "inboundPlanOrderDetailId"),
                @Index(name = "idx_inbound_plan_order__id", columnList = "inboundPlanOrderId"),
                @Index(name = "idx_accept_order_id", columnList = "acceptOrderId")
        }
)
@DynamicUpdate
public class AcceptOrderDetailPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint comment '验收单ID'")
    private Long acceptOrderId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '货主编码'")
    private String ownerCode;

    @Column(nullable = false, columnDefinition = "bigint comment '入库通知单ID'")
    private Long inboundPlanOrderId;

    @Column(nullable = false, columnDefinition = "bigint comment '入库通知单明细ID'")
    private Long inboundPlanOrderDetailId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '箱号'")
    private String boxNo = "";

    // if sku is loose , then they will be packed into a box
    @Column(nullable = false, columnDefinition = "varchar(64) comment '箱号'")
    private String packBoxNo = "";

    @Column(nullable = false)
    private Long targetContainerId;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '目标容器编码'")
    private String targetContainerCode;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '目标容器规格编码'")
    private String targetContainerSpecCode;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '目标容器格口编码'")
    private String targetContainerSlotCode;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '目标容器面'")
    private String targetContainerFace = "";

    @Column(nullable = false, columnDefinition = "int(11) comment '验收数量'")
    private Integer qtyAccepted = 0;

    @Column(nullable = false)
    private Long skuBatchAttributeId;

    @Column(nullable = false)
    private Long skuId;
    @Column(nullable = false, columnDefinition = "varchar(64) comment 'sku编码'")
    private String skuCode;
    @Column(nullable = false, columnDefinition = "varchar(512) comment 'sku名称'")
    private String skuName = "";
    @Column(columnDefinition = "varchar(64) comment '款式'")
    private String style;
    @Column(columnDefinition = "varchar(64) comment '颜色'")
    private String color;
    @Column(columnDefinition = "varchar(64) comment '尺码'")
    private String size;
    @Column(columnDefinition = "varchar(64) comment '品牌'")
    private String brand;

    @Column(columnDefinition = "json comment '批次属性'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> batchAttributes = new TreeMap<>();

    @Column(nullable = false, columnDefinition = "bigint comment '工作站ID'")
    private Long workStationId;

}
