package org.openwes.common.imp.domain.model;

import cn.afterturn.easypoi.handler.inter.IExcelDataHandler;
import org.openwes.common.imp.ImportService;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SymbolCache {

    private Class paramClass;

    private IExcelDataHandler dataHandler;

    private ImportService service;
}
