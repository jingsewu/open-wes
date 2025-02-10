package org.openwes.wes.inbound.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.AuditUserPO;
import org.openwes.wes.api.inbound.constants.AcceptMethodEnum;
import org.openwes.wes.api.inbound.constants.AcceptOrderStatusEnum;
import org.openwes.wes.api.inbound.constants.AcceptTypeEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_accept_order",
        indexes = {
                @Index(unique = true, name = "uk_order_no", columnList = "orderNo"),
                @Index(name = "idx_identify_no", columnList = "identifyNo")
        }
)
@DynamicUpdate
@Comment("Warehouse Acceptance Order - Manages the receiving and acceptance process for goods. " +
        "Supports multiple acceptance methods (box, loose, container) and types (direct, receive, in-warehouse, etc.). " +
        "Tracks the order from creation (NEW) through to completion (COMPLETE)")
public class AcceptOrderPO extends AuditUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Primary key - Unique identifier for each acceptance order, generated using custom IdGenerator")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Order number - Unique business identifier for tracking acceptance orders. " +
            "Indexed for quick lookup and reference")
    private String orderNo;

    @Column(nullable = false, length = 64)
    @Comment("Identification number - Secondary identifier for acceptance order creation. It is the container code in the receiving situation. Indexed for performance")
    private String identifyNo;

    @Column(nullable = false, length = 64)
    @Comment("Warehouse code - Identifies the specific warehouse location for the acceptance process. " +
            "Used for inventory management and location tracking")
    private String warehouseCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Acceptance method - Defines physical receiving process: " +
            "BOX_CONTENT (pre-packed boxes), " +
            "LOOSE_INVENTORY (individual items), " +
            "CONTAINER (shipping containers). " +
            "Determines handling procedures and verification requirements")
    private AcceptMethodEnum acceptMethod = AcceptMethodEnum.LOOSE_INVENTORY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Acceptance type - Business scenario for receiving: " +
            "DIRECT (immediate), RECEIVE (standard), " +
            "IN_WAREHOUSE (internal), WAYBILL (document-driven), " +
            "WITHOUT_ORDER (unplanned). " +
            "Controls workflow and required documentation")
    private AcceptTypeEnum acceptType = AcceptTypeEnum.RECEIVE;

    @Comment("Put-away flag - Indicates whether accepted goods have been moved to their storage location. " +
            "Used to track warehouse operation completion")
    private boolean putAway;

    @Column(nullable = false)
    @Comment("Total quantity - Sum of all individual items in the acceptance order. " +
            "Used for inventory reconciliation and capacity planning")
    private Integer totalQty = 0;

    @Column(nullable = false)
    @Comment("Total boxes - Number of physical containers/boxes in the order. " +
            "Critical for BOX_CONTENT acceptance method and space planning")
    private Integer totalBox = 0;

    @Column(nullable = false, length = 500)
    @Comment("Remarks - Additional notes about the acceptance process. " +
            "Used for special handling instructions or documentation of exceptions")
    private String remark = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Order status - Tracks acceptance order lifecycle: " +
            "NEW (初始状态-待处理), " +
            "COMPLETE (已完成-验收完成). " +
            "Controls available actions and validation rules")
    private AcceptOrderStatusEnum acceptOrderStatus = AcceptOrderStatusEnum.NEW;

    @Version
    @Comment("Version number - Used for optimistic locking to prevent concurrent modifications. " +
            "Automatically managed by Hibernate")
    private Long version;
}
