package org.openwes.gateway.util;

import org.openwes.common.utils.http.Response;
import org.openwes.common.utils.utils.JsonUtils;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import reactor.core.publisher.Mono;

public class ResponseUtil {

    private ResponseUtil() {

    }

    /**
     * 设置webflux模型响应
     *
     * @param response    ServerHttpResponse
     * @param contentType content-type
     * @param status      http状态码
     * @param value       响应内容
     *
     * @return Mono<Void>
     */
    public static Mono<Void> webFluxResponseWriter(ServerHttpResponse response, String contentType, HttpStatus status, Object value) {
        response.setStatusCode(status);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, contentType);
        Response result = Response.builder().code("" + status.value()).msg(value.toString()).build();
        DataBuffer dataBuffer = response.bufferFactory().wrap(JsonUtils.obj2String(result).getBytes());
        return response.writeWith(Mono.just(dataBuffer));
    }
}
