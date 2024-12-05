package org.openwes.wes.outbound.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.AuditUserPO;
import org.openwes.wes.api.outbound.constants.OutboundPlanOrderStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;
import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_outbound_plan_order",
        indexes = {
                @Index(name = "idx_customer_order_no", columnList = "customerOrderNo"),
                @Index(name = "idx_customer_wave_no", columnList = "customerWaveNo"),
                @Index(unique = true, name = "uk_order_no", columnList = "orderNo"),
                @Index(name = "idx_outbound_plan_order_status", columnList = "outboundPlanOrderStatus"),
        }
)
@DynamicUpdate
public class OutboundPlanOrderPO extends AuditUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '外部波次号'")
    private String customerWaveNo = "";
    @Column(nullable = false, columnDefinition = "varchar(64) comment '波次号'")
    private String waveNo = "";

    @Column(nullable = false, columnDefinition = "varchar(64) comment '客户订单编号'")
    private String customerOrderNo;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '订单类型'")
    private String customerOrderType = "";

    @Column(nullable = false, columnDefinition = "varchar(64) comment '承运商'")
    private String currierCode = "";
    @Column(nullable = false, columnDefinition = "varchar(64) comment '运单号'")
    private String waybillNo = "";
    @Column(nullable = false, columnDefinition = "varchar(64) comment '来源平台'")
    private String origPlatformCode = "";

    @Column(nullable = false, columnDefinition = "bigint comment '截单时间'")
    private Long expiredTime = 0L;
    @Column(nullable = false, columnDefinition = "bigint comment '优先级'")
    private Integer priority = 0;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '订单编号'")
    private String orderNo;

    @Column(nullable = false, columnDefinition = "int(11) comment 'SKU种类'")
    private Integer skuKindNum;
    @Column(nullable = false, columnDefinition = "int(11) comment '总数量'")
    private Integer totalQty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private OutboundPlanOrderStatusEnum outboundPlanOrderStatus = OutboundPlanOrderStatusEnum.NEW;

    private boolean shortOutbound;
    private boolean shortWaiting;

    private boolean abnormal;
    @Column(nullable = false, columnDefinition = "varchar(128) comment '异常原因'")
    private String abnormalReason = "";

    @Column(columnDefinition = "json comment '扩展字段'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> reservedFields;

    @Column(columnDefinition = "json comment '出库封箱目的地'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Set<String> destinations;

    @Version
    private Long version;
}
