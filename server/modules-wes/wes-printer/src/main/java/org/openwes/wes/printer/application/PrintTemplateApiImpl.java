package org.openwes.wes.printer.application;

import lombok.RequiredArgsConstructor;
import org.openwes.wes.api.print.IPrintTemplateApi;
import org.openwes.wes.api.print.dto.PrintTemplateDTO;
import org.openwes.wes.printer.domain.repository.PrintTemplateRepository;
import org.openwes.wes.printer.domain.transfer.PrintTemplateTransfer;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@RequiredArgsConstructor
@Validated
public class PrintTemplateApiImpl implements IPrintTemplateApi {

    private final PrintTemplateRepository printTemplateRepository;
    private final PrintTemplateTransfer printTemplateTransfer;

    @Override
    public void updatePrintTemplate(PrintTemplateDTO printTemplateDTO) {
        printTemplateRepository.save(printTemplateTransfer.toDO(printTemplateDTO));
    }

    @Override
    public void createPrintTemplate(PrintTemplateDTO printTemplateDTO) {
        printTemplateRepository.save(printTemplateTransfer.toDO(printTemplateDTO));
    }
}
