package org.openwes.api.platform.controller;

import com.alibaba.fastjson2.JSONArray;
import com.google.common.collect.Lists;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.api.platform.domain.service.ApiConfigService;
import org.openwes.api.platform.utils.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequestMapping("template")
@RestController
@Slf4j
@RequiredArgsConstructor
@Tag(name = "Api Platform Module Api")
public class TemplateController {

    private final ApiConfigService apiConfigService;

    @PostMapping(value = "uploadTemplateBatch")
    @Operation(summary = "批量上传自定义模板")
    public Object uploadTemplateBatch(@RequestParam("templateFile") MultipartFile[] files) {
        Map<String, Object> result = new HashMap<>();
        List<String> successList = new ArrayList<>();
        List<String> failMsgList = new ArrayList<>();
        try {
            for (MultipartFile file : files) {
                String fileName = FreeMarkerHelper.uploadTemplate(file);
                successList.add(fileName);
            }
        } catch (Exception e) {
            failMsgList.add("Failure：" + e.getMessage());
        }
        result.put("successCount", successList.size());
        result.put("successList", successList);
        result.put("failCount", files.length - successList.size());
        result.put("failMsgList", failMsgList);
        return result;
    }

    @PostMapping(value = "removeTemplate")
    @Operation(summary = "删除自定义模板")
    public Object removeTemplate(String templateName) {
        try {
            return FreeMarkerHelper.removeTemplate(templateName);
        } catch (Exception e) {
            return false;
        }
    }

    @PostMapping(value = "downloadTemplate")
    @Operation(summary = "下载模板内容")
    public ResponseEntity<byte[]> downloadTemplate(String templateName) throws IOException {
        return DownloadUtils.download(templateName);
    }

    @PostMapping(value = "testTemplate")
    @Operation(summary = "测试请求模板")
    public Object testTemplate(String apiType, String body) {
        JSONArray jsonArray = CommonUtils.parseBody(body);
        assert jsonArray != null;
        List<Object> targetList = Lists.newArrayList(jsonArray.size());

        ApiConfigPO apiConfigPO = apiConfigService.getByCode(apiType);
        jsonArray.forEach(obj -> {
            Object targetObj = ConverterHelper.convertParam(apiConfigPO, obj);
            targetList.add(targetObj);
        });
        return targetList;
    }

}
