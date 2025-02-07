package org.openwes.wes.basic.main.data.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
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
@Comment("Warehouse Main Data Management Table - Stores detailed information about warehouses, including their attributes, location, and contact details.")
public class WarehouseMainDataPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for the warehouse main data record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the warehouse")
    private String warehouseCode;

    @Column(nullable = false, length = 128)
    @Comment("Name of the warehouse")
    private String warehouseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Type of the warehouse. Possible values are: " +
            "CENTER_WAREHOUSE (CENTER_WAREHOUSE - 中央仓库), " +
            "DELIVERY_WAREHOUSE (DELIVERY_WAREHOUSE - 分发仓), " +
            "TRANSIT_WAREHOUSE (TRANSIT_WAREHOUSE - 中转仓), " +
            "HUB_WAREHOUSE (HUB_WAREHOUSE - 枢纽仓), " +
            "FACTORY_WAREHOUSE (FACTORY_WAREHOUSE - 工厂仓), " +
            "RETURN_WAREHOUSE (RETURN_WAREHOUSE - 退货仓), " +
            "FRONT_WAREHOUSE (FRONT_WAREHOUSE - 前置仓)")
    private WarehouseTypeEnum warehouseType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Attribute type of the warehouse. Possible values are: " +
            "NORMAL (NORMAL - 正常), " +
            "COLD_CHAIN (COLD_CHAIN - 冷链), " +
            "DANGEROUS (DANGEROUS - 危险), " +
            "BONDED (BONDED - 保税)")
    private WarehouseAttrTypeEnum warehouseAttrType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Level of the warehouse. Possible values are: " +
            "A (A), " +
            "B (B), " +
            "C (C), " +
            "D (D), " +
            "E (E), " +
            "F (F)")
    private WarehouseLevelEnum warehouseLevel;

    @Column(length = 64)
    @Comment("Label for the warehouse")
    private String warehouseLabel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Main business type of the warehouse. Possible values are: " +
            "TOB (TOB - TOB), " +
            "TOC (TOC - TOC), " +
            "RETURN (RETURN - 退货), " +
            "CROSS_BORDER (CROSS_BORDER - 越库), " +
            "CONSUMABLES (CONSUMABLES - 耗材)")
    private WarehouseBusinessTypeEnum businessType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Comment("Structure type of the warehouse. Possible values are: " +
            "STEEL (STEEL), " +
            "FLOOR (FLOOR), " +
            "FLAT (FLAT), " +
            "OUTSIDE (OUTSIDE)")
    private WarehouseStructureTypeEnum structureType;

    @Column(nullable = false)
    @Comment("Area of the warehouse in square meters")
    private Integer area = 0;

    @Column(nullable = false)
    @Comment("Height of the warehouse in meters")
    private Integer height = 0;

    @Column
    @Comment("Flag indicating if the warehouse is virtual")
    private boolean virtualWarehouse;

    @Column(length = 64)
    @Comment("Contact person name")
    private String name;

    @Column(length = 64)
    @Comment("Contact person telephone number")
    private String tel;

    @Column(length = 64)
    @Comment("Contact person email address")
    private String mail;

    @Column(length = 64)
    @Comment("Contact person fax number")
    private String fax;

    @Column(length = 64)
    @Comment("Country part of the warehouse address")
    private String country;

    @Column(length = 64)
    @Comment("Province part of the warehouse address")
    private String province;

    @Column(length = 64)
    @Comment("City part of the warehouse address")
    private String city;

    @Column(length = 64)
    @Comment("District part of the warehouse address")
    private String district;

    @Column
    @Comment("Detailed address of the warehouse")
    private String address;

    @Version
    @Comment("Optimistic locking version number")
    private Long version;
}
