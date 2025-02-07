package org.openwes.user.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "u_menu",
        indexes = {
                @Index(name = "idx_system_code", columnList = "systemCode")
        }
)
@DynamicUpdate
@Accessors(chain = true)
@Comment("System menu and permission configuration table - manages hierarchical menu structure and access rights")
public class Menu extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for menu item")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("System identifier code - references the subsystem this menu belongs to")
    private String systemCode;

    @Column(nullable = false)
    @Comment("Parent menu ID - 0 indicates top-level menu item in hierarchy")
    private Long parentId;

    @Column(nullable = false)
    @Comment("Entry type: 1=System, 2=Menu, 3=Permission - defines the hierarchical level and behavior")
    private Integer type;

    @Column(nullable = false, length = 128)
    @Comment("Display name of the menu item shown in UI")
    private String title;

    @Column(length = 255)
    @Comment("Optional detailed description of menu item purpose and functionality")
    private String description;

    @Column(nullable = false, length = 255)
    @Comment("Permission identifiers, comma-separated for multiple permissions (e.g., \"user:create,user:edit\")")
    private String permissions;

    @Column(nullable = false)
    @Comment("Display order priority - lower numbers appear first in menu")
    private Integer orderNum;

    @Column(length = 64)
    @Comment("UI icon identifier for menu item visualization")
    private String icon;

    @Column(length = 128)
    @Comment("URL path or route for menu item navigation")
    private String path;

    @Column
    @Comment("iframe display flag: 1=Show in iframe, 0=Show normally")
    private Integer iframeShow;

    @Column(nullable = false)
    @Comment("Status flag: 1=Enabled and visible, 0=Disabled and hidden")
    private Integer enable;

    @Transient
    private List<Menu> children;
}
