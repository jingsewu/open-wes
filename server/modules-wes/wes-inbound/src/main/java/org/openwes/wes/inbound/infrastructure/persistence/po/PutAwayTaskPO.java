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
import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import org.openwes.wes.api.inbound.constants.PutAwayTaskTypeEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_put_away_task",
        indexes = {
                @Index(unique = true, name = "uk_order_no", columnList = "taskNo")
        }
)
@DynamicUpdate
@Comment("Put-Away Task - Manages the movement of received goods to their final storage locations. " +
        "Tracks container movements, workstation assignments, and task completion status. " +
        "Part of the inbound process after goods acceptance.")
public class PutAwayTaskPO extends AuditUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Primary key - Unique identifier for each put-away task")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Task Number - Unique business identifier for the put-away task. " +
            "Used for operation tracking and reference. Must be unique across all tasks")
    private String taskNo;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, columnDefinition = "varchar(50)")
    @Comment("Task Type - Categorizes the put-away operation type. " +
            "Determines handling procedures and system behavior")
    private PutAwayTaskTypeEnum taskType;

    @Column(nullable = false, length = 64)
    @Comment("Warehouse Code - Identifies the warehouse where put-away occurs. " +
            "Controls location assignment and operation boundaries")
    private String warehouseCode;

    @Column(nullable = false)
    @Comment("Warehouse Area ID - Specific zone/area within the warehouse. " +
            "Used for location planning and optimization")
    private Long warehouseAreaId;

    @Column(nullable = false)
    @Comment("Workstation ID - Identifies the physical workstation assigned to this task. " +
            "Used for task assignment and operation tracking")
    private Long workStationId;

    @Column(nullable = false, length = 64)
    @Comment("Container Code - Identifier for the physical container being moved. " +
            "Used to track the specific unit being put away")
    private String containerCode;

    @Column(nullable = false, length = 64)
    @Comment("Container Specification Code - Defines the type and characteristics of the container. " +
            "Used for compatibility checking with storage locations")
    private String containerSpecCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    @Comment("Put away task Status - Current state in lifecycle: " +
            "NEW (新单据) -> PUTTING_AWAY (上架中) -> PUTTED_AWAY (上架完成) ")
    private PutAwayTaskStatusEnum taskStatus;

    @Column(nullable = false, length = 64)
    @Comment("Location Code - Target storage location for the container. " +
            "Represents the final destination for the put-away operation")
    private String locationCode = "";

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Extended Fields - Dynamic JSON map for additional attributes. " +
            "Stores task-specific data without requiring schema changes")
    private Map<String, Object> extendFields;

    @Version
    @Comment("Version - Optimistic locking mechanism for concurrent updates")
    private Long version;
}
