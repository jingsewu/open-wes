package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_aisle",
        indexes = {
                @Index(unique = true, name = "uk_aisle_code_warehouse_area", columnList = "aisleCode,warehouseAreaId")
        }
)
@DynamicUpdate
@Comment("Aisle Management Table - Stores detailed information about aisles within warehouse areas.")
public class AislePO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the aisle record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the aisle")
    private String aisleCode;

    @Column(nullable = false)
    @Comment("ID of the warehouse area where this aisle is located (Reference to w_warehouse_area table id)")
    private Long warehouseAreaId;

    @Column
    @Comment("Flag indicating if the aisle has a single entrance")
    private boolean singleEntrance;
}
