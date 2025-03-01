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

    @Column(length = 20, columnDefinition = "varchar(20)")
    @Comment("parameter convert type")
    private ConverterTypeEnum paramConverterType;

    @Column(length = 20, columnDefinition = "varchar(20)")
    @Comment("response convert type")
    private ConverterTypeEnum responseConverterType;

    @Column(columnDefinition = "text comment '请求参数转换脚本'")
    private String jsParamConverter;
    @Column(columnDefinition = "text comment '响应参数转换脚本'")
    private String jsResponseConverter;

    @Column(columnDefinition = "text comment '请求参数转换模板'")
    private String templateParamConverter;
    @Column(columnDefinition = "text comment '响应参数转换模板'")
    private String templateResponseConverter;

}
