package org.openwes.wes.api.print.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.openwes.common.utils.base.BaseWebsocketMessage;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class PrintContentDTO extends BaseWebsocketMessage implements Serializable {

    private Long printRecordId;
    private String content;
    private Long workStationId;
    private String printer;
    private int copies;
}
