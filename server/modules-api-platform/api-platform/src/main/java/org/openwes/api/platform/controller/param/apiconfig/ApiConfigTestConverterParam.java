package org.openwes.api.platform.controller.param.apiconfig;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
@Schema(description = "测试接口参数转换脚本")
public class ApiConfigTestConverterParam {

    @NotEmpty(message = "转换脚本不能为空")
    @Schema(title = "JS 转换脚本", requiredMode = Schema.RequiredMode.REQUIRED)
    private String jsScript;

    @NotEmpty(message = "输入 JSON 不能为空")
    @Schema(title = "输入 JSON 字符串", requiredMode = Schema.RequiredMode.REQUIRED)
    private String inputJson;
}
