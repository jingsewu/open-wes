package org.openwes.wes.config.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.config.dto.BatchAttributeConfigDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "m_batch_attribute_config",
    indexes = {
        @Index(unique = true, name = "uk_batch_attribute_config_code", columnList = "code")
    }
)
public class BatchAttributeConfigPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '编码'")
    private String code;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '名称'")
    private String name;

    @Column(columnDefinition = "varchar(64) comment '货主编码'")
    private String ownerCode;
    @Column(columnDefinition = "varchar(255) comment '商品大类'")
    private String skuFirstCategory;

    private boolean enable;

    @Column(columnDefinition = "json comment '参数配置'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<BatchAttributeConfigDTO.BatchAttributeFieldConfigDTO> batchAttributeFieldConfigs;

    @Version
    private Long version;
}
