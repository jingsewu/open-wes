package org.openwes.user.domain.entity;

import org.openwes.common.utils.base.UpdateUserPO;
import io.swagger.annotations.ApiModelProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
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
public class User extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '用户名称'")
    private String name;

    @Column(columnDefinition = "varchar(64) comment '手机号'")
    private String phone;

    @Column(columnDefinition = "varchar(128) comment '邮箱'")
    private String email;

    @ApiModelProperty("登录用户名")
    @Column(nullable = false, columnDefinition = "varchar(128) comment '登录用户名'")
    private String account;

    @ApiModelProperty("密码")
    @Column(nullable = false, columnDefinition = "varchar(128) comment '密码(加密后)'")
    private String password = "123456";

    @ApiModelProperty("帐号状态（1启用, 0停用）")
    @Column(nullable = false, columnDefinition = "int comment '状态（1启用, 0停用）'")
    private Integer status;

    @ApiModelProperty("是否被锁(小于等于5表示未被锁, 大于5表示被锁)")
    @Column(nullable = false, columnDefinition = "int comment '是否被锁(小于等于5表示未被锁, 大于5表示被锁)'")
    private Integer locked = 0;

    @ApiModelProperty("头像地址")
    @Column(columnDefinition = "varchar(128) comment '头像地址'")
    private String avatar;

    @ApiModelProperty("上一次登录的ip地址")
    @Column(columnDefinition = "varchar(64) comment '上一次登录的ip地址'")
    private String lastLoginIp;

    @ApiModelProperty("上一次登录的时间")
    @Column(columnDefinition = "varchar(64) comment '上一次登录的时间'")
    private String lastGmtLoginTime;

    @ApiModelProperty("账号标识,默认为 NORMAL:普通账号")
    @Column(nullable = false, columnDefinition = "varchar(64) comment '账号标识,默认为 NORMAL:普通账号'")
    private String type = "NORMAL";

    @Column(nullable = false, columnDefinition = "varchar(64) comment '租户'")
    private String tenantName;
}
