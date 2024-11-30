package org.openwes.wes.basic.work_station.domain.entity;

import lombok.Data;

@Data
public class WorkStationRule {
    private Long id;

    private String ruleCode;
    private String ruleName;

}
