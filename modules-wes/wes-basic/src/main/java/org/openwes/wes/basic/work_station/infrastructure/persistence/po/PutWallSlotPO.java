package org.openwes.wes.basic.work_station.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.PutWallSlotStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_put_wall_slot",
        indexes = {
                @Index(unique = true, name = "uk_work_station_slot_code", columnList = "workStationId,putWallSlotCode"),
                @Index(name = "idx_put_wall_id", columnList = "putWallId"),
                @Index(name = "idx_picking_order_id", columnList = "pickingOrderId")
        }
)
@DynamicUpdate
public class PutWallSlotPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint(11) comment '工作站ID'")
    private Long workStationId;

    @Column(nullable = false, columnDefinition = "bigint(11) comment '播种墙ID'")
    private Long putWallId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '播种墙编码'")
    private String putWallCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '播种墙槽口编码'")
    private String putWallSlotCode;

    @Column(columnDefinition = "varchar(64) comment '电子标签号'")
    private String ptlTag;

    // it's define the put wall LEFT or RIGHT or MIDDLE
    @Column(nullable = false, columnDefinition = "varchar(64) comment '播种墙位置'")
    private String face;

    @Column(columnDefinition = "varchar(64) comment '所在层编码'")
    private String level;
    @Column(columnDefinition = "varchar(64) comment '所在列编码'")
    private String bay;

    @Column(columnDefinition = "int comment '所在层'")
    private Integer locLevel;
    @Column(columnDefinition = "int comment '所在列'")
    private Integer locBay;

    private boolean enable;

    @Column(nullable = false, columnDefinition = "bigint(11) comment '已分配拣选单ID'")
    private Long pickingOrderId = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(50) comment '槽口状态'")
    private PutWallSlotStatusEnum putWallSlotStatus;

    @Column(columnDefinition = "varchar(64) comment '槽口已绑容器编号'")
    private String transferContainerCode;
    @Column(columnDefinition = "bigint(11) comment '周转容器记录ID'")
    private Long transferContainerRecordId;
    @Version
    private Long version;
}
