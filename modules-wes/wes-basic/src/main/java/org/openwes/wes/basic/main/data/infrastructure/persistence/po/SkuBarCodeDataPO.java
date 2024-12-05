package org.openwes.wes.basic.main.data.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
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
public class SkuBarCodeDataPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigint comment 'skuID'")
    private Long skuId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment 'sku编码'")
    private String skuCode;

    @Column(nullable = false, columnDefinition = "varchar(255) comment '商品条码'")
    private String barCode;
}
