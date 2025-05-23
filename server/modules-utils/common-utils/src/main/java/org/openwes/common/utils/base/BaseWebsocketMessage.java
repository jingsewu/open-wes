package org.openwes.common.utils.base;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class BaseWebsocketMessage implements Serializable {
    private WebsocketMessageTypeEnum type;

    public enum WebsocketMessageTypeEnum {
        PING,
        PONG,
        DATA_CHANGED,
        PRINT
    }
}
