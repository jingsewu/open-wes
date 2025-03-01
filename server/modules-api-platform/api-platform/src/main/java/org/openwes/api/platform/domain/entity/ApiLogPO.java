package org.openwes.api.platform.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.api.platform.api.constants.ApiLogStatusEnum;
import org.openwes.common.utils.base.CreateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
@Table(name = "a_api_log",
        indexes = {
                @Index(name = "uk_message_id", columnList = "messageId", unique = true),
                @Index(name = "idx_api_code", columnList = "apiCode"),
                @Index(name = "idx_create_time", columnList = "createTime")
        })
public class ApiLogPO extends CreateUserPO {
    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(columnDefinition = "bigint not null default 0")
    private Long messageId;

    @Column(length = 128, nullable = false)
    private String apiCode;

    @Column(length = 65535)
    @Comment("请求参数")
    private String requestData;

    @Column(length = 65535)
    @Comment("返回参数")
    private String responseData;

    @Column(columnDefinition = "int(11) not null default 0")
    private Integer retryCount = 0;

    @Column(columnDefinition = "int(11) not null default 0")
    private Long costTime = 0L;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment 'api log status'")
    private ApiLogStatusEnum status;
}
