package org.openwes.wes.api.print.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.openwes.wes.api.print.constants.ModuleEnum;
import org.openwes.wes.api.print.constants.PrintNodeEnum;

import java.util.List;

@Data
public class PrintRuleDTO {

    private Long id;

    @NotEmpty
    private String ruleName;

    @NotEmpty
    private String ruleCode;

    private List<String> ownerCodes;

    private List<String> salesPlatforms;

    private List<String> carrierCodes;

    private List<String> inboundOrderTypes;

    private List<String> outboundOrderTypes;

    @NotNull
    private ModuleEnum module;

    @NotNull
    private PrintNodeEnum printNode;

    private Integer printCount = 1;

    @NotNull
    private String templateCode;

    private String sqlScript;

    private boolean deleted;
    private Long deleteTime = 0L;

    private boolean enabled;
}
