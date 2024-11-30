package org.openwes.api.platform.api.dto.callback;

import org.openwes.common.utils.id.SnowflakeUtils;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@Accessors(chain = true)
public class CallbackMessage<T> implements Serializable {
    private Long messageId = SnowflakeUtils.generateId();
    private T data;
}
