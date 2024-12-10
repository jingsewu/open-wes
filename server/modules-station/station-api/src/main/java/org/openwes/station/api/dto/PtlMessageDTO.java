package org.openwes.station.api.dto;

import org.openwes.wes.api.basic.constants.PtlColorEnum;
import org.openwes.wes.api.basic.constants.PtlModeEnum;
import org.openwes.wes.api.basic.constants.PtlUpdownEnum;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

@Data
@Accessors(chain = true)
public class PtlMessageDTO implements Serializable {
    @Serial
    private static final long serialVersionUID = 3443061526302850189L;

    private String workStationId;
    private String tagCode;
    private PtlColorEnum color;
    private PtlModeEnum mode;
    private PtlUpdownEnum updown;
    private Integer number = 0;
    private String displayText;
}
