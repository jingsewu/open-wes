package org.openwes.wes.api.basic.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReleasePutWallSlotsDTO {

    @NotNull
    private Long workStationId;

    @NotEmpty
    private List<String> putWallSlotCodes;
}
