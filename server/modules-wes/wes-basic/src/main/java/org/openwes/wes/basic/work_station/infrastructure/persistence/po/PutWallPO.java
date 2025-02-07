package org.openwes.wes.basic.work_station.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.PutWallStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_put_wall",
        indexes = {
                @Index(unique = true, name = "uk_put_wall_work_station", columnList = "putWallCode,workStationId,deleteTime"),
                @Index(name = "idx_work_station", columnList = "workStationId")
        }
)
@DynamicUpdate
@Where(clause = "deleted=false")
@Comment("Put Wall Management Table - Stores detailed information about put walls and their statuses.")
public class PutWallPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for the put wall record")
    private Long id;

    @Column(nullable = false)
    @Comment("ID of the workstation where this put wall is located (Reference to w_work_station table id)")
    private Long workStationId;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this put wall is located")
    private String warehouseCode;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the put wall")
    private String putWallCode;

    @Column(nullable = false, length = 128)
    @Comment("Name of the put wall")
    private String putWallName;

    @Column(length = 128)
    @Comment("Location of the put wall")
    private String location = "";

    @Column(length = 64)
    @Comment("Specification code of the container used in the put wall")
    private String containerSpecCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @Comment("Status of the put wall. Possible values are: " +
            "IDLE (IDLE - 空闲), " +
            "WORKING (WORKING - 工作中)")
    private PutWallStatusEnum putWallStatus;

    @Column
    @Comment("Flag indicating if the put wall is deleted")
    private boolean deleted;

    @Column
    @Comment("Flag indicating if the put wall is enabled")
    private boolean enable;

    @Column(nullable = false)
    @Comment("Timestamp when the put wall was deleted, if applicable")
    private Long deleteTime = 0L;

    @Version
    @Comment("Optimistic locking version number")
    private Long version;
}
