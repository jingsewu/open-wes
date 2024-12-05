package org.openwes.wes.basic.work_station.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
public class PutWallPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint(11) comment '工作站ID'")
    private Long workStationId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '播种墙编码'")
    private String putWallCode;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '播种墙名称'")
    private String putWallName;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '播种墙的位置'")
    private String location = "";

    @Column(nullable = false, columnDefinition = "varchar(64) comment '容器规格'")
    private String containerSpecCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50) comment '播种墙状态'")
    private PutWallStatusEnum putWallStatus;

    private boolean deleted;

    private boolean enable;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '删除时间'")
    private Long deleteTime = 0L;

    @Version
    private Long version;
}
