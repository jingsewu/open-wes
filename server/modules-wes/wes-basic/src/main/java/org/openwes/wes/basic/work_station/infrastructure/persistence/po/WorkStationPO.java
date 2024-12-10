package org.openwes.wes.basic.work_station.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.WorkStationModeEnum;
import org.openwes.wes.api.basic.constants.WorkStationStatusEnum;
import org.openwes.wes.api.basic.dto.PositionDTO;
import org.openwes.wes.api.basic.dto.WorkStationDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Where;
import org.hibernate.type.SqlTypes;
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
@Where(clause = "deleted=false")
public class WorkStationPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '工作站编码'")
    private String stationCode;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '工作站编码'")
    private String stationName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private WorkStationStatusEnum workStationStatus = WorkStationStatusEnum.OFFLINE;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;
    @Column(nullable = false, columnDefinition = "bigint comment '库区ID'")
    private Long warehouseAreaId;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50) comment '操作类型'")
    private WorkStationModeEnum workStationMode;

    @Column(columnDefinition = "json comment '工作站允许的操作'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<WorkStationModeEnum> allowWorkStationModes;

    @Column(columnDefinition = "json comment '工作站工作位'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<WorkStationDTO.WorkLocation<? extends WorkStationDTO.WorkLocationSlot>> workLocations;

    @Column(columnDefinition = "json comment '工作站位置'")
    @JdbcTypeCode(SqlTypes.JSON)
    private PositionDTO position;

    private boolean enable;

    private boolean deleted;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '删除时间'")
    private Long deleteTime = 0L;

    @Version
    private Long version;
}
