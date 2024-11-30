package org.openwes.wes.ems.proxy.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.ems.proxy.dto.EmsLocationConfigDTO;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static org.hibernate.type.SqlTypes.JSON;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
@Table(name = "e_ems_location_config",
        indexes = {
                @Index(unique = true, name = "uk_location_code", columnList = "locationCode")
        })
public class EmsLocationConfigPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '点位编码'")
    private String locationCode;

    @JdbcTypeCode(JSON)
    @Column(nullable = false, length = 5000)
    @Comment("点位描述")
    private EmsLocationConfigDTO.LocationType locationType;

    @Column(nullable = false, columnDefinition = "bigint(11) comment '库区ID'")
    private Long warehouseAreaId;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '仓库'")
    private String warehouseCode;
}
