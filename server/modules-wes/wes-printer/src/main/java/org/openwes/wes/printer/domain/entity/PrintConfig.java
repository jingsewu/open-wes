package org.openwes.wes.printer.domain.entity;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;


@Data
public class PrintConfig {

    private Long id;

    private String configCode;

    private Long workStationId;

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
