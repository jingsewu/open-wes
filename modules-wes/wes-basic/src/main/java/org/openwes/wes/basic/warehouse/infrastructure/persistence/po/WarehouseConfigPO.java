package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.config.dto.WarehouseMainDataConfigDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
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
public class WarehouseConfigPO extends UpdateUserPO {


    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    private String warehouseCode;

    @JdbcTypeCode(SqlTypes.JSON)
    private WarehouseMainDataConfigDTO warehouseMainDataConfig;

    @Version
    private long version;
}
