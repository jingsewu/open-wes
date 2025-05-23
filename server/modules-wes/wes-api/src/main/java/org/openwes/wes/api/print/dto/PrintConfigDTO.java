package org.openwes.wes.api.print.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class PrintConfigDTO {

    private Long id;

    @NotEmpty
    private String configCode;

    @NotNull
    private Long workStationId;

    @NotEmpty
    private List<PrintConfigDetail> printConfigDetails;

    private boolean enabled;

    private boolean deleted;
    private Long deleteTime = 0L;

    @Data
    public static class PrintConfigDetail {
        @NotNull
        private String ruleCode;
        @NotNull
        private String printer;
    }
}
