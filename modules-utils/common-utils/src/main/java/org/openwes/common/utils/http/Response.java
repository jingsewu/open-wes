package org.openwes.common.utils.http;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "响应数据")
public final class Response<T> implements Serializable {

    /**
     * 默认成功代码
     */
    public static final String SUCCESS_CODE = "0";
    /**
     * 默认成功信息
     */
    private static final String SUCCESS_MSG = "success";
    /**
     * 默认失败代码
     */
    private static final String FAIL_CODE = "1";
    /**
     * 默认失败信息
     */
    private static final String FAILED_MSG = "failed";


    /**
     *
     */
    @Builder.Default
    @Schema(title = "响应码")
    private String code = SUCCESS_CODE;

    /**
     * Code对应的msg，提示给用户
     */
    @Builder.Default
    @Schema(title = "响应消息")
    private String msg = SUCCESS_MSG;

    /**
     * 存储返回前端的数据
     */
    @Schema(title = "响应数据")
    private T data;

    public Response(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> Response<T> success() {
        return ((Response<T>) Response.builder().code(SUCCESS_CODE).msg(SUCCESS_MSG).build());
    }

    public static <T> Response<T> fail() {
        return ((Response<T>) Response.builder().code(FAIL_CODE).msg(FAILED_MSG).build());
    }

    public static <T> Response<T> success(T t) {
        final Response<T> success = success();
        success.setData(t);
        return success;
    }

}
