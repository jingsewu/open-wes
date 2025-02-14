package org.openwes.wes.inbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.id.IdGenerator;
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
@Comment("Acceptance Order Detail - Contains item-level details for warehouse acceptance orders. " +
        "Tracks individual SKUs, containers, and batch attributes for each accepted item. " +
        "Links to inbound plans and manages container assignments.")
public class AcceptOrderDetailPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Primary key - Unique identifier for each acceptance order detail entry")
    private Long id;

    @Column(nullable = false)
    @Comment("Parent Accept Order ID - Links to w_accept_order table. " +
            "Represents the main acceptance order this detail belongs to")
    private Long acceptOrderId;

    @Column(nullable = false, length = 64)
    @Comment("Owner Code - Identifies the owner of the goods being accepted. " +
            "Used for multi-tenant warehouse management")
    private String ownerCode;

    @Column(nullable = false)
    @Comment("Inbound Plan Order ID - Links to w_inbound_plan_order table. References the planned inbound order. " +
            "Used for matching actual receipts against planned deliveries")
    private Long inboundPlanOrderId;

    @Column(nullable = false)
    @Comment("Inbound Plan Order Detail ID - Links to w_inbound_plan_order_detail." +
            "References the specific line item in the inbound plan. " +
            "Used for detailed receipt vs plan reconciliation")
    private Long inboundPlanOrderDetailId;

    @Column(nullable = false, length = 64)
    @Comment("Box Number - Original box identifier for BOX_CONTENT acceptance method. " +
            "Empty string if not applicable")
    private String boxNo = "";

    @Column(nullable = false, length = 64)
    @Comment("Pack Box Number - Assigned box number for loose items that are packed during acceptance. " +
            "Used when consolidating LOOSE_INVENTORY items")
    private String packBoxNo = "";

    @Column(nullable = false)
    @Comment("Target Container ID - Internal ID of the assigned storage container")
    private Long targetContainerId;

    @Column(nullable = false, length = 64)
    @Comment("Target Container Code - External identifier for the assigned storage container. " +
            "Used for warehouse operations and tracking")
    private String targetContainerCode;

    @Column(nullable = false, length = 64)
    @Comment("Target Container Specification Code - Defines the type and capacity of the container. " +
            "Used for space planning and compatibility checking")
    private String targetContainerSpecCode;

    @Column(nullable = false, length = 64)
    @Comment("Target Container Slot Code - Specific location within the container. " +
            "Used for precise item placement and retrieval")
    private String targetContainerSlotCode;

    @Column(nullable = false, length = 64)
    @Comment("Target Container Face - Identifies which side/face of the container to access. " +
            "Important for container accessibility and picking efficiency")
    private String targetContainerFace = "";

    @Column(nullable = false)
    @Comment("Accepted Quantity - Number of items accepted in this detail record. " +
            "Used for inventory updates and plan reconciliation")
    private Integer qtyAccepted = 0;

    @Column(nullable = false)
    @Comment("SKU Batch Attribute ID - Links to w_sku_batch_attribute table." +
            "References the batch characteristics of the SKU. " +
            "Used for tracking lot-specific information")
    private Long skuBatchAttributeId;

    @Column(nullable = false)
    @Comment("SKU ID - Internal identifier for the Stock Keeping Unit")
    private Long skuId;

    @Column(nullable = false, length = 64)
    @Comment("SKU Code - External identifier for the Stock Keeping Unit. " +
            "Used for business operations and reporting")
    private String skuCode;

    @Column(nullable = false, length = 512)
    @Comment("SKU Name - Descriptive name of the Stock Keeping Unit. " +
            "Used for human readability and verification")
    private String skuName = "";

    @Column(length = 64)
    @Comment("Style - Product style identifier. Part of SKU characteristics")
    private String style;

    @Column(length = 64)
    @Comment("Color - Product color code. Part of SKU characteristics")
    private String color;

    @Column(length = 64)
    @Comment("Size - Product size code. Part of SKU characteristics")
    private String size;

    @Column(length = 64)
    @Comment("Brand - Product brand identifier. Part of SKU characteristics")
    private String brand;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Batch Attributes - JSON map storing dynamic batch-specific attributes. " +
            "Can include manufacture date, expiry date, lot number, etc. " +
            "Stored as TreeMap for consistent ordering")
    private Map<String, Object> batchAttributes = new TreeMap<>();

    @Column(nullable = false)
    @Comment("Workstation ID - Identifies the physical workstation where acceptance was performed. " +
            "Used for operation tracking and accountability")
    private Long workStationId;
}
