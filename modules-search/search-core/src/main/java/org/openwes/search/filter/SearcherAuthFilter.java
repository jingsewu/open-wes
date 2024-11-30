package org.openwes.search.filter;

import org.openwes.common.utils.user.AuthConstants;
import org.openwes.search.context.SearcherAuthContext;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter(filterName = "searcherAuthFilter", urlPatterns = "/*")
@Slf4j
public class SearcherAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        String warehouseAuth = ((HttpServletRequest) request).getHeader(AuthConstants.AUTH_WAREHOUSE);
        SearcherAuthContext.setWarehouseAuth(warehouseAuth);

        try {
            chain.doFilter(request, response);
        } finally {
            SearcherAuthContext.removeWarehouseAuth();
        }
    }
}
