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
import org.openwes.common.utils.id.IdGenerator;
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
@Comment("Inbound Plan Order - Master record for planned warehouse receipts. " +
        "Tracks expected deliveries from creation through receiving and closure. " +
        "Manages shipping details, quantities, and status transitions.")
public class InboundPlanOrderPO extends AuditUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Primary key - Unique identifier for each inbound plan")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Order Number - Internal system reference number. " +
            "Must be unique across all inbound plans")
    private String orderNo;

    @Column(nullable = false, length = 64)
    @Comment("Customer Order Number - External reference number from customer. " +
            "Used for customer communication and tracking")
    private String customerOrderNo;

    @Column(nullable = false, length = 64)
    @Comment("License Plate Number (LPN) - Unique identifier for the physical shipment. " +
            "Used for tracking and receiving operations")
    private String lpnCode = "";

    @Column(nullable = false, length = 64)
    @Comment("Warehouse Code - Identifies the receiving warehouse location. " +
            "Controls inventory assignment and operations")
    private String warehouseCode;

    @Column(length = 128, nullable = false)
    @Comment("Customer Order Type - Categorizes the order based on customer requirements. " +
            "Affects handling procedures and priorities")
    private String customerOrderType = "";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'STORAGE'")
    @Comment("Storage Type - Defines how goods should be stored after receipt. " +
            "Affects put-away rules and location assignment")
    private StorageTypeEnum storageType;

    @Comment("Abnormal Flag - Indicates if this inbound plan has any exceptions. " +
            "Used for exception handling and reporting")
    private boolean abnormal;

    @Column(nullable = false, length = 128)
    @Comment("Sender - Name or identifier of the shipping party. " +
            "Used for shipment verification and communication")
    private String sender = "";

    @Column(nullable = false, length = 128)
    @Comment("Carrier - Transportation company handling the delivery. " +
            "Used for delivery coordination and tracking")
    private String carrier = "";

    @Column(nullable = false, length = 64)
    @Comment("Shipping Method - Mode of transportation used. " +
            "Affects receiving preparation and handling requirements")
    private String shippingMethod = "";

    @Column(nullable = false, length = 128)
    @Comment("Tracking Number - Carrier shipment tracking number. " +
            "Used for shipment tracking and receipt verification")
    private String trackingNumber = "";

    @Column(nullable = false)
    @Comment("Estimated Arrival Date - Expected delivery timestamp (epoch millis). " +
            "Used for resource planning and scheduling")
    private Long estimatedArrivalDate = 0L;

    @Column(nullable = false)
    @Comment("Remarks - Additional notes about the inbound plan. " +
            "Used for special instructions or requirements")
    private String remark = "";

    @Column(nullable = false)
    @Comment("SKU Kind Number - Count of distinct SKUs in the order. " +
            "Used for planning receiving operations")
    private Integer skuKindNum;

    @Column(nullable = false)
    @Comment("Total Quantity - Total number of individual items expected. " +
            "Used for receipt verification and capacity planning")
    private Integer totalQty;

    @Column(nullable = false)
    @Comment("Total Boxes - Number of physical boxes/containers expected. " +
            "Used for space planning and handling preparation")
    private Integer totalBox;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'NEW'")
    @Comment("Inbound Plan Status - Current state in lifecycle: " +
            "NEW (新单据) -> ACCEPTING (收货中) -> ACCEPTED (收货完成) " +
            "Can be CANCEL (取消) or CLOSED (关闭) as terminal states")
    private InboundPlanOrderStatusEnum inboundPlanOrderStatus = InboundPlanOrderStatusEnum.NEW;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Extended Fields - Dynamic JSON map for additional attributes. " +
            "Stores custom fields without schema changes")
    private Map<String, Object> extendFields;

    @Version
    @Comment("Version - Optimistic locking mechanism for concurrent updates")
    private Long version;
}
