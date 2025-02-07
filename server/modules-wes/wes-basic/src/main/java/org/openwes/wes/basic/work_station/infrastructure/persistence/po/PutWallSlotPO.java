package org.openwes.wes.basic.work_station.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_put_wall_slot",
        indexes = {
                @Index(unique = true, name = "uk_work_station_slot_code", columnList = "workStationId,putWallSlotCode"),
                @Index(name = "idx_put_wall_id", columnList = "putWallId"),
                @Index(name = "idx_picking_order_id", columnList = "pickingOrderId")
        }
)
@DynamicUpdate
@Comment("Put Wall Slot Management Table - Stores detailed information about slots in put walls.")
public class PutWallSlotPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for the put wall slot record")
    private Long id;

    @Column(nullable = false)
    @Comment("ID of the workstation where this put wall slot is located (Reference to w_work_station table id)")
    private Long workStationId;

    @Column(nullable = false)
    @Comment("ID of the put wall where this slot belongs (Reference to w_put_wall table id)")
    private Long putWallId;

    @Column(nullable = false, length = 64)
    @Comment("Code of the put wall where this slot belongs")
    private String putWallCode;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the put wall slot")
    private String putWallSlotCode;

    @Column(length = 64)
    @Comment("Electronic tag number for the put wall slot")
    private String ptlTag;

    @Column(nullable = false, length = 64)
    @Comment("Position of the put wall (LEFT, RIGHT, MIDDLE)")
    private String face;

    @Column(length = 64)
    @Comment("Code of the level where the slot is located")
    private String level;

    @Column(length = 64)
    @Comment("Code of the bay where the slot is located")
    private String bay;

    @Column
    @Comment("Level number where the slot is located")
    private Integer locLevel;

    @Column
    @Comment("Bay number where the slot is located")
    private Integer locBay;

    @Column
    @Comment("Flag indicating if the put wall slot is enabled")
    private boolean enable;

    @Column(nullable = false)
    @Comment("ID of the picking order assigned to this slot (Reference to w_picking_order table id)")
    private Long pickingOrderId = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Comment("Status of the put wall slot. Possible values are: " +
            "IDLE (IDLE - 空闲), " +
            "WAITING_BINDING (WAITING_BINDING - 待绑定), " +
            "BOUND (BOUND - 已绑定), " +
            "DISPATCH (DISPATCH - 待分拨), " +
            "WAITING_SEAL (WAITING_SEAL - 待封箱)")
    private PutWallSlotStatusEnum putWallSlotStatus;

    @Column(length = 64)
    @Comment("Code of the transfer container bound to this slot")
    private String transferContainerCode;

    @Column
    @Comment("ID of the transfer container record bound to this slot (Reference to w_transfer_container_record table id)")
    private Long transferContainerRecordId;

    @Version
    @Comment("Optimistic locking version number")
    private Long version;
}
