package org.openwes.wes.outbound.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.outbound.constants.EmptyContainerOutboundOrderStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_empty_container_outbound_order",
        indexes = {
                @Index(unique = true, name = "uk_order_no", columnList = "orderNo"),
        }
)
@DynamicUpdate
public class EmptyContainerOutboundOrderPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "bigint comment '库区ID'")
    private Long warehouseAreaId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '订单编号'")
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private EmptyContainerOutboundOrderStatusEnum emptyContainerOutboundStatus;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器规格'")
    private String containerSpecCode;

    @Column(nullable = false, columnDefinition = "int comment '计划数量'")
    private Integer planCount;

    @Column(nullable = false, columnDefinition = "int comment '实际数量'")
    private Integer actualCount = 0;

    @Column(nullable = false, columnDefinition = "bigint comment '工作站ID'")
    private Long workStationId;

}
