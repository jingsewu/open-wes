package org.openwes.domain.event.api;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.common.utils.id.SnowflakeUtils;

import java.io.Serializable;

/**
 * important:
 * 1. DomainEvent object size can't be over 5000
 * 2. One DomainEvent only should have one consumer in mysql mode, if you used mq, then you need to set consumer as different group.
 */
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class DomainEvent implements Serializable {

    private Long eventId = SnowflakeUtils.generateId();

    private Long aggregatorId;

    public DomainEvent(Long aggregatorId) {
        this.aggregatorId = aggregatorId;
    }
}
