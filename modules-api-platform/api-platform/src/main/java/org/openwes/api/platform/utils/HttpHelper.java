package org.openwes.api.platform.utils;

import org.openwes.common.utils.utils.JsonUtils;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Slf4j
public class HttpHelper {

    private HttpHelper() {
        throw new UnsupportedOperationException("HttpHelper is not allowed to be instantiated");
    }

    public static Object request(HttpRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(request.getFormat()));

        if (StringUtils.isNotEmpty(request.getToken())) {
            headers.set("Authorization", "Bearer " + request.getToken());
        }

        if (MapUtils.isNotEmpty(request.getHeaders())) {
            request.getHeaders().forEach(headers::add);
        }

        HttpEntity<String> httpEntity = new HttpEntity<>(JsonUtils.obj2String(request.getRequestObj()), headers);
        RestTemplate restTemplate = new RestTemplate();

        Object result;
        result = restTemplate.postForObject(request.getUrl(), httpEntity, Object.class);

        return result;
    }


    @Data
    @Accessors(chain = true)
    public static class HttpRequest {
        private String url;
        private String method;
        private String encoding;
        private String format;
        private Object requestObj;
        private String token;
        private Map<String, String> headers;
    }
}
