package org.openwes.api.platform.domain.entity;

import org.openwes.api.platform.api.constants.ApiCallTypeEnum;
import org.openwes.api.platform.api.constants.CallbackApiTypeEnum;
import org.openwes.api.platform.utils.AuthUtils;
import org.openwes.api.platform.utils.HttpHelper;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.constants.MarkConstant;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Map;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
@Table(name = "a_api",
        indexes = {
                @Index(name = "uk_code", columnList = "code", unique = true),
                @Index(name = "idx_api_type", columnList = "apiType")
        })
public class ApiPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(length = 128, nullable = false)
    private String code;

    @Column(length = 128, nullable = false)
    private String name;

    @Column(length = 1024)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment 'api 类型'")
    private ApiCallTypeEnum apiType;

    @Column(length = 128, columnDefinition = "varchar(128) comment '请求url'")
    private String url = "";
    @Column(columnDefinition = "varchar(20) not null comment '请求方式'")
    private String method = "";
    @Column(columnDefinition = "varchar(20) comment '请求编码'")
    private String encoding = "";
    @Column(columnDefinition = "varchar(100) not null comment '请求格式'")
    private String format = "";
    @Column(columnDefinition = "json comment '回传请求头'")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> headers;

    //是否开启token验证，如果需要则需要先请求token，然后再调用
    private boolean auth;
    @Column(columnDefinition = "varchar(128) comment 'auth url'")
    private String authUrl = "";
    @Column(columnDefinition = "varchar(64) comment 'grant type'")
    private String grantType = "";
    @Column(columnDefinition = "varchar(64) comment 'auth username'")
    private String username = "";
    @Column(columnDefinition = "varchar(64) comment 'auth password'")
    private String password = "";
    @Column(columnDefinition = "varchar(64) comment 'auth secretId'")
    private String secretId = "";
    @Column(columnDefinition = "varchar(64) comment 'auth secretKey'")
    private String secretKey = "";
    @Column(columnDefinition = "varchar(64) comment 'auth tokenName'")
    private String tokenName = "";

    /**
     * 是否同步执行回调
     */
    private boolean syncCallback = false;

    private boolean enabled;

    public static String generateCode(CallbackApiTypeEnum callbackType, String bizType) {
        return callbackType + MarkConstant.HYPHEN_CHARACTER + bizType;
    }

    public Object execute(Object targetObj) throws Exception {

        String accessToken = null;
        if (auth) {
            AuthUtils.Authentication authentication = new AuthUtils.Authentication()
                    .setAuthUrl(this.authUrl)
                    .setEncoding(this.encoding)
                    .setGrantType(this.grantType)
                    .setPassword(this.password)
                    .setUsername(this.username)
                    .setSecretId(this.secretId)
                    .setTokenName(this.tokenName)
                    .setSecretKey(this.secretKey);

            accessToken = AuthUtils.getAccessToken(authentication);
        }

        return HttpHelper.request(new HttpHelper.HttpRequest().setRequestObj(targetObj)
                .setEncoding(this.encoding).setFormat(this.format)
                .setMethod(this.method).setFormat(this.format).setUrl(this.url)
                .setToken(accessToken).setHeaders(this.headers));
    }

    public String resolveCallbackType() {
        if (this.code.contains(MarkConstant.HYPHEN_CHARACTER)) {
            return this.code.split(MarkConstant.HYPHEN_CHARACTER)[0];
        }
        return this.code;
    }
}
