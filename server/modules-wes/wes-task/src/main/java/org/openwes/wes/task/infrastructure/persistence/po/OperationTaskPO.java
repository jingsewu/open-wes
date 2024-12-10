package org.openwes.wes.task.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.task.constants.OperationTaskStatusEnum;
import org.openwes.wes.api.task.constants.OperationTaskTypeEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_operation_task",
        indexes = {
                @Index(name = "idx_status_source_container_code", columnList = "taskStatus,sourceContainerCode"),
                @Index(name = "idx_transfer_container_record_id", columnList = "transferContainerRecordId"),
                @Index(name = "idx_picking_order_id", columnList = "orderId,detailId")
        }
)
public class OperationTaskPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(20) comment '任务类型'")
    @Enumerated(EnumType.STRING)
    private OperationTaskTypeEnum taskType;

    @Column(nullable = false, columnDefinition = "int default 0 comment 'priority'")
    private Integer priority = 0;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'SKU ID'")
    private Long skuId;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '批次库存ID'")
    private Long skuBatchStockId;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '批次ID'")
    private Long skuBatchAttributeId;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '容器库存ID'")
    private Long containerStockId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '原容器编码'")
    private String sourceContainerCode;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '原容器面'")
    private String sourceContainerFace = "";
    @Column(nullable = false, columnDefinition = "varchar(64) comment '原容器格口编码'")
    private String sourceContainerSlot;

    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '箱号'")
    private String boxNo = "";

    @Column(nullable = false, columnDefinition = "bigint comment '工作站ID'")
    private Long workStationId = 0L;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编号'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "int(11) comment '需求数量'")
    private Integer requiredQty = 0;
    @Column(nullable = false, columnDefinition = "int(11) comment '操作数量'")
    private Integer operatedQty = 0;
    @Column(nullable = false, columnDefinition = "int(11) comment '异常数量'")
    private Integer abnormalQty = 0;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '目标库位'")
    private String targetLocationCode = "";
    @Column(nullable = false, columnDefinition = "varchar(64) comment '目标容器编码'")
    private String targetContainerCode = "";
    @Column(nullable = false, columnDefinition = "varchar(64) comment '周转容器记录ID'")
    private Long transferContainerRecordId = 0L;
    @Column(nullable = false, columnDefinition = "varchar(64) comment '目标容器格口编码'")
    private String targetContainerSlotCode = "";

    @Column(nullable = false, columnDefinition = "bigint comment '单据ID：可能是拣货单/理货单'")
    private Long orderId;

    @Column(nullable = false, length = 64)
    @Comment("订单号")
    private String orderNo;

    @Column(nullable = false, columnDefinition = "bigint comment '单据明细ID'")
    private Long detailId;

    /**
     * one picking order can be assigned to multiple station slot
     * <p>
     * Key is the station id
     * Value is the put wall slot code
     */
    @Column(columnDefinition = "json comment '分配的工作站格口'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<Long, String> assignedStationSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '任务状态'")
    private OperationTaskStatusEnum taskStatus = OperationTaskStatusEnum.NEW;

    private boolean abnormal;

    @Version
    private Long version;
}
