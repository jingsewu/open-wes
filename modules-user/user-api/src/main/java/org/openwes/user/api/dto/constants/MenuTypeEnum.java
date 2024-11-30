package org.openwes.user.api.dto.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sws
 */
@Getter
@AllArgsConstructor
public enum MenuTypeEnum implements IEnum {
    MENU("1", "系统"),
    PAGE("2", "菜单"),
    PERMISSION("3", "权限");

    private String value;
    private String label;

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getLabel() {
        return this.label;
    }

}
