package org.openwes.station.api.model;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Toolbar {
    private boolean enableReportAbnormal;
    private boolean enableSplitContainer;
    private boolean enableReleaseSlot;
}
