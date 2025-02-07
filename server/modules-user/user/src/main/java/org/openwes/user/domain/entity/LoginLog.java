package org.openwes.user.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
        name = "u_login_log",
        indexes = {
                @Index(name = "idx_user_account", columnList = "account")
        }
)
@Accessors(chain = true)
@Comment("System user login audit trail - tracks all login attempts, successes, and failures for security monitoring")
public class LoginLog {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    @Comment("Unique identifier for each login attempt record")
    private Long id;

    @Column(nullable = false, length = 128)
    @Comment("User login account identifier/username - indexed for quick user activity lookup")
    private String account;

    @Column(nullable = false)
    @Comment("Unix timestamp (milliseconds) of the login attempt for temporal analysis and audit trails")
    private Long loginTime;

    @Column(nullable = false)
    @Comment("Login attempt outcome: 1=successful authentication, 2=failed authentication - used for security metrics and monitoring")
    private Integer loginResult;

    @Column(nullable = false)
    @Comment("IP address or location information of the login attempt - used for geographic tracking and security analysis")
    private String loginAddress = "";

    @Column(nullable = false)
    @Comment("Detailed failure reason when login fails (e.g., \"Invalid password\", \"Account locked\") - helps with security diagnostics")
    private String loginFailureMsg = "";
}
