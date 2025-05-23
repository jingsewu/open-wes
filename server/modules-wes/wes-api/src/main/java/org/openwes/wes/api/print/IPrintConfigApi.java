package org.openwes.wes.api.print;

import jakarta.validation.Valid;
import org.openwes.wes.api.print.dto.PrintConfigDTO;

public interface IPrintConfigApi {

    void updatePrintConfig(@Valid PrintConfigDTO printConfigDTO);

    void createPrintConfig(@Valid PrintConfigDTO printConfigDTO);

    void delete(Long id);
}
