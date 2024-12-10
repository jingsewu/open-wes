package org.openwes.api.platform.controller.param.api;

import org.openwes.api.platform.api.constants.ApiCallTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

@Data
@Schema(description = "更新接口参数")
public class ApiUpdateParam {

    @NotNull
    @Schema(title = "接口 ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long id;

    @NotEmpty(message = "接口编码不能为空")
    @Schema(title = "接口编码（英文大写，用下划线分隔）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotEmpty(message = "接口名称不能为空")
    @Schema(title = "接口名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(title = "接口描述")
    private String title;

    @NotNull(message = "接口类型不能为空")
    @Schema(title = "接口类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private ApiCallTypeEnum apiType;

    @NotEmpty(message = "接口地址不能为空")
    @Schema(title = "接口地址", requiredMode = Schema.RequiredMode.REQUIRED)
    private String url;

    @NotEmpty(message = "接口请求方法不可为空")
    @Schema(title = "接口请求方法，如 GET、POST", requiredMode = Schema.RequiredMode.REQUIRED)
    private String method;

    @Schema(title = "接口请求编码")
    private String encoding;

    @Schema(title = "回传请求头", description = "仅在回传类请求中生效")
    private Map<String, String> headers;

    @NotEmpty(message = "接口请求格式不可为空")
    @Schema(title = "接口请求格式，对应 MediaType 的枚举", requiredMode = Schema.RequiredMode.REQUIRED)
    private String format;

    @Schema(title = "是否进行接口认证", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean auth;

    @Schema(title = "认证服务地址")
    private String authUrl;

    @Schema(title = "接口认证类型")
    private String grantType;

    @Schema(title = "认证服务用户名")
    private String username;

    @Schema(title = "认证服务密码")
    private String password;

    @Schema(title = "密钥 ID")
    private String secretId;

    @Schema(title = "密钥")
    private String secretKey;

    @Schema(title = "认证服务响应的 token 名称")
    private String tokenName;

    @Schema(title = "是否开启")
    private boolean enabled;

    @Schema(title = "是否同步调用")
    private boolean syncCallback;
}
