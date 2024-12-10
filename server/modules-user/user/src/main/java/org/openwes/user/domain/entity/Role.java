package org.openwes.user.domain.entity;

import org.openwes.common.utils.base.UpdateUserPO;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;
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
        name = "u_role",
        indexes = {
                @Index(unique = true, name = "uk_role_code", columnList = "code")
        }
)
@DynamicUpdate
@Accessors(chain = true)
public class Role extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '角色名称'")
    private String name;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '角色编码'")
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "json comment '有权限查询的仓库'")
    private List<String> warehouseCodes;

    @Column(nullable = false, columnDefinition = "int comment '状态（1启用, 0停用）'")
    private Integer status;

    @Column(columnDefinition = "varchar(255) comment '描述'")
    private String description;

}
