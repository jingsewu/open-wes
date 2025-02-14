package org.openwes.wes.basic.main.data.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "m_sku_barcode_data",
        indexes = {
                @Index(unique = true, name = "uk_sku_id_and_barcode", columnList = "skuId,barCode"),
                @Index(name = "idx_barcode", columnList = "barCode")
        }
)
@Comment("SKU Barcode Data Management Table - Stores barcode information associated with SKUs.")
public class SkuBarCodeDataPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the SKU barcode data record")
    private Long id;

    @Column(nullable = false)
    @Comment("Unique identifier of the SKU (Reference to m_sku_main_data table id)")
    private Long skuId;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the SKU")
    private String skuCode;

    @Column(nullable = false, length = 255)
    @Comment("Barcode associated with the SKU")
    private String barCode;
}
