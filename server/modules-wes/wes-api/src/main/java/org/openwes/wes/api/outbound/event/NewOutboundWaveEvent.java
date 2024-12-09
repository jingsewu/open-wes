package org.openwes.wes.api.outbound.event;

import org.openwes.domain.event.DomainEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
public class NewOutboundWaveEvent extends DomainEvent {

    private String waveNo;

}
