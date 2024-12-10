package org.openwes.wes.printer.domain.entity;

import org.openwes.wes.printer.domain.constants.ModuleEnum;
import org.openwes.wes.printer.domain.constants.PrintNodeEnum;
import lombok.Data;

import java.util.List;

@Data
public class PrintRule {

    private Long id;

    private String ruleName;

    private String ruleCode;

    private List<String> ownerCodes;

    private List<String> salesPlatforms;

    private List<String> currierCodes;

    private List<String> inboundOrderTypes;

    private List<String> outboundOrderTypes;

    private ModuleEnum module;

    private PrintNodeEnum printNode;

    private Integer printCount = 1;

    private String templateCode;

    private String sqlScript;

    private boolean deleted;
    private Long deleteTime = 0L;

    private boolean enabled;
}
