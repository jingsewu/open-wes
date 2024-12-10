package org.openwes.wes.task.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.task.constants.TransferContainerRecordStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_transfer_container_record",
        indexes = {
                // only binding the same transferContainerCode after it sealed.
                @Index(name = "uk_container_order_order_status", columnList = "transferContainerCode,pickingOrderId,sealTime", unique = true),
                // for manual area picking order query
                @Index(name = "idx_order", columnList = "pickingOrderId")
        }
)
public class TransferContainerRecordPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '周转容器编码'")
    private String transferContainerCode;

    @Column(columnDefinition = "bigint(11) comment '工作站ID'")
    private Long workStationId;

    @Column(nullable = false, columnDefinition = "bigint(11) comment '拣选订单ID'")
    private Long pickingOrderId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '播种墙格口'")
    private String putWallSlotCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "int(11) comment '第几个周转箱'")
    private Integer containerIndex = 0;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '周转箱目的地'")
    private String destination = "";

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '封箱时间'")
    private Long sealTime = 0L;

    @Column(nullable = false, columnDefinition = "varchar(20) comment '周转箱记录状态'")
    @Enumerated(EnumType.STRING)
    private TransferContainerRecordStatusEnum transferContainerStatus;

    @Version
    private Long version;
}
