package org.openwes.wes.outbound.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
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
@Comment("Empty Container Outbound Order Management Table - Stores detailed information about empty container outbound orders.")
public class EmptyContainerOutboundOrderPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the empty container outbound order record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this order belongs")
    private String warehouseCode;

    @Column(nullable = false)
    @Comment("ID of the warehouse area where this order belongs (Reference to w_warehouse_area table id)")
    private Long warehouseAreaId;

    @Column(nullable = false, length = 64)
    @Comment("Unique order number for the empty container outbound order")
    private String orderNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Status of the empty container outbound order. Possible values are: " +
            "NEW NEW New Order), " +
            "PENDING PENDING Outbound in Progress), " +
            "FINISHED FINISHED Completed), " +
            "ABNORMAL ABNORMAL Abnormal Status), " +
            "CANCELED CANCELED Canceled)")
    private EmptyContainerOutboundOrderStatusEnum emptyContainerOutboundStatus;

    @Column(nullable = false, length = 64)
    @Comment("Specification code of the container used in the order")
    private String containerSpecCode;

    @Column(nullable = false)
    @Comment("Planned number of containers for the order")
    private Integer planCount;

    @Column(nullable = false)
    @Comment("Actual number of containers processed for the order")
    private Integer actualCount = 0;

    @Column(nullable = false)
    @Comment("ID of the workstation where this order is processed (Reference to w_work_station table id)")
    private Long workStationId;
}
