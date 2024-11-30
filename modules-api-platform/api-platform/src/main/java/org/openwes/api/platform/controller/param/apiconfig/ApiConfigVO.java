package org.openwes.api.platform.controller.param.apiconfig;

import org.openwes.api.platform.api.constants.ConverterTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "接口参数转换配置")
public class ApiConfigVO {

    @Schema(title = "接口转换配置 ID")
    private Long id;

    @Schema(title = "接口编码（英文大写，用下划线分隔）")
    private String code;

    @Schema(title = "请求转换脚本的类型")
    private ConverterTypeEnum paramConverterType;

    @Schema(title = "JS 类型的请求请求转换脚本")
    private String jsParamConverter;

    @Schema(title = "FreeMarker 类型的请求转换脚本")
    private String templateParamConverter;

    @Schema(title = "响应换脚本的类型")
    private ConverterTypeEnum responseConverterType;

    @Schema(title = "JS 类型的请求响应转换脚本")
    private String jsResponseConverter;

    @Schema(title = "FreeMarker 类型的响应转换脚本")
    private String templateResponseConverter;
}
