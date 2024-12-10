package org.openwes.user.domain.entity;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
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
public class Menu extends UpdateUserPO {


    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '所属系统'")
    private String systemCode;

    @Column(nullable = false, columnDefinition = "bigint comment '父菜单id,如果是顶级菜单, 则为0'")
    private Long parentId;

    @Column(nullable = false, columnDefinition = "int comment '类型(1: 系统、2: 菜单、3: 权限)'")
    private Integer type;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '名称'")
    private String title;

    @Column(columnDefinition = "varchar(255) comment '描述'")
    private String description;

    @Column(nullable = false, columnDefinition = "varchar(255) comment '权限标识, 多个可用逗号分隔'")
    private String permissions;

    @Column(nullable = false, columnDefinition = "int comment '排序，数字越小越靠前'")
    private Integer orderNum;

    @Column(columnDefinition = "varchar(64) comment '图标'")
    private String icon;

    @Column(columnDefinition = "varchar(128) comment '路径地址'")
    private String path;

    @Column(columnDefinition = "int comment '是否以 iframe 的方式显示(1启用, 0禁用)'")
    private Integer iframeShow;

    @Column(nullable = false, columnDefinition = "int comment '是否启用(1启用, 0禁用)'")
    private Integer enable;

    @Transient
    private List<Menu> children;
}
