package org.openwes.user.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.openwes.common.utils.base.UpdateUserPO;
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
@Comment("System role definition table - manages user role assignments and warehouse access permissions")
public class Role extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for role")
    private Long id;

    @Column(nullable = false, length = 128)
    @Comment("Display name of the role shown in UI and reports")
    private String name;

    @Column(nullable = false, length = 64)
    @Comment("Unique role identifier code used for programmatic access control - immutable after creation")
    private String code;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(nullable = false)
    @Comment("List of warehouse codes this role can access - stored as JSON array for flexible permission management")
    private List<String> warehouseCodes;

    @Column(nullable = false)
    @Comment("Role status: 1=Active and can be assigned to users, 0=Inactive and cannot be assigned")
    private Integer status;

    @Comment("Optional detailed description of role purpose, responsibilities and access scope")
    private String description;
}
