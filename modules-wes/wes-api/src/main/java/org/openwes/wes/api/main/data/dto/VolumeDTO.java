package org.openwes.wes.api.main.data.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "体积信息")
public class VolumeDTO implements Serializable {

    @Min(value = 0, message = "volume must be greater than or equal to 0")
    @NotNull
    @Schema(title = "体积", description = "立方毫米(mm³)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long volume;

    @Min(value = 0, message = "height must be greater than or equal to 0")
    @NotNull
    @Schema(title = "高", description = "毫米(mm)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long height;

    @Min(value = 0, message = "width must be greater than or equal to 0")
    @NotNull
    @Schema(title = "宽", description = "毫米(mm)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long width;

    @Min(value = 0, message = "length must be greater than or equal to 0")
    @NotNull
    @Schema(title = "长", description = "毫米(mm)", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long length;
}
