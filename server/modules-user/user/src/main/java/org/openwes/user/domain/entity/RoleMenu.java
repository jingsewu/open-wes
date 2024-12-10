package org.openwes.user.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
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
public class RoleMenu implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigInt(11) comment '角色id'")
    private Long roleId;

    @Column(nullable = false, columnDefinition = "bigInt(11) comment '菜单id'")
    private Long menuId;
}
