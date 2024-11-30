package org.openwes.wes.outbound.infrastructure.repository.impl;

import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.OutboundWavePORepository;
import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundWavePO;
import org.openwes.wes.outbound.infrastructure.persistence.transfer.OutboundWavePOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundWaveRepositoryImpl implements OutboundWaveRepository {

    private final OutboundWavePORepository outboundWavePORepository;

    private final OutboundWavePOTransfer outboundWavePOTransfer;

    @Override
    public void save(OutboundWave outboundWave) {
        outboundWavePORepository.save(outboundWavePOTransfer.toPO(outboundWave));
    }

    @Override
    public OutboundWave findByWaveNo(String waveNo) {
        OutboundWavePO outboundWavePO = outboundWavePORepository.findByWaveNo(waveNo);
        return outboundWavePOTransfer.toDO(outboundWavePO);
    }

    @Override
    public List<OutboundWave> findByWaveNos(Collection<String> waveNos) {
        List<OutboundWavePO> outboundWavePOS = outboundWavePORepository.findByWaveNoIn(waveNos);
        return outboundWavePOTransfer.toDOs(outboundWavePOS);
    }

    @Override
    public void saveAll(List<OutboundWave> outboundWaves) {
        outboundWavePORepository.saveAll(outboundWavePOTransfer.toPOs(outboundWaves));
    }
}
