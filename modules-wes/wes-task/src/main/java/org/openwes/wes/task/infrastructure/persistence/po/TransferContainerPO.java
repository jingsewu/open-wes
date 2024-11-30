package org.openwes.wes.task.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.task.constants.TransferContainerStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

import static org.hibernate.type.SqlTypes.JSON;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_transfer_container",
        indexes = {
                @Index(name = "uk_container_code_warehouse", columnList = "transferContainerCode,warehouseCode", unique = true),
                @Index(name = "idx_update_time", columnList = "updateTime")
        }
)
public class TransferContainerPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '周转容器编码'")
    private String transferContainerCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编号'")
    private String warehouseCode;

    @Column(columnDefinition = "varchar(64) comment '周转容器规格'")
    private String containerSpecCode = "";

    @Column(nullable = false, columnDefinition = "bigint(11) comment '最后一次工作的库区'")
    private Long warehouseAreaId = 0L;

    @Column(columnDefinition = "varchar(64) comment '点位编码'")
    private String locationCode;

    private boolean virtualContainer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(32) comment '状态'")
    private TransferContainerStatusEnum transferContainerStatus;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '锁定时间'")
    private Long lockedTime = 0L;

    @Comment("表示一个周期内关联的周转容器记录(TransferContainerRecord)")
    @JdbcTypeCode(JSON)
    private List<Long> currentPeriodRelateRecordIds;

    @Version
    private Long version;
}
