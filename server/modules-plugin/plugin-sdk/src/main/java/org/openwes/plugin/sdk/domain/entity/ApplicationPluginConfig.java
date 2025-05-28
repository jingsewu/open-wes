package org.openwes.plugin.sdk.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "p_plugin_config",
        indexes = {
                @Index(unique = true, name = "uk_plugin_unique_key", columnList = "pluginUniqueKey")
        }
)
@DynamicUpdate
public class ApplicationPluginConfig extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '插件唯一值，用于校验加载插件'")
    private String pluginUniqueKey;

    @Column(columnDefinition = "text comment '插件配置'")
    private String configInfo;

    @Version
    private Long version;
}
