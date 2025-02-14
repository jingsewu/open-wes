package org.openwes.user.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serial;
import java.io.Serializable;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "u_user_role",
        indexes = {
                @Index(name = "idx_user_id", columnList = "userId"),
        }
)
@Accessors(chain = true)
@Comment("User-Role mapping table - manages many-to-many relationships between users and their assigned roles")
public class UserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for user-role assignment")
    private Long id;

    @Column(nullable = false)
    @Comment("Foreign key reference to u_user.id - links to the user receiving the role assignment")
    private Long userId;

    @Column(nullable = false)
    @Comment("Foreign key reference to u_role.id - links to the role being assigned to the user")
    private Long roleId;
}
