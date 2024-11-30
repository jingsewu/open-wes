package org.openwes.api.platform.utils;

import org.openwes.api.platform.api.constants.TemplateConstant;
import org.apache.commons.io.FileUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class DownloadUtils {

    public static ResponseEntity<byte[]> download(String fileName) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        String downloadFileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        headers.setContentDispositionFormData("attachment", downloadFileName);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);

        InputStream inputStream = null;
        try {
            File file = FileUtils.getFile(TemplateConstant.TEMPLATE_PATH + fileName);

            byte[] content;
            if (file == null || !file.exists()) {
                //挂载的文件不存在，查询resource目录的模板
                if (!fileName.contains("ftl/")) {
                    fileName = "ftl/" + fileName;
                }
                ClassPathResource classPathResource = new ClassPathResource(fileName);
                inputStream = classPathResource.getInputStream();
                content = FileCopyUtils.copyToByteArray(inputStream);
            } else {
                content = FileUtils.readFileToByteArray(file);
            }

            //MediaType:互联网媒介类型  contentType：具体请求中的媒体类型信息
            return new ResponseEntity<byte[]>(content, headers, HttpStatus.CREATED);
        } catch (Exception e) {
            throw e;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}
