package org.openwes.wes.outbound.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.outbound.constants.PickingOrderStatusEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_picking_order",
        indexes = {
                @Index(unique = true, name = "uk_picking_order_no", columnList = "pickingOrderNo"),
                @Index(name = "idx_received_user_account", columnList = "receivedUserAccount"),
                @Index(name = "idx_wave_no", columnList = "waveNo")
        }
)
@DynamicUpdate
public class PickingOrderPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "bigint comment '库区'")
    private Long warehouseAreaId;

    @Column(nullable = false, columnDefinition = "varchar(32) comment '波次号'")
    private String waveNo;

    @Column(nullable = false, columnDefinition = "varchar(32) comment '拣选单号'")
    private String pickingOrderNo;

    private int priority;

    private boolean shortOutbound;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private PickingOrderStatusEnum pickingOrderStatus = PickingOrderStatusEnum.NEW;

    private boolean isReallocatedOrder;

    /**
     * one picking order can be assigned to multiple station slot
     * <p>
     * Key is the station id
     * Value is the put wall slot code
     */
    @Column(columnDefinition = "json comment '分配的工作站格口'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, String> assignedStationSlot;

    @Column(columnDefinition = "varchar(64) comment '领取单据的用户账号'")
    private String receivedUserAccount;

    private boolean allowReceive;

    @Version
    private Long version;
}
