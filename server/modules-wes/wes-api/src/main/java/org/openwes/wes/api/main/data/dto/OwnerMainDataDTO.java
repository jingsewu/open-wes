package org.openwes.wes.api.main.data.dto;

import org.openwes.wes.api.main.data.constants.OwnerTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

@Data
public class OwnerMainDataDTO implements Serializable {

    private Long id;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "仓库编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String warehouseCode;

    @NotEmpty
    @Size(max = 64)
    @Schema(title = "货主编码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerCode;

    @NotEmpty
    @Size(max = 128)
    @Schema(title = "货主名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ownerName;

    @NotNull
    @Schema(title = "货主类型", requiredMode = Schema.RequiredMode.REQUIRED)
    private OwnerTypeEnum ownerType;

    private ContactorDTO contactorDTO;

    private AddressDTO addressDTO;

    private Long version;
}
