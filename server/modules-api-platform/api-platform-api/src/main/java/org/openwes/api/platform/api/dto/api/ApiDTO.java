package org.openwes.api.platform.api.dto.api;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.openwes.api.platform.api.constants.ApiCallTypeEnum;
import org.openwes.common.utils.base.UpdateUserDTO;

import java.util.Map;

@EqualsAndHashCode(callSuper = true)
@Data
public class ApiDTO extends UpdateUserDTO {

    private Long id;

    private String code;

    private String name;

    private String description;

    private ApiCallTypeEnum apiType;

    private String url = "";
    private String method = "";
    private String encoding = "";
    private String format = "";
    private Map<String, String> headers;

    //是否开启token验证，如果需要则需要先请求token，然后再调用
    private boolean auth;
    private String authUrl = "";
    private String grantType = "";
    private String username = "";
    private String password = "";
    private String secretId = "";
    private String secretKey = "";
    private String tokenName = "";

    /**
     * 是否同步执行回调
     */
    private boolean syncCallback = false;

    private boolean enabled;
}
