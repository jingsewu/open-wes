package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.basic.constants.WarehouseAreaTypeEnum;
import org.openwes.wes.api.basic.constants.WarehouseAreaUseEnum;
import org.openwes.wes.api.basic.constants.WarehouseAreaWorkTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "w_warehouse_area",
        indexes = {
                @Index(unique = true, name = "uk_warehouse_area_group_code",
                        columnList = "warehouseAreaCode,warehouseGroupCode,warehouseCode,deleteTime")
        }
)
@DynamicUpdate
@Where(clause = "deleted=false")
public class WarehouseAreaPO extends UpdateUserPO {


    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓区编码'")
    private String warehouseGroupCode;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '库区编码'")
    private String warehouseAreaCode;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '库区名称'")
    private String warehouseAreaName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '库区类型'")
    private WarehouseAreaTypeEnum warehouseAreaType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '库区用途'")
    private WarehouseAreaUseEnum warehouseAreaUse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '库区工作类型'")
    private WarehouseAreaWorkTypeEnum warehouseAreaWorkType;

    @Column(columnDefinition = "varchar(500) comment '备注'")
    private String remark;

    private int level;
    private int temperatureLimit;
    private int wetLimit;

    private boolean enable;
    private boolean deleted;
    @Column(nullable = false, columnDefinition = "bigint default 0 comment '删除时间'")
    private Long deleteTime = 0L;

    @Version
    private long version;
}
