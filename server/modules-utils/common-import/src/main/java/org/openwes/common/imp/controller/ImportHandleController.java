package org.openwes.common.imp.controller;

import org.openwes.common.imp.ExcelSymbol;
import org.openwes.common.imp.constants.CommonImportConstants;
import org.openwes.common.imp.domain.service.ImportHandleService;
import org.openwes.common.utils.constants.MarkConstant;
import org.openwes.common.utils.http.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/common-import")
public class ImportHandleController {

    private ImportHandleService importHandleService;

    @Autowired
    public ImportHandleController(ImportHandleService importHandleService) {
        this.importHandleService = importHandleService;
    }

    @GetMapping("/downloadTemplate")
    public ResponseEntity<byte[]> downloadTemplate(@RequestParam("symbol") ExcelSymbol symbol) {

        byte[] data = importHandleService.generateTemplateFile(symbol);

        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setAccessControlExposeHeaders(List.of("Content-Disposition"));

        ContentDisposition contentDisposition = ContentDisposition
                .builder("attachment")
                .filename(symbol + MarkConstant.FULL_STOP_MARK + CommonImportConstants.TEMPLATE_FILE_EXT_NAME).build();
        headers.setContentDisposition(contentDisposition);

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    @PostMapping("/import")
    public Object doImport(@RequestParam("symbol") ExcelSymbol symbol, @RequestParam("file") MultipartFile file) throws IOException {
        importHandleService.doImport(symbol, file.getInputStream());
        return Response.success();
    }
}
