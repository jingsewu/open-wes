package org.openwes.wes.outbound.infrastructure.persistence.mapper;

import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundWavePO;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface OutboundWavePORepository extends JpaRepository<OutboundWavePO, Long> {

    OutboundWavePO findByWaveNo(String waveNo);

    List<OutboundWavePO> findByWaveNoIn(Collection<String> waveNos);
}
