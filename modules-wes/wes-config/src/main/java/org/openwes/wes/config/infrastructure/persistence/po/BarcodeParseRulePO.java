package org.openwes.wes.config.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.wes.api.config.constants.BusinessFlowEnum;
import org.openwes.wes.api.config.constants.ExecuteTimeEnum;
import org.openwes.wes.api.config.constants.UnionLocationEnum;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.List;


@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(
    name = "m_barcode_parse_rule",
    indexes = {
        @Index(unique = true, name = "uk_barcode_parse_code", columnList = "code")
    }
)
@DynamicUpdate
public class BarcodeParseRulePO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, columnDefinition = "varchar(64) comment '编码'")
    private String code;

    @Column(nullable = false, columnDefinition = "varchar(128) comment '名称'")
    private String name;

    @Column(columnDefinition = "varchar(64) comment '货主编码'")
    private String ownerCode;
    @Column(columnDefinition = "varchar(64) comment '品牌'")
    private String brand;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '执行时间'")
    private ExecuteTimeEnum executeTime;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(20) comment '业务流程'")
    private BusinessFlowEnum businessFlow;

    private boolean enable;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) comment '拼接位置'")
    private UnionLocationEnum unionLocation;

    @Column(columnDefinition = "varchar(255) comment '拼接符'")
    private String unionStr;

    @Column(nullable = false, columnDefinition = "varchar(500) comment '正则表达式'")
    private String regularExpression;

    @Column(columnDefinition = "json comment '解析规则参数'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> resultFields;

    @Version
    private Long version;
}
