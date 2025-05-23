package org.openwes.wes.api.print;

import jakarta.validation.Valid;
import org.openwes.wes.api.print.dto.PrintTemplateDTO;

public interface IPrintTemplateApi {

    void updatePrintTemplate(@Valid PrintTemplateDTO printTemplateDTO);

    void createPrintTemplate(@Valid PrintTemplateDTO printTemplateDTO);
}
