package org.openwes.wes.inbound.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.AuditUserPO;
import org.openwes.wes.api.inbound.constants.InboundPlanOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.StorageTypeEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_inbound_plan_order",
        indexes = {
                @Index(name = "idx_customer_order_no", columnList = "customerOrderNo"),
                @Index(name = "idx_lpn", columnList = "lpnCode"),
                @Index(unique = true, name = "uk_order_no", columnList = "orderNo"),
                @Index(name = "idx_inbound_plan_order_status", columnList = "inboundPlanOrderStatus")
        }
)
@DynamicUpdate
public class InboundPlanOrderPO extends AuditUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '订单编号'")
    private String orderNo;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '客户订单编号'")
    private String customerOrderNo;

    @Column(nullable = false, columnDefinition = "varchar(64) comment 'LPN'")
    private String lpnCode = "";

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库'")
    private String warehouseCode;

    @Column(length = 128, nullable = false)
    @Comment("customer order type")
    private String customerOrderType = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '存储类型'")
    private StorageTypeEnum storageType;
    private boolean abnormal;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '发货方'")
    private String sender;
    @Column(nullable = false, columnDefinition = "varchar(128) comment '承运商'")
    private String carrier;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '承运方式'")
    private String shippingMethod;
    @Column(nullable = false, columnDefinition = "varchar(128) comment '承运单号'")
    private String trackingNumber;
    @Column(nullable = false, columnDefinition = "bigint comment '预计到达时间'")
    private Long estimatedArrivalDate = 0L;

    @Column(nullable = false, columnDefinition = "varchar(255) comment '备注'")
    private String remark = "";

    @Column(nullable = false, columnDefinition = "int(11) comment 'SKU种类'")
    private Integer skuKindNum;
    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '总数量'")
    private Integer totalQty;
    @Column(nullable = false, columnDefinition = "int(11) default 0 comment '总箱数'")
    private Integer totalBox;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private InboundPlanOrderStatusEnum inboundPlanOrderStatus = InboundPlanOrderStatusEnum.NEW;

    @Column(columnDefinition = "json comment '扩展字段'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> extendFields;

    @Version
    private Long version;
}
