package org.openwes.wes.inbound.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_put_away_task_detail",
        indexes = {
                @Index(name = "idx_put_away_task_id", columnList = "putAwayTaskId")
        }
)
@DynamicUpdate
@Comment("Put-Away Task Detail - Contains item-level information for put-away operations. " +
        "Links accepted goods to their final storage locations, tracking SKUs and quantities. " +
        "Connects acceptance process to put-away tasks.")
public class PutAwayTaskDetailPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Primary key - Unique identifier for each put-away task detail record")
    private Long id;

    @Column(nullable = false)
    @Comment("Accept Order ID - References the master acceptance order. " +
            "Links put-away details to the original receipt")
    private Long acceptOrderId;

    @Column(nullable = false)
    @Comment("Accept Order Detail ID - References the specific line item from acceptance. " +
            "Enables tracing from receipt to storage location")
    private Long acceptOrderDetailId;

    @Column(nullable = false, length = 64)
    @Comment("Owner Code - Identifies the owner of the goods being put away. " +
            "Used for multi-tenant inventory management")
    private String ownerCode;

    @Column(nullable = false)
    @Comment("Container ID - Internal identifier for the storage container")
    private Long containerId;

    @Column(nullable = false, length = 64)
    @Comment("Container Code - External identifier for the storage container. " +
            "Used for physical container tracking")
    private String containerCode;

    @Column(nullable = false, length = 64)
    @Comment("Container Slot Code - Specific compartment within the container. " +
            "Used for precise item placement")
    private String containerSlotCode;

    @Column(nullable = false, length = 64)
    @Comment("Container Face - Identifies which side/face of the container is used. " +
            "Important for accessibility and picking efficiency")
    private String containerFace;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column
    @Comment("Batch Attributes - JSON map storing batch-specific properties. " +
            "May include manufacture date, expiry date, lot number, etc.")
    private Map<String, Object> batchAttributes;

    @Column(nullable = false)
    @Comment("Put-Away Task ID - References the master put-away task. " +
            "Links detail records to the main task")
    private Long putAwayTaskId;

    @Column(nullable = false)
    @Comment("SKU ID - Internal identifier for the Stock Keeping Unit")
    private Long skuId;

    @Column(nullable = false, length = 64)
    @Comment("SKU Code - External identifier for the Stock Keeping Unit. " +
            "Used for inventory management and tracking")
    private String skuCode;

    @Column(nullable = false, length = 512)
    @Comment("SKU Name - Descriptive name of the Stock Keeping Unit. " +
            "Used for human readability and verification")
    private String skuName;

    @Column(nullable = false)
    @Comment("SKU Batch Attribute ID - References the batch characteristics. " +
            "Links to master batch attribute definitions")
    private Long skuBatchAttributeId;

    @Column(nullable = false)
    @Comment("Put-Away Quantity - Number of items being put away in this detail record. " +
            "Used for inventory updates and reconciliation")
    private Integer qtyPutAway;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Extended Fields - Dynamic JSON map for additional attributes. " +
            "Stores detail-specific data without schema changes")
    private Map<String, Object> extendFields;
}
