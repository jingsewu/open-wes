package org.openwes.wes.inbound.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.AuditUserPO;
import org.openwes.wes.api.inbound.constants.PutAwayTaskStatusEnum;
import org.openwes.wes.api.inbound.constants.PutAwayTaskTypeEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_put_away_task",
        indexes = {
                @Index(unique = true, name = "uk_order_no", columnList = "taskNo")
        }
)

@DynamicUpdate
public class PutAwayTaskPO extends AuditUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '任务编号'")
    private String taskNo;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(50) default '' comment '上架任务类型'")
    private PutAwayTaskTypeEnum taskType;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '仓库编码'")
    private String warehouseCode;

    @Column(nullable = false)
    private Long warehouseAreaId;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '工作台ID'")
    private Long workStationId;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '容器编码'")
    private String containerCode;
    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '容器规格'")
    private String containerSpecCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) default '' comment '上架任务状态'")
    private PutAwayTaskStatusEnum taskStatus;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '库位编码'")
    private String locationCode = "";

    @Column(columnDefinition = "json comment '扩展字段'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> extendFields;

    @Version
    private Long version;
}
