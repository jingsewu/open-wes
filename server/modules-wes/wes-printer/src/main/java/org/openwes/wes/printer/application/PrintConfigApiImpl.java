package org.openwes.wes.printer.application;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.print.IPrintConfigApi;
import org.openwes.wes.api.print.dto.PrintConfigDTO;
import org.openwes.wes.printer.domain.entity.PrintConfig;
import org.openwes.wes.printer.domain.repository.PrintConfigRepository;
import org.openwes.wes.printer.domain.transfer.PrintConfigTransfer;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class PrintConfigApiImpl implements IPrintConfigApi {

    private final PrintConfigRepository printConfigRepository;
    private final PrintConfigTransfer printConfigTransfer;

    @Override
    public void updatePrintConfig(PrintConfigDTO printConfigDTO) {
        printConfigRepository.save(printConfigTransfer.toDO(printConfigDTO));
    }

    @Override
    public void createPrintConfig(PrintConfigDTO printConfigDTO) {
        printConfigRepository.save(printConfigTransfer.toDO(printConfigDTO));
    }

    @Override
    public void delete(Long id) {
        PrintConfig printConfig = printConfigRepository.findById(id).orElseThrow();
        printConfig.delete();
        printConfigRepository.save(printConfig);
    }
}
