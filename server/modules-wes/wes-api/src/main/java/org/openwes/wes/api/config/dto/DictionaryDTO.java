package org.openwes.wes.api.config.dto;

import org.openwes.common.utils.base.UpdateUserDTO;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class DictionaryDTO extends UpdateUserDTO {

    private Long id;
    @NotEmpty
    private String code;
    private boolean editable;

    @NotEmpty
    private String name;
    private String description;

    @NotEmpty
    private List<DictionaryItem> items;
    private long version;

    @Data
    public static class DictionaryItem {

        @NotEmpty
        private String value;
        private String showContent;     // 合并后的展示 label / 管理界面写入的 custom label
        private String systemContent;   // 系统默认 label（管理界面只读参考）
        private int order;
        private boolean defaultItem;
        private String description;
    }


}
