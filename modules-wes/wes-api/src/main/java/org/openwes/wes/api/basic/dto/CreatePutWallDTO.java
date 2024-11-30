package org.openwes.wes.api.basic.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreatePutWallDTO {

    private Long id;
    private Long version;

    @NotEmpty
    private String warehouseCode;
    @NotNull
    private Long workStationId;
    @NotEmpty
    private String putWallCode;
    @NotEmpty
    private String putWallName;
    @NotEmpty
    private String containerSpecCode;
}
