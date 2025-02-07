package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.WarehouseAreaTypeEnum;
import org.openwes.wes.api.basic.constants.WarehouseAreaUseEnum;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_warehouse_area",
        indexes = {
                @Index(unique = true, name = "uk_warehouse_area_group_code",
                        columnList = "warehouseAreaCode,warehouseGroupCode,warehouseCode,deleteTime")
        }
)
@DynamicUpdate
@Where(clause = "deleted=false")
@Comment("Warehouse Area Management Table - Stores detailed information about warehouse areas.")
public class WarehouseAreaPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for the warehouse area record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this area belongs")
    private String warehouseCode;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse group where this area belongs")
    private String warehouseGroupCode;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the warehouse area")
    private String warehouseAreaCode;

    @Column(nullable = false, length = 128)
    @Comment("Name of the warehouse area")
    private String warehouseAreaName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Type of the warehouse area. Possible values are: " +
            "STORAGE_AREA (STORAGE_AREA - 存储区), " +
            "STORAGE_CACHE (STORAGE_CACHE - 暂存区), " +
            "PICKING_STORAGE_CACHE (PICKING_STORAGE_CACHE - 下架暂存区), " +
            "ABNORMAL_AREA (ABNORMAL_AREA - 异常区)")
    private WarehouseAreaTypeEnum warehouseAreaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Usage of the warehouse area. Possible values are: " +
            "RECEIVE (RECEIVE - 收货), " +
            "PUT_AWAY_HOLDER (PUT_AWAY_HOLDER - 上架暂存), " +
            "PICK (PICK - 拣选)")
    private WarehouseAreaUseEnum warehouseAreaUse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Work type of the warehouse area. Possible values are: " +
            "MANUAL (MANUAL - 人工区), " +
            "ROBOT (ROBOT - 机器人区)")
    private WarehouseAreaWorkTypeEnum warehouseAreaWorkType;

    @Column(length = 500)
    @Comment("Additional remarks or notes about the warehouse area")
    private String remark;

    @Column
    @Comment("Level of the warehouse area")
    private int level;

    @Column
    @Comment("Temperature limit for the warehouse area")
    private int temperatureLimit;

    @Column
    @Comment("Humidity limit for the warehouse area")
    private int wetLimit;

    @Column
    @Comment("Flag indicating if the warehouse area is enabled")
    private boolean enable;

    @Column
    @Comment("Flag indicating if the warehouse area is deleted")
    private boolean deleted;

    @Column(nullable = false)
    @Comment("Timestamp when the warehouse area was deleted, if applicable")
    private Long deleteTime = 0L;

    @Version
    @Comment("Optimistic locking version number")
    private long version;
}
