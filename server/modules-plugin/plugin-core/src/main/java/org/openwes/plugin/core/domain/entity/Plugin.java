package org.openwes.plugin.core.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.openwes.plugin.api.constants.PluginStatusEnum;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "p_plugin",
        indexes = {
                @Index(unique = true, name = "uk_plugin_unique_key", columnList = "pluginUniqueKey"),
                @Index(unique = true, name = "uk_plugin_code_version", columnList = "code,pluginVersion")
        }
)
@DynamicUpdate
public class Plugin extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("插件编号")
    private String code;

    @Column(nullable = false)
    @Comment("插件ID,用于校验加载插件")
    private String pluginUniqueKey;

    @Column(nullable = false, length = 128)
    @Comment("插件名称")
    private String name;

    @Column(nullable = false)
    @Comment("开发者")
    private String developer;

    @Column(nullable = false, length = 64)
    @Comment("插件版本")
    private String pluginVersion;

    @Column(nullable = false)
    @Comment("描述")
    private String description = "";

    @Column(nullable = false)
    @Comment("jar文件地址")
    private String jarFilePath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private PluginStatusEnum pluginStatus;

    @Version
    private Long version;


    public void approve() {
        if (this.pluginStatus != PluginStatusEnum.NEW) {
            throw new IllegalArgumentException("illegal states");
        }
        this.pluginStatus = PluginStatusEnum.APPROVED;
    }

    public void reject() {
        if (this.pluginStatus != PluginStatusEnum.NEW) {
            throw new IllegalArgumentException("illegal states");
        }
        this.pluginStatus = PluginStatusEnum.REJECTED;
    }

    public void publish() {
        if (this.pluginStatus != PluginStatusEnum.APPROVED) {
            throw new IllegalArgumentException("illegal states");
        }
        this.pluginStatus = PluginStatusEnum.PUBLISHED;
    }

    public void unpublish() {
        if (this.pluginStatus != PluginStatusEnum.PUBLISHED) {
            throw new IllegalArgumentException("illegal states");
        }
        this.pluginStatus = PluginStatusEnum.APPROVED;
    }

    public void delete() {
        if (this.pluginStatus != PluginStatusEnum.NEW && this.pluginStatus != PluginStatusEnum.REJECTED) {
            throw new IllegalArgumentException("illegal states");
        }
        this.pluginStatus = PluginStatusEnum.DELETED;
    }
}
