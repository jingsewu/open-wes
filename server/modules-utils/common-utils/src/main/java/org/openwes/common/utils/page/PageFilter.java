package org.openwes.common.utils.page;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@WebFilter(filterName = "pageFilter", urlPatterns = "/*")
@Slf4j
public class PageFilter implements Filter {

    private static final String PAGE = "page";
    private static final String PAGE_SIZE = "perPage";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        PaginationContext.setPageNum(getPageNum(httpRequest));
        PaginationContext.setPageSize(getPageSize(httpRequest));

        try {
            chain.doFilter(request, response);
        } finally {
            PaginationContext.removePageNum();
            PaginationContext.removePageSize();
        }
    }

    /**
     * 获得pager.offset参数的值
     */
    protected int getPageNum(HttpServletRequest request) {
        int pageNum = 0;
        try {
            String pageNumString = request.getParameter(PAGE);
            if (!StringUtils.isEmpty(pageNumString)) {
                pageNum = Integer.parseInt(pageNumString);
                return pageNum;
            }
        } catch (NumberFormatException e) {
            log.error("getPageNum error page:[{}]", request.getParameter(PAGE), e);
        }
        return pageNum;
    }

    /**
     * 设置默认每页大小
     *
     * @return 每页大小
     */
    protected int getPageSize(HttpServletRequest request) {
        // 默认每页10条记录
        int pageSize = PaginationContext.getPageSize();
        try {
            String pageSizes = request.getParameter(PAGE_SIZE);
            if (!StringUtils.isEmpty(pageSizes)) {
                pageSize = Integer.parseInt(pageSizes);
            }
        } catch (NumberFormatException e) {
            log.error("page error pageSize:[{}]", request.getParameter(PAGE_SIZE), e);
        }
        return pageSize;
    }

}
