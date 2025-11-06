package org.openwes.wes.outbound.infrastructure.repository.impl;

import org.openwes.domain.event.AggregatorRoot;
import org.openwes.wes.outbound.domain.entity.OutboundWave;
import org.openwes.wes.outbound.domain.repository.OutboundWaveRepository;
import org.openwes.wes.outbound.infrastructure.persistence.mapper.OutboundWavePORepository;
import org.openwes.wes.outbound.infrastructure.persistence.po.OutboundWavePO;
import org.openwes.wes.outbound.infrastructure.persistence.transfer.OutboundWavePOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboundWaveRepositoryImpl implements OutboundWaveRepository {

    private final OutboundWavePORepository outboundWavePORepository;
    private final OutboundWavePOTransfer outboundWavePOTransfer;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(OutboundWave outboundWave) {
        outboundWave.sendAndClearEvents();
        outboundWavePORepository.save(outboundWavePOTransfer.toPO(outboundWave));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAll(List<OutboundWave> outboundWaves) {
        outboundWaves.forEach(AggregatorRoot::sendAndClearEvents);
        outboundWavePORepository.saveAll(outboundWavePOTransfer.toPOs(outboundWaves));
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
    public OutboundWave findById(Long id) {
        OutboundWavePO outboundWavePO = outboundWavePORepository.findById(id).orElseThrow();
        return outboundWavePOTransfer.toDO(outboundWavePO);
    }
}
