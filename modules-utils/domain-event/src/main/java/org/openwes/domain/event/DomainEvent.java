package org.openwes.domain.event;

import org.openwes.common.utils.id.SnowflakeUtils;
import lombok.Data;

import java.io.Serializable;

/**
 * import: DomainEvent object size can't be over 5000
 */
@Data
public class DomainEvent implements Serializable {

    private Long eventId = SnowflakeUtils.generateId();
}
