package org.openwes.api.platform.api.dto.request;

import lombok.Data;
import org.openwes.api.platform.api.dto.api.ApiConfigDTO;
import org.openwes.common.utils.http.Response;

import java.util.List;

@Data
public class RequestHandleContext {

    private String apiType;

    private ApiConfigDTO apiConfig;

    /**
     * 客户请求的原始内容
     */
    private String body;

    /**
     * 转换后的目标对象
     */
    private List<Object> targetList;

    private Response response = Response.success();
}
