package org.openwes.user.domain.entity;

import io.swagger.annotations.ApiModelProperty;
import jakarta.persistence.*;
import lombok.Data;
import lombok.experimental.Accessors;
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
public class LoginLog {

    @ApiModelProperty("id")
    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '账号'")
    private String account;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '登录时间'")
    private String gmtLoginTime;

    @Column(nullable = false, columnDefinition = "bigint comment '登录的时间戳'")
    private Long gmtLoginTimestamp;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '登录ip'")
    private String loginIp;

    @Column(nullable = false, columnDefinition = "int comment '登录结果(1成功, 2失败)'")
    private Integer loginResult;

    @Column(nullable = false, columnDefinition = "varchar(255) comment '登录地址'")
    private String loginAddress = "";

    @Column(nullable = false, columnDefinition = "varchar(255) comment '登录失败原因'")
    private String loginFailureMsg = "";

}
