package org.openwes.user.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "u_user",
        indexes = {
                @Index(unique = true, name = "uk_account", columnList = "account")
        }
)
@DynamicUpdate
@Accessors(chain = true)
@Comment("System user management table - stores user authentication and profile information in multi-tenant environment")
public class User extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    @Comment("Unique identifier for user")
    private Long id;

    @Column(nullable = false, length = 128)
    @Comment("Display name shown in UI and reports")
    private String name;

    @Column(length = 64)
    @Comment("Contact phone number - optional for notifications")
    private String phone;

    @Column(length = 128)
    @Comment("Email address - optional for notifications and password recovery")
    private String email;

    @Column(nullable = false, length = 128)
    @Comment("Unique login username/account identifier for authentication")
    private String account;

    @Column(nullable = false, length = 128)
    @Comment("Encrypted password hash - defaults to 123456 before first login")
    private String password = "123456";

    @Column(nullable = false)
    @Comment("Account status: 1=Active can login, 0=Inactive blocked from login")
    private Integer status;

    @Column(nullable = false)
    @Comment("Account lock counter: less or equal 5=active, greater than 5=locked")
    private Integer locked = 0;

    @Column(length = 128)
    @Comment("URL or path to profile avatar image")
    private String avatar;

    @Column(length = 64)
    @Comment("Last successful login IP address for security tracking")
    private String lastLoginIp;

    @Column(length = 64)
    @Comment("Last successful login timestamp for security tracking")
    private String lastGmtLoginTime;

    @Column(nullable = false, length = 64)
    @Comment("Account type identifier: NORMAL=Standard account, others for special types")
    private String type = "NORMAL";

    @Column(nullable = false, length = 64)
    @Comment("Tenant identifier for multi-tenant system isolation")
    private String tenantName;
}
