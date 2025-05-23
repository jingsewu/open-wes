package org.openwes.wes.api.print.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PrintContentDTO implements Serializable {
    private Long printRecordId;
    private String html;
    private Long workStationId;
    private String printer;
}
