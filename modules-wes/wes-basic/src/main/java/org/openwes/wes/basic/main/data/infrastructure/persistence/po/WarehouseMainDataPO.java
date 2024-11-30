package org.openwes.wes.basic.main.data.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.wes.api.main.data.constants.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "m_warehouse_main_data",
        indexes = {
                @Index(unique = true, name = "uk_warehouse_code", columnList = "warehouseCode")
        }
)
public class WarehouseMainDataPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库编码'")
    private String warehouseCode;
    @Column(nullable = false, columnDefinition = "varchar(128) comment '仓库名称'")
    private String warehouseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '仓库类型'")
    private WarehouseTypeEnum warehouseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '仓库属性'")
    private WarehouseAttrTypeEnum warehouseAttrType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '仓库等级'")
    private WarehouseLevelEnum warehouseLevel;

    @Column(columnDefinition = "varchar(64) comment '仓库标签'")
    private String warehouseLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '主营业务'")
    private WarehouseBusinessTypeEnum businessType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '仓库结构'")
    private WarehouseStructureTypeEnum structureType;

    @Column(nullable = false, columnDefinition = "int default 0 comment '仓库面积(m²)'")
    private Integer area = 0;

    @Column(nullable = false, columnDefinition = "int default 0 comment '层高(m)'")
    private Integer height = 0;
    private boolean virtualWarehouse;

    @Column(columnDefinition = "varchar(64) comment '联系人'")
    private String name;
    @Column(columnDefinition = "varchar(64) comment '联系人电话'")
    private String tel;
    @Column(columnDefinition = "varchar(64) comment '联系邮箱'")
    private String mail;
    @Column(columnDefinition = "varchar(64) comment '传真'")
    private String fax;

    @Column(columnDefinition = "varchar(64) comment '国家'")
    private String country;
    @Column(columnDefinition = "varchar(64) comment '省'")
    private String province;
    @Column(columnDefinition = "varchar(64) comment '市'")
    private String city;
    @Column(columnDefinition = "varchar(64) comment '区'")
    private String district;
    @Column(columnDefinition = "varchar(255) comment '详细地址'")
    private String address;

    @Version
    private Long version;
}
