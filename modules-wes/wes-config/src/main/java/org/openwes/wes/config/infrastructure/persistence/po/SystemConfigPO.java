package org.openwes.wes.config.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.config.dto.SystemConfigDTO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import static org.hibernate.type.SqlTypes.JSON;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "m_system_config",
        indexes = {
                @Index(unique = true, name = "uk_singleton_key", columnList = "singletonKey")
        }
)
public class SystemConfigPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @JdbcTypeCode(JSON)
    private SystemConfigDTO.BasicConfigDTO basicConfig;

    @JdbcTypeCode(JSON)
    private SystemConfigDTO.EmsConfigDTO emsConfig;

    @JdbcTypeCode(JSON)
    private SystemConfigDTO.InboundConfigDTO inboundConfig;

    @JdbcTypeCode(JSON)
    private SystemConfigDTO.OutboundConfigDTO outboundConfig;

    @JdbcTypeCode(JSON)
    private SystemConfigDTO.OutboundAlgoConfigDTO outboundAlgoConfig;

    @Column(nullable = false)
    private String singletonKey = "SYSTEM_CONFIG";

    private Long version;

}
