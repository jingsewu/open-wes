package org.openwes.api.platform.controller.param.apiconfig;

import org.openwes.api.platform.api.constants.ConverterTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "更新接口参数转换配置")
public class ApiConfigUpdateParam {

    @Schema(title = "接口转换配置 ID")
    private Long id;

    @NotEmpty(message = "接口编码不能为空")
    @Schema(title = "接口编码（英文大写，用下划线分隔）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String code;

    @NotNull(message = "请求转换脚本的类型不能为空")
    @Schema(title = "请求转换脚本的类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private ConverterTypeEnum paramConverterType;

    @Schema(title = "JS 类型的请求请求转换脚本")
    private String jsParamConverter;

    @Schema(title = "FreeMarker 类型的请求转换脚本")
    private String templateParamConverter;

    @NotNull(message = "响应换脚本的类型不能为空")
    @Schema(title = "响应换脚本的类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private ConverterTypeEnum responseConverterType;

    @Schema(title = "JS 类型的请求响应转换脚本")
    private String jsResponseConverter;

    @Schema(title = "FreeMarker 类型的响应转换脚本")
    private String templateResponseConverter;
}
