package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_warehouse_logic",
        indexes = {
                @Index(unique = true, name = "uk_warehouse_logic_area", columnList = "warehouseAreaId,warehouseLogicCode,deleteTime")
        }
)
@DynamicUpdate
@Where(clause = "deleted=false")
public class WarehouseLogicPO extends UpdateUserPO {


    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '库区ID'")
    private Long warehouseAreaId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '逻辑区编码'")
    private String warehouseLogicCode;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '逻辑区名称'")
    private String warehouseLogicName;

    @Column(columnDefinition = "varchar(500) comment '备注'")
    private String remark;

    private boolean enable;
    private boolean deleted;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '备注'")
    private Long deleteTime = 0L;

    @Version
    private long version;
}
