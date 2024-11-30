package org.openwes.wes.outbound.domain.repository;

import org.openwes.wes.outbound.domain.entity.OutboundWave;

import java.util.Collection;
import java.util.List;

public interface OutboundWaveRepository {

    void save(OutboundWave outboundWave);

    OutboundWave findByWaveNo(String waveNo);

    List<OutboundWave> findByWaveNos(Collection<String> waveNos);

    void saveAll(List<OutboundWave> outboundWaves);
}
