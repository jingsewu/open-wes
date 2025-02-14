package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Where;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
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
@Comment("Warehouse Logic Management Table - Stores detailed information about warehouse logic zones.")
public class WarehouseLogicPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the warehouse logic zone record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Code of the warehouse where this logic zone belongs")
    private String warehouseCode;

    @Column(nullable = false)
    @Comment("ID of the warehouse area where this logic zone belongs (Reference to w_warehouse_area table id)")
    private Long warehouseAreaId;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the warehouse logic zone")
    private String warehouseLogicCode;

    @Column(nullable = false, length = 128)
    @Comment("Name of the warehouse logic zone")
    private String warehouseLogicName;

    @Column(length = 500)
    @Comment("Additional remarks or notes about the warehouse logic zone")
    private String remark;

    @Column
    @Comment("Flag indicating if the warehouse logic zone is enabled")
    private boolean enable;

    @Column
    @Comment("Flag indicating if the warehouse logic zone is deleted")
    private boolean deleted;

    @Column(nullable = false)
    @Comment("Timestamp when the warehouse logic zone was deleted, if applicable")
    private Long deleteTime = 0L;

    @Version
    @Comment("Optimistic locking version number")
    private long version;
}
