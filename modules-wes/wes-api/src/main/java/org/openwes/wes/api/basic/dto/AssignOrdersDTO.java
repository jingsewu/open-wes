package org.openwes.wes.api.basic.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@Accessors(chain = true)
public class AssignOrdersDTO {

    @NotEmpty
    private List<AssignDetail> assignDetails;

    @Data
    @Accessors(chain = true)
    public static class AssignDetail {

        @NotNull
        private Long workStationId;

        @NotEmpty
        private String putWallSlotCode;

        @NotNull
        private Long orderId;

    }
}
