package org.openwes.station.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Accessors(chain = true)
@Data
public class TapPtlEvent implements Serializable {

    @NotEmpty
    private String ptlTag;

    @NotNull
    private Long workStationId;
}
