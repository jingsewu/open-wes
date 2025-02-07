package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import org.openwes.common.utils.base.UpdateUserPO;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_warehouse_area_group",
        indexes = {
                @Index(unique = true, name = "uk_warehouse_area_group_code",
                        columnList = "warehouseAreaGroupCode,warehouseCode,deleteTime")
        }
)
@DynamicUpdate
@Where(clause = "deleted=false")
@Comment("Warehouse Area Group Management Table - Stores information about groups of warehouse areas.")
public class WarehouseAreaGroupPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for the warehouse area group record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this area group belongs")
    private String warehouseCode;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the warehouse area group")
    private String warehouseAreaGroupCode;

    @Column(nullable = false, length = 128)
    @Comment("Name of the warehouse area group")
    private String warehouseAreaGroupName;

    @Column(length = 500)
    @Comment("Additional remarks or notes about the warehouse area group")
    private String remark;

    @Column
    @Comment("Flag indicating if the warehouse area group is enabled")
    private boolean enable;

    @Column
    @Comment("Flag indicating if the warehouse area group is deleted")
    private boolean deleted;

    @Column(nullable = false)
    @Comment("Timestamp when the warehouse area group was deleted, if applicable")
    private Long deleteTime = 0L;

    @Version
    @Comment("Optimistic locking version number")
    private long version;
}
