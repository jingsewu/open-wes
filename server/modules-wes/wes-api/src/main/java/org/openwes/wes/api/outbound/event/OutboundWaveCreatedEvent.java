package org.openwes.wes.api.outbound.event;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.openwes.domain.event.api.DomainEvent;

@EqualsAndHashCode(callSuper = true)
@Data
@Accessors(chain = true)
@NoArgsConstructor
public class OutboundWaveCreatedEvent extends DomainEvent {

    private String waveNo;

    public OutboundWaveCreatedEvent(Long id, String waveNo) {
        super(id);
        this.waveNo = waveNo;
    }

}
