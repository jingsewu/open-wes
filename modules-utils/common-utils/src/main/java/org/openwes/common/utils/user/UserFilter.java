package org.openwes.common.utils.user;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter(filterName = "pageFilter", urlPatterns = "/*")
@Slf4j
public class UserFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        String username;
        try {
            username = ((HttpServletRequest) request).getHeader(AuthConstants.USERNAME);
            log.debug("receive outside request: username is {}", username);
        } catch (Exception e) {
            log.error("resolve the request error", e);
            username = AuthConstants.ANONYMOUS_USER;
        }

        if (StringUtils.isEmpty(username)) {
            log.warn("request:{} has no username.", ((HttpServletRequest) request).getRequestURI());
        }

        try {
            UserContext.setAccount(username);
            chain.doFilter(request, response);
        } finally {
            UserContext.removeUser();
        }
    }
}
