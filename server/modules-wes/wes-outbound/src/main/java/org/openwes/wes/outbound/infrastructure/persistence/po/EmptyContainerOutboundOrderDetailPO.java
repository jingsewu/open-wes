package org.openwes.wes.outbound.infrastructure.persistence.po;


import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.outbound.constants.EmptyContainerOutboundDetailStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_empty_container_outbound_order_detail",
        indexes = {
                @Index(name = "idx_container_code", columnList = "containerCode"),
                @Index(name = "idx_empty_container_outbound_order_id", columnList = "emptyContainerOutboundOrderId"),
        }
)
@DynamicUpdate
public class EmptyContainerOutboundOrderDetailPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint comment '容器出库单ID'")
    private Long emptyContainerOutboundOrderId;

    @Column(nullable = false, columnDefinition = "bigint comment '容器ID'")
    private Long containerId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器编码'")
    private String containerCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private EmptyContainerOutboundDetailStatusEnum detailStatus = EmptyContainerOutboundDetailStatusEnum.UNDO;

}
