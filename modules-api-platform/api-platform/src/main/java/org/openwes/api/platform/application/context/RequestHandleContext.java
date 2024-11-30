package org.openwes.api.platform.application.context;

import org.openwes.api.platform.domain.entity.ApiConfigPO;
import org.openwes.common.utils.http.Response;
import lombok.Data;

import java.util.List;

@Data
public class RequestHandleContext {

    private String apiType;

    private ApiConfigPO apiConfig;

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
