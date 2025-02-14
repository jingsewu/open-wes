package org.openwes.wes.basic.main.data.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "m_sku_main_data",
        indexes = {
                @Index(unique = true, name = "uk_sku_owner_warehouse", columnList = "skuCode,ownerCode,warehouseCode")
        }
)
@Comment("SKU Main Data Management Table - Stores detailed information about SKUs, including their attributes, dimensions, and classification.")
public class SkuMainDataPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the SKU main data record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the SKU")
    private String skuCode;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this SKU is stored (Reference to m_warehouse_main_data table warehouseCode)")
    private String warehouseCode;

    @Column(nullable = false, length = 64)
    @Comment("Code of the owner of this SKU (Reference to m_owner_main_data table ownerCode)")
    private String ownerCode;

    @Column(nullable = false, length = 128)
    @Comment("Name of the SKU")
    private String skuName;

    @Column(length = 64)
    @Comment("Style of the SKU")
    private String style;

    @Column(length = 64)
    @Comment("Color of the SKU")
    private String color;

    @Column(length = 64)
    @Comment("Size of the SKU")
    private String size;

    @Column(length = 64)
    @Comment("Brand of the SKU")
    private String brand;

    @Column(nullable = false)
    @Comment("Gross weight of the SKU")
    private Long grossWeight = 0L;

    @Column(nullable = false)
    @Comment("Net weight of the SKU")
    private Long netWeight = 0L;

    @Column(nullable = false)
    @Comment("Volume of the SKU")
    private Long volume = 0L;

    @Column(nullable = false)
    @Comment("Height of the SKU")
    private Long height = 0L;

    @Column(nullable = false)
    @Comment("Width of the SKU")
    private Long width = 0L;

    @Column(nullable = false)
    @Comment("Length of the SKU")
    private Long length = 0L;

    @Column()
    @Comment("URL of the image for the SKU")
    private String imageUrl;

    @Column(length = 64)
    @Comment("Unit of measurement for the SKU")
    private String unit;

    @Column(length = 64)
    @Comment("First-level category of the SKU")
    private String skuFirstCategory;

    @Column(length = 64)
    @Comment("Second-level category of the SKU")
    private String skuSecondCategory;

    @Column(length = 64)
    @Comment("Third-level category of the SKU")
    private String skuThirdCategory;

    @Column(length = 64)
    @Comment("First-level attribute category of the SKU")
    private String skuAttributeCategory;

    @Column(length = 64)
    @Comment("Second-level attribute sub-category of the SKU")
    private String skuAttributeSubCategory;

    @Column
    @Comment("Flag indicating if serial number tracking is enabled for the SKU")
    private boolean enableSn;

    @Column
    @Comment("Flag indicating if effective date tracking is enabled for the SKU")
    private boolean enableEffective;

    @Column(nullable = false)
    @Comment("Shelf life of the SKU in days")
    private Integer shelfLife = 0;

    @Column(nullable = false)
    @Comment("Effective days limit for the SKU")
    private Integer effectiveDays = 0;

    @Column(length = 64)
    @Comment("Barcode rule code for the SKU")
    private String barcodeRuleCode;

    @Column(length = 64)
    @Comment("Heat level of the SKU")
    private String heat;

    @Column
    @Comment("Maximum stock level for the SKU")
    private Integer maxStock;

    @Column
    @Comment("Minimum stock level for the SKU")
    private Integer minStock;

    @Column
    @Comment("Flag indicating if heat calculation is enabled for the SKU")
    private boolean calculateHeat;

    @Column
    @Comment("Flag indicating if the SKU does not require a barcode")
    private boolean noBarcode;

    @Version
    @Comment("Optimistic locking version number")
    private Long version;
}
