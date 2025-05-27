package org.openwes.plugin.sdk.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.plugin.api.constants.TenantPluginStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "p_tenant_plugin",
        indexes = {
                @Index(unique = true, name = "uk_plugin_unique_key", columnList = "pluginUniqueKey")
        }
)
@DynamicUpdate
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TenantPlugin extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '插件唯一值，用于校验加载插件'")
    private String pluginUniqueKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '状态'")
    private TenantPluginStatusEnum status;

    @Version
    private Long version;

    public void start() {
        this.status = TenantPluginStatusEnum.STARTED;
    }

    public void stop() {
        this.status = TenantPluginStatusEnum.STOPPED;
    }
}
