package org.openwes.api.platform.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.openwes.api.platform.api.constants.ConverterTypeEnum;
import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.id.IdGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
@DynamicUpdate
@DynamicInsert
@Table(name = "a_api_config",
        indexes = {
                @Index(name = "uk_code", columnList = "code", unique = true),
        })
public class ApiConfigPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", type = IdGenerator.class)
    private Long id;

    @Column(length = 128, nullable = false)
    private String code;

    @Comment("parameter convert type")
    private ConverterTypeEnum paramConverterType;

    @Comment("response convert type")
    private ConverterTypeEnum responseConverterType;

    @Column(columnDefinition = "text")
    @Comment("参数转换脚本")
    private String paramConverterScript;

    @Column(columnDefinition = "text")
    @Comment("响应转换脚本")
    private String responseConverterScript;

}
