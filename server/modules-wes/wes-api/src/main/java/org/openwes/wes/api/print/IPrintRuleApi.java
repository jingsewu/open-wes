package org.openwes.wes.api.print;

import jakarta.validation.Valid;
import org.openwes.wes.api.print.dto.PrintRuleDTO;

public interface IPrintRuleApi {

    void updatePrintRule(@Valid PrintRuleDTO printRuleDTO);

    void createPrintRule(@Valid PrintRuleDTO printRuleDTO);

    void delete(Long id);
}
