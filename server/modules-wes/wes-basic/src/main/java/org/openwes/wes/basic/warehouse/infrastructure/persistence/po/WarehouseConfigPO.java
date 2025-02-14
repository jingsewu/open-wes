package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.wes.api.config.dto.WarehouseMainDataConfigDTO;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EqualsAndHashCode(callSuper = true)
@Data
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "w_warehouse_config",
        indexes = {
                @Index(unique = true, name = "uk_warehouse_code", columnList = "warehouseCode")
        }
)
@DynamicUpdate
@Comment("Warehouse Configuration Management Table - Stores configuration details for warehouses.")
public class WarehouseConfigPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for the warehouse configuration record")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("Unique code for the warehouse")
    private String warehouseCode;

    @JdbcTypeCode(SqlTypes.JSON)
    @Comment("JSON object containing main data configuration for the warehouse")
    private WarehouseMainDataConfigDTO warehouseMainDataConfig;

    @Version
    @Comment("Optimistic locking version number")
    private long version;
}
