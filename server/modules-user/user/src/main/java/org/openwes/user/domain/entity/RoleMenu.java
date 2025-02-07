package org.openwes.user.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "u_role_menu",
        indexes = {
                @Index(name = "idx_role_id", columnList = "roleId")
        }
)
@Accessors(chain = true)
@Comment("Role-Menu mapping table - manages many-to-many relationships between roles and menu permissions")
public class RoleMenu implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for role-menu mapping")
    private Long id;

    @Column(nullable = false)
    @Comment("Foreign key reference to u_role.id - indicates which role this permission belongs to")
    private Long roleId;

    @Column(nullable = false)
    @Comment("Foreign key reference to u_menu.id - indicates which menu/permission is granted to the role")
    private Long menuId;
}
