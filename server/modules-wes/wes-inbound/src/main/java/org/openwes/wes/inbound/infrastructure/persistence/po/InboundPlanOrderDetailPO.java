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
        name = "w_inbound_plan_order_detail",
        indexes = {
                @Index(name = "idx_inbound_plan_order_id", columnList = "inboundPlanOrderId"),
                @Index(name = "idx_box_no", columnList = "boxNo")
        }
)
@DynamicUpdate
public class InboundPlanOrderDetailPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint comment '入库通知单ID'")
    private Long inboundPlanOrderId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '箱号'")
    private String boxNo = "";

    @Column(nullable = false, columnDefinition = "int(11) comment '计划数量'")
    private Integer qtyRestocked = 0;
    @Column(nullable = false, columnDefinition = "int(11) comment '验收数量'")
    private Integer qtyAccepted = 0;
    @Column(nullable = false, columnDefinition = "int(11) comment '未收货数量'")
    private Integer qtyUnreceived = 0;

    @Column(nullable = false, columnDefinition = "int(11) comment '异常数量'")
    private Integer qtyAbnormal = 0;
    @Column(columnDefinition = "varchar(128) comment '异常原因'")
    private String abnormalReason;
    @Column(columnDefinition = "varchar(128) comment '异常原因责任方'")
    private String responsibleParty;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '货主编码'")
    private String ownerCode;
    @Column(nullable = false, columnDefinition = "bigint comment 'skuID'")
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

    @Column(columnDefinition = "json comment '扩展字段'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> extendFields = new TreeMap<>();
}
