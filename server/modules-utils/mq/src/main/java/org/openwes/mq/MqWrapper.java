package org.openwes.mq;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
public class MqWrapper {
    private String tenantId;
    private String username;

    private Object message;
}
