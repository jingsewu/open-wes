package org.openwes.wes.basic.container.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.wes.api.task.constants.TransferContainerRecordStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_transfer_container_record",
        indexes = {
                @Index(name = "uk_container_order_order_status", columnList = "transferContainerCode,pickingOrderId,sealTime", unique = true),
                @Index(name = "idx_order", columnList = "pickingOrderId")
        }
)
@Comment("Transfer Container Record Management Table - Tracks records of transfer containers, including their status, destination, and related order information.")
public class TransferContainerRecordPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the transfer container record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the transfer container")
    private String transferContainerCode;

    @Column(length = 64)
    @Comment("ID of the workstation associated with this transfer container record")
    private Long workStationId;

    @Column(nullable = false)
    @Comment("ID of the picking order associated with this transfer container record")
    private Long pickingOrderId;

    @Column(nullable = false, length = 64)
    @Comment("Code of the put wall slot where the transfer container is destined")
    private String putWallSlotCode;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this transfer container is used")
    private String warehouseCode;

    @Column(nullable = false)
    @Comment("Index indicating the sequence of the transfer container in a series")
    private Integer containerIndex = 0;

    @Column(length = 128)
    @Comment("Destination of the transfer container")
    private String destination = "";

    @Column(nullable = false)
    @Comment("Timestamp when the transfer container was sealed")
    private Long sealTime = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20)")
    @Comment("Status of the transfer container record. Possible values are: " +
            "BOUNDED (BOUNDED - 绑定), " +
            "SEALED (SEALED - 已封箱)")
    private TransferContainerRecordStatusEnum transferContainerStatus;

    @Version
    @Comment("Optimistic locking version number")
    private Long version;
}
