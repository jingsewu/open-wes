package org.openwes.wes.api.basic.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateEmptyContainerDTO {

    @NotEmpty
    String containerCode;

    @NotEmpty
    String containerCodeSpec;
}
