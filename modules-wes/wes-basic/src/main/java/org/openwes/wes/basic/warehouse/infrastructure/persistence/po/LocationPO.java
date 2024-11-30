package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.LocationStatusEnum;
import org.openwes.wes.api.basic.constants.LocationTypeEnum;
import org.openwes.wes.api.basic.dto.PositionDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
public class LocationPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '库位编码'")
    private String locationCode;

    @Column(columnDefinition = "varchar(64) comment '巷道编码'")
    private String aisleCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '货架编码'")
    private String shelfCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '库区ID'")
    private Long warehouseAreaId;

    @Column(columnDefinition = "bigint default 0 comment '逻辑区ID'")
    private Long warehouseLogicId;

    @Column(columnDefinition = "varchar(20) comment '库位类型'")
    @Enumerated(EnumType.STRING)
    private LocationTypeEnum locationType;

    @Column(columnDefinition = "varchar(64) comment '热度'")
    private String heat;

    private boolean occupied;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '库位状态'")
    private LocationStatusEnum locationStatus = LocationStatusEnum.PUT_AWAY_PUT_DOWN;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json comment '位置信息'")
    private PositionDTO position;

    @Version
    private long version;
}
