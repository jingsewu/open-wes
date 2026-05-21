package org.openwes.wes.config.domain.entity;

import org.openwes.common.utils.base.UpdateUserDTO;
import org.openwes.common.utils.language.MultiLanguage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class Dictionary extends UpdateUserDTO {

    private Long id;
    private String code;
    private boolean editable;
    private MultiLanguage name;
    private MultiLanguage description;
    private long version;

    private List<DictionaryItem> items;

    @Data
    public static class DictionaryItem {
        private String value;
        private MultiLanguage systemLabel;   // 系统默认，由 refresh / Liquibase 写入
        private MultiLanguage customLabel;   // 客户覆盖，由管理界面写入（null = 未定制）
        private int order;
        private boolean defaultItem;
        private MultiLanguage description;

    }
}
