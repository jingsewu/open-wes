package org.openwes.script.plugin.core.domain.entity;

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
@Table(name = "p_script_plugin_config", indexes = {
        @Index(unique = true, name = "uk_script_config_plugin", columnList = "pluginId")})
@DynamicUpdate
public class ScriptPluginConfig extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false)
    private Long pluginId;

    @Column(nullable = false, columnDefinition = "json")
    private String configJson;

    @Version
    private Long lockVersion;
}
