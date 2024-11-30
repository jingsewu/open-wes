package org.openwes.wes.config.infrastructure.persistence.po;

import org.openwes.common.utils.base.UpdateUserPO;
import org.openwes.common.utils.language.MultiLanguage;
import org.openwes.wes.config.domain.entity.Dictionary;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
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
        name = "m_dictionary",
        indexes = {
                @Index(unique = true, name = "uk_dictionary_code", columnList = "code")
        }
)
public class DictionaryPO extends UpdateUserPO {

    @Id
    @GeneratedValue(generator = "databaseIdGenerator")
    @GenericGenerator(name = "databaseIdGenerator", strategy = "org.openwes.common.utils.id.IdGenerator")
    private Long id;

    @Column(nullable = false, length = 64)
    @Comment("字典编码")
    private String code;

    private boolean editable;

    @Column(nullable = false, length = 2000)
    @Comment("字典名称")
    @JdbcTypeCode(SqlTypes.JSON)
    private MultiLanguage name;

    @Column(length = 2000)
    @Comment("字典描述")
    @JdbcTypeCode(SqlTypes.JSON)
    private MultiLanguage description;

    @Column(nullable = false, columnDefinition = "json comment '字典内容'")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Dictionary.DictionaryItem> items;

    @Version
    private long version;

}
