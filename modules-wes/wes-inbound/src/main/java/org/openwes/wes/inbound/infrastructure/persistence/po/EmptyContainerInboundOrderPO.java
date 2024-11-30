package org.openwes.wes.inbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.AuditUserPO;
import org.openwes.wes.api.inbound.constants.EmptyContainerInboundWayEnum;
import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_empty_container_inbound_order",
        indexes = {
                @Index(unique = true, name = "uk_empty_container_inbound_order_order_no", columnList = "orderNo")
        }
)
@DynamicUpdate
@EqualsAndHashCode(callSuper = true)
public class EmptyContainerInboundOrderPO extends AuditUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '订单编号'")
    private String orderNo;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '入库方式'")
    private EmptyContainerInboundWayEnum inboundWay;

    @Column(nullable = false, columnDefinition = "int(11) comment '计划数量'")
    private Integer planCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private PutAwayTaskStatusEnum inboundStatus;
}
