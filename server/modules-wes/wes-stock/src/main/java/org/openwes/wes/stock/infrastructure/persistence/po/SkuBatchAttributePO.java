package org.openwes.wes.stock.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
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
        name = "w_sku_batch_attribute",
        indexes = {
                @Index(name = "idx_sku_batch_no", columnList = "skuId,batchNo", unique = true)
        }
)
@DynamicUpdate
public class SkuBatchAttributePO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment 'skuId'")
    private Long skuId;

    @Column(columnDefinition = "json comment '批次属性'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> skuAttributes;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '批次号'")
    private String batchNo;

    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute1;
    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute2;
    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute3;
    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute4;
    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute5;
    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute6;
    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute7;
    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute8;
    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute9;
    @Column(columnDefinition = "varchar(128) comment '批次属性预留'")
    private String skuAttribute10;

    @Version
    private Long version;
}
