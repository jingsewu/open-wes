package org.openwes.user.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
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
        name = "u_user_role",
        indexes = {
                @Index(name = "idx_user_id", columnList = "userId"),
        }
)
@Accessors(chain = true)
public class UserRole implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "bigInt(11) comment '用户id'")
    private Long userId;

    @Column(nullable = false, columnDefinition = "bigInt(11) comment '角色id'")
    private Long roleId;

}
