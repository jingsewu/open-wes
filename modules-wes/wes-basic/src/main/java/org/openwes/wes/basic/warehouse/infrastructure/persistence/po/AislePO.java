package org.openwes.wes.basic.warehouse.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
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
public class AislePO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '巷道编码'")
    private String aisleCode;

    @Column(nullable = false, columnDefinition = "bigint default 0 comment '库区ID'")
    private Long warehouseAreaId;

    private boolean singleEntrance;
}
