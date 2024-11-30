package org.openwes.wes.printer.infrastructure.repository;

import org.openwes.wes.printer.domain.entity.PrintConfig;
import org.openwes.wes.printer.domain.repository.PrintConfigRepository;
import org.openwes.wes.printer.infrastructure.persistence.mapper.PrintConfigPORepository;
import org.openwes.wes.printer.infrastructure.persistence.po.PrintConfigPO;
import org.openwes.wes.printer.infrastructure.persistence.transfer.PrintConfigPOTransfer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PrintConfigRepositoryImpl implements PrintConfigRepository {

    private final PrintConfigPORepository printConfigPORepository;
    private final PrintConfigPOTransfer printConfigPOTransfer;

    @Override
    public List<PrintConfig> findAllByWorkStationId(Long workStationId) {
        List<PrintConfigPO> printConfigPOS = printConfigPORepository.findAllByWorkStationId(workStationId);
        return printConfigPOTransfer.toDOs(printConfigPOS);
    }
}
