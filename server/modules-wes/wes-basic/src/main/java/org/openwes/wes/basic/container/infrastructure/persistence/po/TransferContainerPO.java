package org.openwes.wes.basic.container.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

import static org.hibernate.type.SqlTypes.JSON;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_transfer_container",
        indexes = {
                @Index(name = "uk_container_code_warehouse", columnList = "transferContainerCode,warehouseCode", unique = true),
                @Index(name = "idx_update_time", columnList = "updateTime")
        }
)
@Comment("Transfer Container Management Table - Stores details of transfer containers, including their status, location, and related records.")
public class TransferContainerPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for the transfer container")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the transfer container")
    private String transferContainerCode;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this transfer container is used")
    private String warehouseCode;

    @Column(length = 64)
    @Comment("Specification code of the transfer container")
    private String containerSpecCode = "";

    @Column(nullable = false)
    @Comment("ID of the last warehouse area where the container was used")
    private Long warehouseAreaId = 0L;

    @Column(length = 64)
    @Comment("Location code where the transfer container is currently located")
    private String locationCode;

    @Comment("Flag indicating if the container is virtual")
    private boolean virtualContainer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    @Comment("Current status of the transfer container. Possible values are: " +
            "IDLE (IDLE - 空闲), " +
            "OCCUPANCY (OCCUPANCY - 占用), " +
            "LOCKED (LOCKED - 锁定) ")
    private TransferContainerStatusEnum transferContainerStatus;

    @Column(nullable = false)
    @Comment("Timestamp when the container was last locked")
    private Long lockedTime = 0L;

    @JdbcTypeCode(JSON)
    @Comment("List of IDs of related transfer container records for the current period")
    private List<Long> currentPeriodRelateRecordIds;

    @Version
    @Comment("Optimistic locking version number")
    private Long version;
}
