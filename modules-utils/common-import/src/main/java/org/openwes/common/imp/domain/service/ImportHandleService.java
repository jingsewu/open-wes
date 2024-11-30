package org.openwes.common.imp.domain.service;

import org.openwes.common.imp.ExcelSymbol;

import java.io.InputStream;

public interface ImportHandleService {

    byte[] generateTemplateFile(ExcelSymbol symbol);

    void doImport(ExcelSymbol symbol, InputStream is);
}
