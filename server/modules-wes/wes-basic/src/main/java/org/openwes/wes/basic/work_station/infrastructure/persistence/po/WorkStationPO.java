package org.openwes.wes.basic.work_station.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.constants.WorkStationStatusEnum;
import org.openwes.wes.api.basic.dto.PositionDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_work_station",
        indexes = {
                @Index(unique = true, name = "uk_station_code_warehouse", columnList = "stationCode,warehouseCode,deleteTime")
        }
)
@DynamicUpdate
@Comment("Workstation Management Table - Stores detailed information about workstations and their statuses.")
public class WorkStationPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the workstation record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Code of the workstation")
    private String stationCode;

    @Column(nullable = false, length = 128)
    @Comment("Name of the workstation")
    private String stationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20) DEFAULT 'OFFLINE'")
    @Comment("Status of the workstation. Possible values are: " +
            "ONLINE (ONLINE - Online), " +
            "PAUSED (PAUSED - Paused), " +
            "OFFLINE (OFFLINE - Offline)")
    private WorkStationStatusEnum workStationStatus = WorkStationStatusEnum.OFFLINE;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this workstation belongs")
    private String warehouseCode;

    @Column(nullable = false)
    @Comment("ID of the warehouse area where this workstation belongs (Reference to w_warehouse_area table id)")
    private Long warehouseAreaId;

    @Enumerated(EnumType.STRING)
    @Column(length = 50, columnDefinition = "VARCHAR(50) DEFAULT 'RECEIVE'")
    @Comment("Operation mode of the workstation. Possible values are: " +
            "RECEIVE (RECEIVE - Receive), " +
            "SELECT_CONTAINER_PUT_AWAY (SELECT_CONTAINER_PUT_AWAY - Select container for put away), " +
            "RECOMMENDED_CONTAINER_PUT_AWAY (RECOMMENDED_CONTAINER_PUT_AWAY - Recommended container for put away), " +
            "PICKING (PICKING - Picking), " +
            "STOCKTAKE (STOCKTAKE - Stock take), " +
            "EMPTY_CONTAINER_OUTBOUND (EMPTY_CONTAINER_OUTBOUND - Empty container outbound), " +
            "ONE_STEP_RELOCATION (ONE_STEP_RELOCATION - One-step relocation), " +
            "TWO_STEP_RELOCATION (TWO_STEP_RELOCATION - Two-step relocation)")
    private WorkStationModeEnum workStationMode;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("List of allowed operation modes for the workstation (JSON format)")
    private List<WorkStationModeEnum> allowWorkStationModes;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Work locations of the workstation (JSON format)")
    private List<WorkStationDTO.WorkLocation<? extends WorkStationDTO.WorkLocationSlot>> workLocations;

    @Column
    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("Position information of the workstation (JSON format)")
    private PositionDTO position;

    @Column
    @Comment("Flag indicating if the workstation is enabled")
    private boolean enable;

    @Column
    @Comment("Flag indicating if the workstation is deleted")
    private boolean deleted;

    @Column(nullable = false)
    @Comment("Timestamp when the workstation was deleted, if applicable")
    private Long deleteTime = 0L;

    @Version
    @Comment("Optimistic locking version number")
    private Long version;
}
