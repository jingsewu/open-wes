package org.openwes.wes.inbound.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_put_away_task_detail",
        indexes = {
                @Index(name = "idx_put_away_task_id", columnList = "putAwayTaskId")
        }
)
@DynamicUpdate
public class PutAwayTaskDetailPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false)
    @Comment("accept order id")
    private Long acceptOrderId;

    @Column(nullable = false)
    @Comment("accept order detail id")
    private Long acceptOrderDetailId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '货主编码'")
    private String ownerCode;

    @Column(nullable = false)
    private Long containerId;
    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '容器编码'")
    private String containerCode;
    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '容器格口'")
    private String containerSlotCode;
    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment '容器面'")
    private String containerFace;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "json comment '批次属性'")
    private Map<String, Object> batchAttributes;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '上架任务ID'")
    private Long putAwayTaskId;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'SKU id'")
    private Long skuId;
    @Column(nullable = false, columnDefinition = "varchar(64) default '' comment 'SKU 代码'")
    private String skuCode;
    @Column(nullable = false, columnDefinition = "varchar(512) default '' comment 'SKU 名称'")
    private String skuName;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '批次属性 ID'")
    private Long skuBatchAttributeId;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '上架数量'")
    private Integer qtyPutAway;

    @Column(columnDefinition = "json comment '扩展字段'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> extendFields;
}
