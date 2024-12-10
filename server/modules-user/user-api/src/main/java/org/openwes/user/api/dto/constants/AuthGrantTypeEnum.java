package org.openwes.user.api.dto.constants;

import org.openwes.common.utils.dictionary.IEnum;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sws
 */
@Getter
@AllArgsConstructor
public enum AuthGrantTypeEnum implements IEnum {
    PASSWORD("password", "密码"),
    CLIENT_CREDENTIALS("client_credentials", "客户端"),
    REFRESH_TOKEN("refresh_token", "刷新token"),
    AUTHORIZATION_CODE("authorization_code", "授权码");

    private String code;
    private String desc;

    @Override
    public String getValue() {
        return this.code;
    }

    @Override
    public String getLabel() {
        return this.desc;
    }

}
