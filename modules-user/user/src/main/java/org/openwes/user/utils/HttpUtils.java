package org.openwes.user.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

@Slf4j
public class HttpUtils {

    private HttpUtils() {

    }

    public static HttpServletRequest getRequest() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return requestAttributes.getRequest();
    }

    public static String getRemoteAddress() {
        HttpServletRequest request = getRequest();
        if (request == null) {
            return "Not found";
        }
        return request.getRemoteAddr();
    }

    /**
     * 获取服务的ip+端口
     *
     * @return <a href="http://ip:port">...</a>
     */
    public static String getServiceAddress() {
        try {
            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (requestAttributes == null) {
                return "";
            }
            HttpServletRequest request = requestAttributes.getRequest();
            String refererUrl = request.getRequestURL().toString();
            return toUrl(refererUrl);
        } catch (Exception e) {
            log.error("获取地址异常", e);
            return "";
        }
    }

    private static String toUrl(String refererUrl) {
        try {
            URL url = URI.create(refererUrl).toURL();
            return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
        } catch (MalformedURLException e) {
            log.error("转换 url '{}' 异常", refererUrl, e);
            return "";
        }
    }
}
