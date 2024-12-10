package org.openwes.api.platform.application.service;

import org.openwes.api.platform.application.context.RequestHandleContext;

import java.util.List;

public interface RequestHandlerService {

    /**
     * 接口类型
     *
     * @return
     */
    String getApiType();

    /**
     * 转换请求参数
     *
     * @param context
     */
    void convertParam(RequestHandleContext context);

    /**
     * 参数校验
     *
     * @param context
     */
    void validate(RequestHandleContext context);

    /**
     * 数据填充
     *
     * @param context
     */
    void supply(RequestHandleContext context);

    /**
     * 本地数据存储
     */
    void saveData(RequestHandleContext context);

    /**
     * 服务调用
     */
    void invoke(RequestHandleContext context);

    void afterInvoke(RequestHandleContext context);

    /**
     * 返回结果
     *
     * @return
     */
    Object response(RequestHandleContext context);

    /**
     * 获取目标DTO（Object转换成DTO）
     *
     * @param clz
     * @param <T>
     * @return
     */
    <T> List<T> getTargetList(RequestHandleContext context, Class<T> clz);
}
