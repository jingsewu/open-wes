package org.openwes.api.platform.controller.param.apiconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openwes.api.platform.api.constants.ConverterTypeEnum;

@Data
@Schema(description = "测试接口参数转换脚本")
public class ApiConfigTestConverterParam {

    @NotNull(message = "转换类型不能为空")
    @Schema(title = "转换类型（仅支持 JS / JAVA）", requiredMode = Schema.RequiredMode.REQUIRED)
    private ConverterTypeEnum converterType;

    @NotEmpty(message = "转换脚本不能为空")
    @Schema(title = "转换脚本", requiredMode = Schema.RequiredMode.REQUIRED)
    private String script;

    @NotEmpty(message = "输入 JSON 不能为空")
    @Schema(title = "输入 JSON 字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inputJson;
}
