package org.openwes.wes.inbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.AuditUserPO;
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
        name = "w_empty_container_inbound_order_detail",
        indexes = {
                @Index(name = "idx_empty_container_inbound_order_id", columnList = "emptyContainerInboundOrderId"),
                @Index(name = "idx_container_code", columnList = "containerCode"),
                @Index(name = "idx_location_code", columnList = "locationCode")
        }
)
@DynamicUpdate
@EqualsAndHashCode(callSuper = true)
public class EmptyContainerInboundOrderDetailPO extends AuditUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint comment '空箱入库单ID'")
    private Long emptyContainerInboundOrderId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器编号'")
    private String containerCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器规格'")
    private String containerSpecCode;

    @Column(columnDefinition = "varchar(64) comment '地面码'")
    private String locationCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private PutAwayTaskStatusEnum inboundStatus;
}
