package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.LocationStatusEnum;
import org.openwes.wes.api.basic.constants.LocationTypeEnum;
import org.openwes.wes.api.basic.dto.PositionDTO;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_location",
        indexes = {
                @Index(unique = true, name = "uk_warehouse_area_location", columnList = "locationCode,warehouseAreaId"),
                @Index(name = "idx_aisle_warehouse_area", columnList = "aisleCode,warehouseAreaId"),
                @Index(name = "idx_warehouse_area_id", columnList = "warehouseAreaId"),
                @Index(name = "idx_warehouse_logic_id", columnList = "warehouseLogicId"),
                @Index(name = "idx_shelf_code", columnList = "shelfCode")
        }
)
@DynamicUpdate
@Comment("Location Management Table - Stores detailed information about storage locations within warehouse areas.")
public class LocationPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for the location record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the storage location")
    private String locationCode;

    @Column(length = 64)
    @Comment("Code of the aisle where this location is situated")
    private String aisleCode;

    @Column(nullable = false, length = 64)
    @Comment("Code of the shelf where this location is situated")
    private String shelfCode;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this location is situated")
    private String warehouseCode;

    @Column(nullable = false)
    @Comment("ID of the warehouse area where this location is situated (Reference to w_warehouse_area table id)")
    private Long warehouseAreaId;

    @Column
    @Comment("ID of the logic zone where this location is situated (Reference to w_warehouse_logic table id)")
    private Long warehouseLogicId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    @Comment("Type of the storage location. Possible values are: " +
            "RACK")
    private LocationTypeEnum locationType;

    @Column(length = 64)
    @Comment("Heat level of the storage location")
    private String heat;

    @Column
    @Comment("Flag indicating if the storage location is occupied")
    private boolean occupied;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Status of the storage location. Possible values are: " +
            "PUT_AWAY_ONLY (PUT_AWAY_ONLY - 仅上架), " +
            "TAKE_OFF_ONLY (TAKE_OFF_ONLY - 仅下架), " +
            "PUT_AWAY_PUT_DOWN (PUT_AWAY_PUT_DOWN - 上架&下架), " +
            "NONE (NONE - 禁用)")
    private LocationStatusEnum locationStatus = LocationStatusEnum.PUT_AWAY_PUT_DOWN;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    @Comment("Position information of the storage location stored as JSON")
    private PositionDTO position;

    @Version
    @Comment("Optimistic locking version number")
    private long version;
}
