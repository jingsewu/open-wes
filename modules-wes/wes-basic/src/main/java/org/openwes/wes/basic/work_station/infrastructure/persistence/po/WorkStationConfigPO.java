package org.openwes.wes.basic.work_station.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.dto.WorkStationConfigDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_work_station_config",
        indexes = {
                @Index(name = "uk_work_station_id", columnList = "workStationId", unique = true)
        }
)
@DynamicUpdate
public class WorkStationConfigPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint(11) comment '工作站ID'")
    private Long workStationId;

    @Column(columnDefinition = "varchar(2048) comment '入库工作站配置'")
    @JdbcTypeCode(SqlTypes.JSON)
    private WorkStationConfigDTO.InboundStationConfigDTO inboundStationConfig;

    @Column(columnDefinition = "varchar(2048) comment '拣货工作站配置'")
    @JdbcTypeCode(SqlTypes.JSON)
    private WorkStationConfigDTO.PickingStationConfigDTO pickingStationConfig;

    @Column(columnDefinition = "varchar(2048) comment '盘点工作站配置'")
    @JdbcTypeCode(SqlTypes.JSON)
    private WorkStationConfigDTO.StocktakeStationConfigDTO stocktakeStationConfig;

    @Column(columnDefinition = "varchar(2048) comment '理库工作站配置'")
    @JdbcTypeCode(SqlTypes.JSON)
    private WorkStationConfigDTO.RelocationStationConfigDTO relocationStationConfig;

    @Version
    private Long version;
}
