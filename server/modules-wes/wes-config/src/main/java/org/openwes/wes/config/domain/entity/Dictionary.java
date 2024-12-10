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
        private MultiLanguage showContext;
        private int order;
        private boolean defaultItem;
        private MultiLanguage description;

    }
}
