package org.openwes.common.imp.domain.service.impl;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ExportParams;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import org.openwes.common.imp.ExcelSymbol;
import org.openwes.common.imp.ImportService;
import org.openwes.common.imp.domain.model.SymbolCache;
import org.openwes.common.imp.domain.service.ImportHandleService;
import org.openwes.common.imp.domain.service.SymbolCacheFactory;
import org.openwes.common.utils.constants.RedisConstants;
import org.openwes.common.utils.exception.WmsException;
import org.openwes.common.utils.exception.code_enum.CommonErrorDescEnum;
import org.openwes.common.utils.utils.RedisUtils;
import org.openwes.distribute.file.client.FastdfsClient;
import lombok.Data;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import static org.openwes.common.imp.constants.CommonImportConstants.TEMPLATE_FILE_EXT_NAME;

@Data
@Service
public class ImportHandleServiceImpl implements ImportHandleService {

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private FastdfsClient fastdfsClient;

    @Override
    public byte[] generateTemplateFile(ExcelSymbol symbol) {
        // 尝试从 Redis 中获取上一次生成的模版文件在 Fastdfs 中的保存路径
        String remoteFilePath = redisUtils.get(RedisConstants.COMMON_IMPORT_TEMPLATE_FILE + symbol);
        if (StringUtils.isNotEmpty(remoteFilePath)) {
            byte[] data = fastdfsClient.download(remoteFilePath);
            if (data != null) {
                return data;
            }
        }

        // 重新生成模版文件，并保存到 Fastdfs 中
        ByteArrayInputStream is = createTemplateFile(symbol);
        String path = fastdfsClient.updateFile(is, is.available(), TEMPLATE_FILE_EXT_NAME, null);
        redisUtils.set(RedisConstants.COMMON_IMPORT_TEMPLATE_FILE + symbol, path);

        is.reset();
        byte[] data = is.readAllBytes();
        IOUtils.closeQuietly(is);

        return data;
    }

    private ByteArrayInputStream createTemplateFile(ExcelSymbol symbol) {
        SymbolCache cache = SymbolCacheFactory.get(symbol);

        ExportParams params = new ExportParams(null, symbol.name());
        Workbook workbook = ExcelExportUtil.exportExcel(params, cache.getParamClass(), Collections.emptyList());

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            workbook.write(os);
        } catch (IOException e) {
            throw WmsException.throwWmsException(CommonErrorDescEnum.GENERATE_TEMPLATE_FAILED, e.getMessage());
        }

        return new ByteArrayInputStream(os.toByteArray());
    }

    @Override
    public void doImport(ExcelSymbol symbol, InputStream is) {
        SymbolCache cache = SymbolCacheFactory.get(symbol);
        Class paramClass = cache.getParamClass();

        ImportParams params = new ImportParams();
        params.setDataHandler(cache.getDataHandler());
        try {
            List<Object> objects = ExcelImportUtil.importExcel(is, paramClass, params);
            ImportService service = cache.getService();
            service.doImport(objects);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
