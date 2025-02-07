package org.openwes.wes.basic.container.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.ContainerStatusEnum;
import org.openwes.wes.api.basic.dto.ContainerDTO;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_container",
        indexes = {
                @Index(unique = true, name = "uk_container_code_warehouse_code", columnList = "containerCode,warehouseCode"),
                @Index(name = "idx_container_spec_code", columnList = "containerSpecCode")
        }
)
@DynamicUpdate
@Comment("Container management table - tracks physical storage containers and their locations in warehouse")
public class ContainerPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for container")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Unique container identifier code")
    private String containerCode;

    @Column(nullable = false, length = 64)
    @Comment("Container specification/type code")
    private String containerSpecCode;

    @Column(nullable = false, length = 64)
    @Comment("Warehouse code where container is stored")
    private String warehouseCode;

    @Column(length = 64)
    @Comment("Warehouse area code for container location")
    private String warehouseAreaCode;

    @Column(nullable = false)
    @Comment("Warehouse area ID reference, default 0")
    private Long warehouseAreaId = 0L;

    @Column(length = 64)
    @Comment("Logical zone code within warehouse")
    private String warehouseLogicCode;

    @Column(nullable = false)
    @Comment("Logical zone ID reference, default 0")
    private Long warehouseLogicId = 0L;

    @Column(length = 64)
    @Comment("Storage location identifier within warehouse")
    private String locationCode;

    @Column(length = 64)
    @Comment("Type of storage location")
    private String locationType;

    @Column(nullable = false, precision = 18, scale = 6)
    @Comment("SKU volume occupation ratio, default 0")
    private BigDecimal occupationRatio = BigDecimal.ZERO;

    @Comment("Flag indicating if container is empty")
    private boolean emptyContainer;

    @Comment("Flag indicating if container is locked")
    private boolean locked;

    @Comment("Flag indicating if container is open")
    private boolean opened;

    @Column(nullable = false)
    @Comment("Total number of slots in container, default 0")
    private Integer containerSlotNum;

    @Column(nullable = false)
    @Comment("Number of empty slots available, default 0")
    private Integer emptySlotNum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Current container status: IN_SIDE=Container is in side,OUT_SIDE=Container is outside")
    private ContainerStatusEnum containerStatus;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Container slot configuration stored as JSON")
    private List<ContainerDTO.ContainerSlot> containerSlots;

    @Version
    @Comment("Optimistic locking version")
    private Long version;
}
